package code.ponfee.job.custom;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import code.ponfee.commons.json.Jsons;
import code.ponfee.job.common.CommonUtils;
import code.ponfee.job.common.Constants;
import code.ponfee.job.dao.ISchedJobDao;
import code.ponfee.job.dao.cache.SchedJobCached;
import code.ponfee.job.model.SchedJob;
import code.ponfee.job.model.SchedLog;
import code.ponfee.job.spy.ClusterServerHeartBeat;
import code.ponfee.job.spy.SpyJobExecutor;
import code.ponfee.job.spy.SpyJobScheduler;
import code.ponfee.job.spy.SpyNormalJobExecutor;

/**
 * 
 * TODO
 * 1、循环线程，查询任务表中最进5秒内要执行的任务
 * 2、把任务放进优先队列中
 * 3、加一张任务队列表，在这张任务队列中去竞争任务
 * 4、扫描执行失败的任务重试
 * 
 * @author Ponfee
 * @param <T>
 */
public class JobTaskScheduler<T extends Comparable<? super T> & Serializable> {

    private static Logger logger = LoggerFactory.getLogger(SpyJobScheduler.class);

    private final PriorityBlockingQueue<JobTask<T>> queue = new PriorityBlockingQueue<>();
    private final RollingThread rollingThread;
    private volatile boolean started = false;

    public JobTaskScheduler(ISchedJobDao schedJobDao, SchedJobCached schedJobCached, 
                            ThreadPoolTaskExecutor executor, 
                            RedisTemplate<String, byte[]> bytRedis) {
        this(schedJobDao, schedJobCached, executor, 2, bytRedis);
    }

    public JobTaskScheduler(ISchedJobDao schedJobDao, SchedJobCached schedJobCached, 
                           ThreadPoolTaskExecutor executor, int periodSeconds,
                           RedisTemplate<String, byte[]> bytRedis) {
        this.rollingThread = new RollingThread(
            schedJobDao, schedJobCached, executor.getThreadPoolExecutor(),
            new ClusterServerHeartBeat(bytRedis, periodSeconds, Math.max(periodSeconds * 4, 300))
        );
    }

    /**
     * Startup scheduler
     */
    public synchronized void startup() {
        if (this.started) {
            throw new RuntimeException("The scheduler are already started!");
        }
        this.started = true;
        this.rollingThread.start();
    }

    public synchronized void stop() {
        this.started = false;
    }

    public boolean isStarted() {
        return this.started;
    }

    public boolean isStop() {
        return !this.isStarted();
    }

    @Override
    protected void finalize() {
        this.stop();
    }

    /**
     * run self this alone thread
     */
    private final class RollingThread extends Thread {
        final ISchedJobDao schedJobDao;
        final SpyJobExecutor schedJobExecutor;
        final ThreadPoolExecutor threadPoolExecutor;
        final ClusterServerHeartBeat heartbeat;

        RollingThread(ISchedJobDao schedJobDao, SchedJobCached schedJobCached, 
                      ThreadPoolExecutor threadPoolExecutor, ClusterServerHeartBeat heartbeat) {
            this.schedJobDao = schedJobDao;
            this.schedJobExecutor = new SpyNormalJobExecutor(schedJobDao, schedJobCached);
            this.threadPoolExecutor = threadPoolExecutor;
            this.heartbeat = heartbeat;
        }

        @Override
        public void run() {
            while(started) {
                heartbeat.heartbeat(Constants.SERVER_INSTANCE);
                try {
                    // 负载均衡控制(least active load balance)
                    // 查看排名：有序集成员按score值递增(从小到大)顺序排列，
                    // 排名以0为底，score值最小的成员排名为0
                    if (schedJobDao.incrAndRank(Constants.SERVER_INSTANCE, 0) > 0) {
                        return;
                    }
    
                    for (long jobId : schedJobDao.listJobIds()) {
                        // 占位符
                        if (Constants.NONE_JOB_PLACEHOLDER.equals(jobId)) {
                            continue;
                        }
    
                        // 获取调度实体
                        SchedJob job = schedJobDao.get(jobId);
                        if (job == null) {
                            logger.error("schedule job not found[{}].", jobId);
                            continue;
                        } else if (job.getStatus() == SchedJob.STATUS_STOP) {
                            logger.error("schedule job was stop[{}].", jobId);
                            continue;
                        }
    
                        if (job.getIsExecuting() && !heartbeat.isAlive(job.getLastSchedServer())) {
                            // last exec job server maybe shutdown
                            // record exec fail msg
                            schedJobDao.recordLog(new SchedLog(
                                job.getLastSchedServer(), false, false, job.getId(), job.getName(), job.getExecParams(), 
                                job.getLastSchedTime(), new Date(job.getExecTimeMillis()), new Date(), 
                                "Exec server maybe shutdown, correct server: " + Constants.SERVER_INSTANCE
                            ));
    
                            // recovery the failed executing schedule
                            job.setNextSchedTime(
                                job.getRecoverySupport() ? job.getLastSchedTime() : CommonUtils.computeNextFireTime(job)
                            );
                            job.setExecTimeMillis(null);
                            job.setIsExecuting(false);
                            schedJobDao.correctExec(job);
                            continue;
                        }
    
                        // 获取下次执行时间
                        Date now = new Date();
                        if (job.getNextSchedTime() == null) {
                            Date nextFireTime = CommonUtils.computeNextFireTime(job);
                            if (nextFireTime == null) {
                                logger.error("This spy job cannot be fire: {}.", Jsons.toJson(job));
                                job.setStatus(SchedJob.STATUS_STOP);
                                schedJobDao.toggle(job); // stop the job
                                continue; // 不符合可执行的时间范围则跳过
                            }
                            job.setNextSchedTime(nextFireTime);
                        } else if (job.getNextSchedTime().after(now)) {
                            continue; // 还未到执行时间点则跳过
                        }
    
                        // 判断是否需要做串行执行
                        if (   !job.getConcurrentSupport()
                            && job.getIsExecuting()
                            && (now.getTime() - job.getExecTimeMillis()) < clockdiff(job)
                        ) {
                            continue; // 如果 [不支持并发] && [正在执行] && [还未到达防止死锁的超时时间] 则跳过
                        }
    
                        // 尝试获取执行锁
                        if (tryAcquire(job, now)) {
                            schedJobDao.incrAndRank(Constants.SERVER_INSTANCE, job.getScore());
                            threadPoolExecutor.execute(() -> schedJobExecutor.execute(job)); // asnyc exec job
                            //break; // break for loop: 获取到新任务后就不再执行其它任务，等待下一次调度执行
                        }
                    }
                } catch (Throwable e) {
                    logger.error("The job task run occur error.", e);
                }
            }
        }

        /**
         * 尝试获取执行锁
         * @param  job
         * @return boolean
         */
        private boolean tryAcquire(SchedJob job, Date date) {
            Date schedTime = job.getNextSchedTime();
            boolean future;
            if (future = schedTime.after(date)) {
                // 先更新，但不执行
                job.setIsExecuting(false);
                job.setExecTimeMillis(null);
            } else {
                job.setIsExecuting(true); // 执行
                job.setExecTimeMillis(date.getTime());
                job.setLastSchedTime(schedTime); // 本次执行后变为上一次执行时间
                job.setLastSchedServer(Constants.SERVER_INSTANCE); // 执行服务器
                job.setNextSchedTime(CommonUtils.computeNextFireTime(job)); // 更新下一次执行时间点
            }
            // 先获取锁再判断时间
            return schedJobDao.tryAcquire(job) && !future;
        }
        
    }

    /**
     * Calculate the sched time diff
     * 
     * @param job the SchedJob
     * @return a long time millis of the sched time diff
     */
    private static long clockdiff(SchedJob job) {
        return job.getNextSchedTime().getTime() - job.getLastSchedTime().getTime();
    }
}

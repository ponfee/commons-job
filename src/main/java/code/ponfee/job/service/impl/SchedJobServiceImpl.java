package code.ponfee.job.service.impl;

import static code.ponfee.commons.model.ResultCode.BAD_REQUEST;
import static code.ponfee.commons.model.ResultCode.NOT_FOUND;
import static code.ponfee.commons.model.ResultCode.OPS_CONFLICT;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMap;

import code.ponfee.commons.constrain.Constraint;
import code.ponfee.commons.constrain.Constraint.Tense;
import code.ponfee.commons.constrain.Constraints;
import code.ponfee.commons.math.Numbers;
import code.ponfee.commons.model.Page;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.model.ResultCode;
import code.ponfee.commons.util.Dates;
import code.ponfee.job.common.CommonUtils;
import code.ponfee.job.dao.ISchedJobDao;
import code.ponfee.job.dao.cache.SchedJobCached;
import code.ponfee.job.enums.TriggerType;
import code.ponfee.job.handler.JobHandler;
import code.ponfee.job.handler.JobHandlerLoader;
import code.ponfee.job.model.SchedJob;
import code.ponfee.job.model.SchedLog;
import code.ponfee.job.quartz.QuartzJobManager;
import code.ponfee.job.quartz.QuartzUtils;
import code.ponfee.job.service.ISchedJobService;
import code.ponfee.job.spy.SpyJobExecutor;
import code.ponfee.job.spy.SpyNormalJobExecutor;

/**
 * Schedule job service implementation
 * 
 * @author Ponfee
 */
public class SchedJobServiceImpl implements ISchedJobService {

    private @Resource ISchedJobDao   schedJobDao;
    private @Resource SchedJobCached schedJobCached;
    private @Resource ThreadPoolTaskExecutor executor;

    private final QuartzJobManager quartzJobManager;

    public SchedJobServiceImpl() {
        this(null);
    }

    public SchedJobServiceImpl(QuartzJobManager quartzJobManager) {
        this.quartzJobManager = quartzJobManager;
    }

    @Transactional(readOnly = true)
    @Constraints(@Constraint(field = "status", notNull = false, series = { 0, 1 }))
    public @Override Result<Page<SchedJob>> queryJobsForPage(Map<String, ?> params) {
        return Result.success(schedJobDao.queryJobsForPage(params));
    }

    @Transactional(rollbackFor = Exception.class)
    @Constraint(field = "name", notBlank = true, maxLen = 255)
    @Constraint(field = "triggerType", series = { 1, 2, 3, 4, 5, 6, 7, 8, 9 })
    @Constraint(field = "triggerSched", notBlank = true, maxLen = 255)
    @Constraint(field = "handler", notBlank = true)
    @Constraint(field = "status", series = { 0, 1 })
    @Constraint(field = "score", notNull = false, min = 0, max = 100)
    @Constraint(field = "execParams", notNull = false, maxLen = 4000)
    @Constraint(field = "remark", notNull = false, maxLen = 255)
    @Constraint(field = "createBy", notBlank = true, maxLen = 60)
    public @Override Result<Long> addJob(SchedJob job) {
        Result<Long> result = verifyJob(job);
        if (result != null && result.isFailure()) {
            return result;
        }

        this.defaultSetting(job);
        Date nextSchedTime = computeFirstFireTime(job);
        if (nextSchedTime == null) {
            return Result.failure(BAD_REQUEST, "Unreachable trigger: " + triggerString(job));
        }
        job.setNextSchedTime(nextSchedTime);
        job.setCreateTm(new Date());

        // 设置更新人为创建人
        job.setUpdateBy(job.getCreateBy());
        job.setUpdateTm(job.getCreateTm());

        schedJobDao.add(job);

        if (quartzJobManager != null) {
            if (job.getStatus() == SchedJob.STATUS_START) {
                quartzJobManager.addOrUpdateJob(job);
            }
        }

        return Result.success(job.getId());
    }

    //@Transactional(readOnly = true)
    @Constraints(@Constraint(min = 1))
    public @Override Result<SchedJob> getJob(long jobId) {
        return Result.success(schedJobDao.get(jobId));
    }

    @Transactional(rollbackFor = Exception.class)
    @Constraint(index = 0, min = 1)
    @Constraint(index = 1, min = 1)
    public @Override Result<Void> delJob(long jobId, int version) {
        if (schedJobDao.delete(jobId, version)) {
            if (quartzJobManager != null) {
                if (!quartzJobManager.deleteJobIfExists(String.valueOf(jobId))) {
                    throw new RuntimeException("Quartz job delete occur exception");
                }
            }
            return Result.success();
        } else {
            return Result.failure(OPS_CONFLICT);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Constraint(field = "id", min = 1)
    @Constraint(field = "name", notBlank = true, maxLen = 255)
    @Constraint(field = "triggerType", series = { 1, 2, 3, 4, 5, 6, 7, 8, 9 })
    @Constraint(field = "triggerSched", notBlank = true, maxLen = 255)
    @Constraint(field = "handler", notBlank = true)
    @Constraint(field = "status", series = { 0, 1 })
    @Constraint(field = "score", notNull = false, min = 0, max = 100)
    @Constraint(field = "execParams", notNull = false, maxLen = 4000)
    @Constraint(field = "remark", notNull = false, maxLen = 255)
    @Constraint(field = "updateBy", notBlank = true, maxLen = 60)
    @Constraint(field = "version", min = 1)
    public @Override Result<Void> updJob(SchedJob job) {
        Result<Void> result = verifyJob(job);
        if (result != null && result.isFailure()) {
            return result;
        }

        SchedJob job0 = schedJobDao.get(job.getId());
        if (job0 == null) {
            return Result.failure(NOT_FOUND, "Sched job not foud: " + job.getId());
        }

        this.defaultSetting(job);
        job.setLastSchedTime(job0.getLastSchedTime()); // not change
        // 判断时间表达式是否被修改
        if (   !Numbers.equals(job0.getTriggerType(), job.getTriggerType())
            || !StringUtils.equals(job0.getTriggerSched(), job.getTriggerSched())
        ) {
            Date nextSchedTime = computeFirstFireTime(job);
            if (nextSchedTime == null) {
                return Result.failure(BAD_REQUEST, "Unreachable trigger: " + triggerString(job));
            }
            job.setNextSchedTime(nextSchedTime);
        }
        job.setUpdateTm(new Date());
        if (schedJobDao.update(job)) {
            if (quartzJobManager != null) {
                if (job.getStatus() == SchedJob.STATUS_START) {
                    quartzJobManager.addOrUpdateJob(job);
                } else {
                    quartzJobManager.pauseJobIfExists(job.jobName());
                    //quartzJobManager.deleteJobIfExists(job.jobName());
                }
            }
            return Result.success();
        } else {
            return Result.failure(OPS_CONFLICT);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Constraint(index = 0, min = 1)
    @Constraint(index = 1, min = 1)
    public @Override Result<Void> startJob(long jobId, int version) {
        SchedJob job = new SchedJob(jobId, version);
        job.setStatus(SchedJob.STATUS_START);
        Result<Void> result = toggleJob(job);
        if (result.isSuccess()) {
            if (quartzJobManager != null) {
                quartzJobManager.addOrUpdateJob(schedJobDao.get(jobId));
            }
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Constraint(index = 0, min = 1)
    @Constraint(index = 1, min = 1)
    public @Override Result<Void> stopJob(long jobId, int version) {
        SchedJob job = new SchedJob(jobId, version);
        job.setStatus(SchedJob.STATUS_STOP);
        if (quartzJobManager != null) {
            quartzJobManager.pauseJobIfExists(job.jobName());
            //quartzJobManager.deleteJobIfExists(job.jobName());
        }
        return toggleJob(job);
    }

    //@Transactional(readOnly = true) // FIXME @Transactional报错
    @Constraints(@Constraint(min = 1))
    public @Override Result<Void> triggerJob(long jobId) {
        SchedJob job = schedJobDao.get(jobId);
        if (job == null) {
            return Result.failure(NOT_FOUND, "Sched job not found: " + jobId);
        }

        // limit manual trigger frequency
        if (!schedJobCached.manualTrigger(jobId)) {
            return Result.failure(OPS_CONFLICT, "Sched job being trigger.");
        }

        if (quartzJobManager != null) {
            quartzJobManager.triggerJobIfExists(job.jobName());
        } else {
            executor.execute(new JobTriggerOnceAsync(
                new SpyNormalJobExecutor(schedJobDao, schedJobCached), job
            )); // async exec job
        }
        return Result.success();
    }

    @Transactional(readOnly = true)
    @Constraint(field = "pageNum", min = 1, msg = "pageNum不能小于1")
    @Constraint(field = "pageSize", min = 1, msg = "pageSize不能小于1")
    @Constraint(field = "beginTime", notNull = false, tense = Tense.PAST)
    public @Override Result<Page<SchedLog>> queryLogsForPage(Map<String, ?> params) {
        Date beginTime = (Date) params.get("beginTime");
        Date endTime = (Date) params.get("endTime");
        if (beginTime != null && endTime != null && !beginTime.before(endTime)) {
            return Result.failure(BAD_REQUEST, "endTime must be after beginTime.");
        }

        return Result.success(schedJobDao.queryLogsForPage(params));
    }

    // --------------------------------------------------------quartz sched jobs
    @Override
    public Result<List<SchedJob>> listQuartzAllJobs() {
        if (quartzJobManager == null) {
            return Result.failure(ResultCode.SERVER_UNSUPPORT, "Unsupported quartz!");
        }
        return Result.success(quartzJobManager.listAllJobs());
    }

    @Override
    public Result<List<SchedJob>> listQuartzRunningJobs() {
        if (quartzJobManager == null) {
            return Result.failure(ResultCode.SERVER_UNSUPPORT, "Unsupported quartz!");
        }
        return Result.success(quartzJobManager.listRunningJobs());
    }

    // --------------------------------------------------------private method
    private <T> Result<T> verifyJob(SchedJob job) {
        TriggerType type = TriggerType.of(job.getTriggerType());
        if (!QuartzUtils.verifyTrigger(type, job.getTriggerSched())) {
            return Result.failure(BAD_REQUEST, "Invalid trigger: " + triggerString(job));
        }

        try {
            JobHandler<?> handler = JobHandlerLoader.loadHandler(job.getHandler());
            if (!handler.verify(job)) {
                return Result.failure(
                    BAD_REQUEST, "Invalid sched job params: " + job.getExecParams()
                );
            }
        } catch (Exception e) {
            return Result.failure(BAD_REQUEST, "Invalid sched job handler: " + job.getHandler());
        }
        return null;
    }

    private Result<Void> toggleJob(SchedJob job) {
        return schedJobDao.toggle(job) ? Result.success() : Result.failure(OPS_CONFLICT);
    }

    private void defaultSetting(SchedJob job) {
        job.setScore(Numbers.bounds(job.getScore(), 1, 100));
        if (job.getConcurrentSupport() == null) {
            job.setConcurrentSupport(false);
        }
        if (job.getRecoverySupport() == null) {
            job.setRecoverySupport(true);
        }
    }

    /**
     * 推算下一次执行点
     * 
     * @param job
     * @return
     */
    private Date computeFirstFireTime(SchedJob job) {
        job.setLastSchedTime(null);
        if (job.getStartTime() != null) {
            // minus one seconds
            job.setStartTime(Dates.plusSeconds(job.getStartTime(), -1));
        }
        return CommonUtils.computeNextFireTime(job);
    }

    private String triggerString(SchedJob job) {
        return ImmutableMap.of(
            "type", job.getTriggerType(),
            "name", TriggerType.of(job.getTriggerType()).name(),
            "sched", job.getTriggerSched()
        ).toString();
    }

    /**
     * 触发执行一次
     */
    private static final class JobTriggerOnceAsync implements Runnable {
        final SpyJobExecutor executor;
        final SchedJob job;

        JobTriggerOnceAsync(SpyJobExecutor executor, SchedJob job) {
            this.executor = executor;
            job.setManualTrigger(true);
            job.setLastSchedTime(null);
            job.setNextSchedTime(null);
            job.setExecTimeMillis(null);
            this.job = job;
        }

        @Override
        public void run() {
            executor.execute(job);
        }
    }

}

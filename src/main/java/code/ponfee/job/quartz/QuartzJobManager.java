package code.ponfee.job.quartz;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.math.Numbers;
import code.ponfee.commons.util.ObjectUtils;
import code.ponfee.job.common.CommonUtils;
import code.ponfee.job.enums.TriggerType;
import code.ponfee.job.model.SchedJob;

/**
 * Trigger mapped JobDetail is one-to-one
 * 
 * https://www.cnblogs.com/drift-ice/p/3817269.html
 * 
 * @author Ponfee
 */
public class QuartzJobManager implements InitializingBean, DisposableBean {

    public static final String SCHED_JOB_DATA = "sched-job-data";
    public static final String MANUAL_TRIGGER = "manual-trigger";

    private static Logger logger = LoggerFactory.getLogger(QuartzJobManager.class);

    private final Scheduler scheduler;
    private final String    groupName;

    public QuartzJobManager(Scheduler scheduler) throws SchedulerException {
        // scheduler.getSchedulerInstanceId()
        this(scheduler, Scheduler.DEFAULT_GROUP);
    }

    public QuartzJobManager(Scheduler scheduler, String groupName) {
        Assert.notNull(scheduler, "Quartz scheduler cannot be null.");
        this.scheduler = scheduler;
        this.groupName = groupName;
    }

    // ---------------------------------------------------------spring
    @Override
    public void afterPropertiesSet() {
        this.startup();
    }

    @Override
    public void destroy() {
        this.shutdown();
    }

    // ---------------------------------------------------------checkJobExists
    public boolean checkJobExists(SchedJob job) {
        return checkJobExists(job.jobName());
    }

    public boolean checkJobExists(String jobName) {
        return checkJobExists(JobKey.jobKey(jobName, groupName));
    }

    /**
     * Checks the job key exists
     * 
     * @param jobKey the jobKey
     * @return {@code true} means jobKey exists
     */
    public boolean checkJobExists(JobKey jobKey) {
        try {
            return scheduler.checkExists(jobKey);
        } catch (SchedulerException e) {
            throw new RuntimeException("Check quartz job key exists occur error: " 
                           + jobKey.getName() + ", " + jobKey.getGroup() + ".", e);
        }
    }

    // ---------------------------------------------------------assertJobExists
    public void assertJobExists(SchedJob job) {
        checkJobExists(job.jobName());
    }

    public void assertJobExists(String jobName) {
        checkJobExists(JobKey.jobKey(jobName, groupName));
    }

    /**
     * Assert job exists
     * 
     * @param jobKey the jobKey
     */
    public void assertJobExists(JobKey jobKey) {
        if (!checkJobExists(jobKey)) {
            throw new RuntimeException("Quartz job not found: " + jobKey.getName() 
                                     + ", " + jobKey.getGroup() + ".");
        }
    }

    // ---------------------------------------------------------Job operations
    /**
     * Add or update sched job
     * 
     * @param job the SchedJob
     */
    public void addOrUpdateJob(SchedJob job) {
        Date nextFireTime = CommonUtils.computeNextFireTime(job);
        if (nextFireTime == null) {
            logger.error("Unreachable job trigger: {}.", Jsons.toJson(job));
            return;
        }

        try {
            // job detail
            JobKey jobKey = JobKey.jobKey(job.jobName(), groupName);
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            boolean isNewJob = (jobDetail == null);
            SchedJob prevJob; 
            if (isNewJob) {
                Class<? extends Job> clazz = job.getConcurrentSupport() 
                                             ? QuartzNormalJobExecutor.class 
                                             : QuartzDefaultJobExecutor.class;
                boolean recovery = Optional.ofNullable(job.getRecoverySupport()).orElse(false);
                jobDetail = JobBuilder.newJob(clazz)
                                      .withIdentity(job.jobName(), groupName)
                                      .storeDurably(true) // 如果一个任务不是durable，那么当没有Trigger关联它的时候，它就会被自动删除
                                      .requestRecovery(recovery) // 如果一个任务是"requests recovery"， 那么当任务运行过程非正常退出时
                                                                 // （比如进程崩溃，机器断电，但不包括抛出异常这种情况），Quartz再次启动时，会重新运行一次
                                                                 // 这个任务实例，可以通过JobExecutionContext.isRecovering()查询任务是否是被恢复的
                                      .build();
                prevJob = job;
            } else {
                prevJob = Optional.ofNullable((SchedJob) jobDetail.getJobDataMap().get(SCHED_JOB_DATA))
                                  .orElse(job);
            }

            jobDetail.getJobDataMap().put(SCHED_JOB_DATA, job); // add or update job data
            //trigger.getJobDataMap().put(SCHED_JOB_DATA, job);

            // trigger
            TriggerKey triggerKey = TriggerKey.triggerKey(job.jobName(), groupName);
            Trigger trigger;
            if (   !Numbers.equals(job.getTriggerType(), prevJob.getTriggerType()) 
                || !StringUtils.equals(job.getTriggerSched(), prevJob.getTriggerSched()) 
                || (trigger = scheduler.getTrigger(triggerKey)) == null
            ) {
                scheduler.pauseTrigger(triggerKey);
                scheduler.rescheduleJob(triggerKey, trigger = buildTrigger(job, groupName, nextFireTime));
                //scheduler.resumeTrigger(triggerKey);
            }

            if (isNewJob) {
                scheduler.scheduleJob(jobDetail, trigger);
            } else {
                // a stateful job is just the opposite - its JobDataMap 
                // is re-stored after every execution of the job. But
                // otherwise must be call Scheduler#addJob(JobDetail, boolean) 
                // replace the stored job data with the one you are providing. 
                // The next time the job is fired it will see the updated queue.
                scheduler.addJob(jobDetail, true); // store job data for update
                scheduler.resumeJob(jobKey);
            }
        } catch (Exception e) {
            throw new RuntimeException("Add or update quartz job occur error: " + job.getId() + ".", e);
        }
    }

    // ---------------------------------------------------------assertJobExists
    public SchedJob getSchedJob(String jobName) {
        return getSchedJob(JobKey.jobKey(jobName, groupName));
    }

    /**
     * Gets the SchedJob by jobName
     * 
     * @param jobKey the jobKey
     * @return a SchedJob entity
     */
    public SchedJob getSchedJob(JobKey jobKey) {
        try {
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            if (jobDetail == null) {
                return null;
            }
            SchedJob job = (SchedJob) jobDetail.getJobDataMap().get(SCHED_JOB_DATA);
            if (job == null) {
                return null;
            }

            // get the job trigger state
            TriggerState state = TriggerState.NONE;
            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
            if (!ObjectUtils.isEmpty(triggers)) {
                state = scheduler.getTriggerState(triggers.get(0).getKey());
            }
            job.setStatus(state.ordinal());
            return job;
        } catch (SchedulerException e) {
            throw new RuntimeException("Get the quartz job occur error: " + jobKey.getName() + ".", e);
        }
    }

    /**
     * Returns all SchedJob
     * 
     * @return a list of all SchedJob
     */
    public List<SchedJob> listAllJobs() {
        try {
            List<SchedJob> allJobs = new ArrayList<>();
            // GroupMatcher.jobGroupEquals(groupName); scheduler.getJobGroupNames();
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.anyJobGroup())) {
                SchedJob job = getSchedJob(jobKey);
                if (job != null) {
                    allJobs.add(job);
                }
            }
            return allJobs;
        } catch (SchedulerException e) {
            throw new RuntimeException("Find all quartz jobs occur error.", e);
        }
    }

    /**
     * Returns running SchedJob
     * 
     * @return a list of all running SchedJob
     */
    public List<SchedJob> listRunningJobs() {
        try {
            List<SchedJob> runningJobs = new ArrayList<>();
            for (JobExecutionContext execJob : scheduler.getCurrentlyExecutingJobs()) {
                SchedJob job = getSchedJob(execJob.getJobDetail().getKey());
                if (job != null) {
                    runningJobs.add(job);
                }
            }
            return runningJobs;
        } catch (SchedulerException e) {
            throw new RuntimeException("Find running quartz jobs occur error.", e);
        }
    }

    /**
     * Returns all Trigger
     * 
     * @return a list of all Trigger
     */
    public List<Trigger> listAllTriggers() {
        try {
            List<Trigger> allTriggers = new ArrayList<>();
            for (TriggerKey triggerKey : scheduler.getTriggerKeys(GroupMatcher.anyTriggerGroup())) {
                allTriggers.add(QuartzUtils.cloneTrigger(scheduler.getTrigger(triggerKey)));
            }
            return allTriggers;
        } catch (SchedulerException e) {
            throw new RuntimeException("Find all quartz triggers occur error.", e);
        }
    }

    /**
     * Pause a job
     * 
     * @param jobName the jobName
     */
    public void pauseJob(String jobName) {
        JobKey jobKey = JobKey.jobKey(jobName, groupName);
        assertJobExists(jobKey);

        try {
            scheduler.pauseJob(jobKey);
        } catch (SchedulerException e) {
            throw new RuntimeException("Pause quartz job occur error: " + jobName + ".", e);
        }
    }

    public void pauseJobIfExists(String jobName) {
        if (checkJobExists(jobName)) {
            pauseJob(jobName);
        }
    }

    /**
     * Resume a job
     * 
     * @param jobName the jobName
     */
    public void resumeJob(String jobName) {
        JobKey jobKey = JobKey.jobKey(jobName, groupName);
        assertJobExists(jobKey);

        try {
            scheduler.resumeJob(jobKey);
        } catch (SchedulerException e) {
            throw new RuntimeException("Resume quartz job occur error: " + jobName + ".", e);
        }
    }

    public void resumeJobIfExists(String jobName) {
        if (checkJobExists(jobName)) {
            resumeJob(jobName);
        }
    }

    /**
     * Delete a job
     * 
     * @param jobName the jobName
     */
    public boolean deleteJob(String jobName) {
        JobKey jobKey = JobKey.jobKey(jobName, groupName);
        assertJobExists(jobKey);

        try {
            return scheduler.unscheduleJob(TriggerKey.triggerKey(jobName, groupName))
                && scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            throw new RuntimeException("Delete quartz job occur error: " + jobName + ".", e);
        }
    }

    /**
     * Delete a job if exists
     * 
     * @param jobName the jobName
     */
    public boolean deleteJobIfExists(String jobName) {
        return checkJobExists(jobName) && deleteJob(jobName);
    }

    /**
     * Manual trigger once
     * 
     * @param jobName the jobName
     */
    public void triggerJob(String jobName) {
        JobKey jobKey = JobKey.jobKey(jobName, groupName);
        assertJobExists(jobKey);

        try {
            JobDataMap data = new JobDataMap();
            data.put(MANUAL_TRIGGER, true);
            //scheduler.getContext().put(MANUAL_TRIGGER, true); // scheduler instance area
            scheduler.triggerJob(jobKey, data);
        } catch (SchedulerException e) {
            throw new RuntimeException("Trigger quartz job occur error: " + jobName + ".", e);
        }
    }

    public void triggerJobIfExists(String jobName) {
        if (checkJobExists(jobName)) {
            triggerJob(jobName);
        }
    }

    /**
     * Gets the trigger state
     * 
     * @param jobName the jobName
     * @return the int of trigger state
     */
    public TriggerState getTriggerState(String jobName) {
        try {
            return scheduler.getTriggerState(TriggerKey.triggerKey(jobName, groupName));
        } catch (SchedulerException e) {
            throw new RuntimeException("Get quartz trigger state occur error: " + jobName + ".", e);
        }
    }

    /**
     * Starts the scheduler
     * 
     * 启动Scheduler
     */
    public void startup() {
        try {
            if (!scheduler.isStarted()) {
                scheduler.start();
            }
        } catch (SchedulerException e) {
            throw new RuntimeException("Start quartz scheduler occur error.", e);
        }
    }

    /**
     * Standby the scheduler
     * 
     * standby模式时Scheduler暂时停止查找Job去执行
     */
    public void standby() {
        try {
            if (!scheduler.isInStandbyMode()) {
                scheduler.standby();
            }
        } catch (SchedulerException e) {
            throw new RuntimeException("Standby quartz scheduler occur error.", e);
        }
    }

    /**
     * Shutdown the scheduler
     * 
     * 关闭Scheduler
     */
    public void shutdown() {
        try {
            if (!scheduler.isShutdown()) {
                scheduler.shutdown();
            }
        } catch (SchedulerException e) {
            throw new RuntimeException("Shutdown quartz scheduler occur error.", e);
        }
    }

    /**
     * Returns a Trigger
     * 
     * @param job the SchedJob
     * @return a trigger
     */
    private static Trigger buildTrigger(SchedJob job, String groupName, Date nextFireTime) {
        return TriggerBuilder.newTrigger()
        .withIdentity(
            job.jobName(), groupName
        ).withSchedule(
            QuartzUtils.toScheduleBuilder(
                TriggerType.of(job.getTriggerType()), job.getTriggerSched()
            )
        ).startAt(
            nextFireTime
        ).endAt(
            job.getEndTime()
        ).forJob(
            JobKey.jobKey(job.jobName(), groupName)
        ).build();
    }

}

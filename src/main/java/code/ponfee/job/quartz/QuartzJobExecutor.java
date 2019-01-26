package code.ponfee.job.quartz;

import static code.ponfee.job.quartz.QuartzJobManager.MANUAL_TRIGGER;
import static code.ponfee.job.quartz.QuartzJobManager.SCHED_JOB_DATA;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;

import code.ponfee.commons.util.SpringContextHolder;
import code.ponfee.job.common.Constants;
import code.ponfee.job.dao.ISchedJobDao;
import code.ponfee.job.model.SchedJob;
import code.ponfee.job.spy.SpyJobExecutor;

/**
 * Quartz job executor
 * 
 * @see org.springframework.scheduling.quartz.QuartzJobBean
 * 
 * @author Ponfee
 */
public abstract class QuartzJobExecutor implements Job {

    /**
     * 此方法是不允许抛出除JobExecutionException之外的所有异常的（包括RuntimeException)，
     * 所以编码的时候，最好是try-catch住所有的Throwable，小心处理。
     */
    @Override
    public final void execute(JobExecutionContext ctx) throws JobExecutionException {
        //System.out.println(Jsons.toJson(ctx.getJobDetail().getJobDataMap()));
        //System.out.println(Jsons.toJson(ctx.getTrigger().getJobDataMap()));
        //System.out.println(Jsons.toJson(ctx.getMergedJobDataMap()));
        //ctx.isRecovering();
        //ctx.getScheduler().getContext();

        try {
            SchedJob job = (SchedJob) ctx.getJobDetail().getJobDataMap().get(SCHED_JOB_DATA);
            Boolean isManual = (Boolean) ctx.getMergedJobDataMap().get(MANUAL_TRIGGER);
            job.setManualTrigger(isManual);
            if (isManual != null && isManual) {
                // manual trigger job
                job.setLastSchedTime(null);
                job.setNextSchedTime(null);
                job.setExecTimeMillis(null);
                SpringContextHolder.getBean(SpyJobExecutor.class).execute(job);
            } else {
                Trigger trigger = QuartzUtils.cloneTrigger(ctx.getTrigger());
    
                job.setLastSchedTime(adjustCurrentSchedTm(
                    trigger, ctx.getScheduledFireTime(), ctx.getPreviousFireTime()
                ));
                job.setNextSchedTime(trigger.getFireTimeAfter(job.getLastSchedTime()));
                //job.setNextSchedTime(ctx.getNextFireTime()); // the next sched time
                job.setExecTimeMillis(ctx.getFireTime().getTime()); // new Date().getTime()
                job.setIsExecuting(true); // executing
                job.setLastSchedServer(Constants.SERVER_INSTANCE); // the exec server ip address

                // update job sched info to database(t_sched_job)
                ISchedJobDao schedJobDao = SpringContextHolder.getBean(ISchedJobDao.class);
                job.setVersion(schedJobDao.get(job.getId()).getVersion());
                if (schedJobDao.tryAcquire(job)) {
                    job.setVersion(job.getVersion() + 1); // try acquire update sql was increment 1
                }

                SpringContextHolder.getBean(SpyJobExecutor.class).execute(job);

                // when current scheduled be done then update job data for stored quartz table
                ctx.getJobDetail().getJobDataMap().put(SCHED_JOB_DATA, resetJobData(job));
            }
        } catch (Throwable t) {
            throw (t instanceof JobExecutionException)
                ? (JobExecutionException) t
                : new JobExecutionException(t);
        }
    }

    private SchedJob resetJobData(SchedJob job) {
        job.setManualTrigger(null);
        job.setExecTimeMillis(null);
        job.setIsExecuting(false); // exec was done
        return job;
    }

    private Date adjustCurrentSchedTm(Trigger trigger, Date schedTm, Date prevTm) {
        if (prevTm == null) {
            return schedTm;
        }

        Date nextTm = trigger.getFireTimeAfter(prevTm);
        if (nextTm == null) {
            return schedTm;
        } else if (schedTm.before(nextTm)) {
            return prevTm;
        } else if (schedTm.equals(nextTm)) {
            return nextTm;
        } else {
            return adjustCurrentSchedTm(trigger, schedTm, nextTm);
        }
    }

}

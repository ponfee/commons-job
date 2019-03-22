package code.ponfee.job.common;

import java.util.Date;

import code.ponfee.job.enums.TriggerType;
import code.ponfee.job.model.SchedJob;
import code.ponfee.job.quartz.QuartzUtils;

/**
 * Common utility
 * 
 * @author Ponfee
 */
public class CommonUtils {

    /**
     * Returns the next sched fire date time
     * @param job the SchedJob
     * @return a date time of the next time
     */
    public static Date computeNextFireTime(SchedJob job) {
        if (job == null) {
            return null;
        }

        Date now = new Date(), start;
        if (job.getRecoverySupport()) {
            // support recovery
            start = (job.getLastSchedTime() != null) 
                  ? job.getLastSchedTime() 
                  : (job.getStartTime() != null) 
                  ? job.getStartTime() 
                  : now;
        } else {
            // cannot support recovery: discard the past misfire
            start = now;
        }

        TriggerType trigger = TriggerType.of(job.getTriggerType());

        // will be exec code with Dates.plusSeconds(start, 1) in QuartzUtils.getNextFireTime
        Date date = QuartzUtils.getNextFireTime(trigger, job.getTriggerSched(), start);
        if (   date == null
            || (job.getStartTime() != null && date.before(job.getStartTime()))
            || (job.getEndTime() != null && date.after(job.getEndTime()))
        ) {
            return null;
        } else {
            return date;
        }
    }
}

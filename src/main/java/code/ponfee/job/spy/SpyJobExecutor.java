package code.ponfee.job.spy;

import code.ponfee.job.model.SchedJob;

/**
 * Simply job executor interface
 * 
 * @author Ponfee
 */
@FunctionalInterface
public interface SpyJobExecutor {

    void execute(SchedJob job);
}

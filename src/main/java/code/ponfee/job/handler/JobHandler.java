package code.ponfee.job.handler;

import code.ponfee.commons.model.Result;
import code.ponfee.job.model.SchedJob;

/**
 * Actual job handler for biz
 * 
 * @author Ponfee
 */
@FunctionalInterface
public interface JobHandler<T> {

    /**
     * Handles sched job
     * 
     * @param job the SchedJob
     * @return result
     */
    Result<T> handle(SchedJob job);

    /**
     * Verifies SchedJob
     * 
     * @param job the SchedJob
     * @return verify result, {@code true} means verify success
     */
    default boolean verify(SchedJob job) {
        return true;
    }
}

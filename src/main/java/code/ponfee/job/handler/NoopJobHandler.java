package code.ponfee.job.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.model.Result;
import code.ponfee.job.model.SchedJob;

/**
 * No operate job handler
 * 
 * @author Ponfee
 */
@JobHandlerMeta("空操作的调度器")
public class NoopJobHandler implements JobHandler<Void> {

    private static Logger logger = LoggerFactory.getLogger(NoopJobHandler.class);

    @Override
    public Result<Void> handle(SchedJob job) {
        logger.info("Noop job handler exec: {}", Jsons.toJson(job));
        return Result.SUCCESS;
    }

}

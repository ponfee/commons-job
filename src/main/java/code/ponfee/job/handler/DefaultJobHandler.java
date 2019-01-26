package code.ponfee.job.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import code.ponfee.commons.json.Jsons;
import code.ponfee.job.model.SchedJob;

/**
 * Default job handler
 * 
 * @author Ponfee
 */
@JobHandlerMeta("默认调度处理器")
public class DefaultJobHandler implements JobHandler {

    private static Logger logger = LoggerFactory.getLogger(DefaultJobHandler.class);

    @Override
    public boolean handle(SchedJob job) {
        logger.info("Default job handler, job : {}", Jsons.toJson(job));
        return true;
    }

    @Override
    public boolean verify(SchedJob job) {
        return true;
    }

}

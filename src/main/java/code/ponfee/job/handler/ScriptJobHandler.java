package code.ponfee.job.handler;

import code.ponfee.commons.model.Result;
import code.ponfee.job.model.SchedJob;

/**
 * Script job handler
 * 
 * TODO
 * 
 * @author Ponfee
 */
@JobHandlerMeta("执行脚本的调度器")
public class ScriptJobHandler implements JobHandler<String> {

    @Override
    public Result<String> handle(SchedJob job) {
        return Result.success("OK");
    }

}

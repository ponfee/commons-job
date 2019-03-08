package code.ponfee.job.handler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import code.ponfee.commons.model.Result;
import code.ponfee.commons.model.ResultCode;
import code.ponfee.job.model.SchedJob;

/**
 * Command job handler
 * 
 * execParams: cmd.exe /c dir d:\
 * 
 * @author Ponfee
 */
@JobHandlerMeta("执行命令的调度器")
public class CommandJobHandler implements JobHandler<String> {

    private static Logger logger = LoggerFactory.getLogger(CommandJobHandler.class);

    @Override
    public Result<String> handle(SchedJob job) {
        InputStream input = null;
        try {
            Process process = Runtime.getRuntime().exec(job.getExecParams());
            input = process.getInputStream();
            String verbose = IOUtils.toString(input, StandardCharsets.UTF_8);

            process.waitFor();
            int code = process.exitValue();
            if (code == 0) {
                logger.info("Commond verbose: {}.", verbose);
                return Result.success(verbose);
            } else {
                return Result.failure(ResultCode.SERVER_ERROR, "Command fail: " + code + ", verbose: " + verbose);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    logger.error("", e);
                }
            }
        }
    }

}

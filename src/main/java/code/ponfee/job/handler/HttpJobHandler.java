package code.ponfee.job.handler;

import java.net.URL;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import code.ponfee.commons.http.Http;
import code.ponfee.commons.http.HttpStatus;
import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.model.ResultCode;
import code.ponfee.job.model.SchedJob;

/**
 * Http job handler
 * 
 * execParams: {"url":"http://www.baidu.com"}
 * 
 * @author Ponfee
 */
@JobHandlerMeta("执行http请求的调度器")
public class HttpJobHandler implements JobHandler {

    private static Logger logger = LoggerFactory.getLogger(HttpJobHandler.class);

    @Override @SuppressWarnings("unchecked")
    public Result<Void> handle(SchedJob job) {
        Map<String, Object> params = Jsons.fromJson(job.getExecParams(), Map.class);
        Http http = Http.of(String.valueOf(params.get("url")), String.valueOf(params.get("method")));
        if (params.containsKey("params")) {
            http.addParam((Map<String, Object>) params.get("params"));
        }
        if (params.containsKey("data")) {
            http.data((String) params.get("data"));
        }

        String resp = http.request();
        HttpStatus status = http.getStatus();
        if (HttpStatus.Series.valueOf(status) == HttpStatus.Series.SUCCESSFUL) {
            logger.info("Http success: {}, response: {}.", status, resp);
            return Result.SUCCESS;
        } else {
            return Result.failure(ResultCode.SERVER_ERROR, "Http fail: " + status + ", response: " + resp);
        }
    }

    @Override @SuppressWarnings("unchecked")
    public boolean verify(SchedJob job) {
        try {
            Map<String, Object> params = Jsons.fromJson(job.getExecParams(), Map.class);
            new URL(String.valueOf(params.get("url")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}

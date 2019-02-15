package code.ponfee.job.service;

import java.io.IOException;

import org.junit.Test;

import code.ponfee.commons.io.Files;
import code.ponfee.commons.model.PageRequestParams;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.util.MavenProjects;
import code.ponfee.job.BaseTest;
import code.ponfee.job.enums.TriggerType;
import code.ponfee.job.handler.TestJobHandler;
import code.ponfee.job.model.SchedJob;
import code.ponfee.job.service.ISchedJobService;


public class SchedJobServiceTest extends BaseTest<ISchedJobService> {

    @Test
    public void testAdd() {
        SchedJob job = new SchedJob();
        job.setName("调度测试类");
        job.triggerType(TriggerType.CRON);
        job.setTriggerSched("0 */1 * * * ?");
        //job.setHandler("com.sf.sids.job.handler.JobHandlerTest");
        job.setHandler("com.sf.sids.job.handler.DefaultJobHandler");
        job.setExecParams("{\"days\":3}");
        job.setStatus(0);
        job.setCreateBy("alice");
        Result<?> result = getBean().addJob(job);
        printJson(result);
    }

    @Test
    public void testAdd2() throws IOException {
        for (int i = 0; i < 10; i++) {
            SchedJob job = new SchedJob();
            job.setName("repairBug" + i);
            job.triggerType(TriggerType.CRON);
            job.setTriggerSched("0 */2 * * * ?");
            job.setHandler(Files.toString(MavenProjects.getMainJavaFile(TestJobHandler.class)));
            job.setStatus(1);
            job.setCreateBy("tom");
            Result<?> result = getBean().addJob(job);
            printJson(result);
        }
    }

    @Test
    public void testUpdate() throws InterruptedException {
        SchedJob job = new SchedJob();
        job.setId(1L);
        job.setStatus(3);
        job.setName("name22");
        job.setConcurrentSupport(false);
        job.triggerType(TriggerType.CRON);
        job.setTriggerSched("*/30 * * * * ?");
        job.setExecParams("{\"depotId\":11, \"days\":3}");
        job.setHandler("com.sf.sids.job.handler.JobHandlerTest");
        job.setUpdateBy("bob");
        job.setVersion(3);
        Result<?> result = getBean().updJob(job);
        printJson(result);
        Thread.sleep(9999999999L);
    }

    @Test
    public void testStop() {
        Result<?> result = getBean().stopJob(1, 16);
        printJson(result);
    }

    @Test
    public void testStart() {
        Result<?> result = getBean().startJob(1, 17);
        printJson(result);
    }

    @Test
    public void testGet() {
        Result<?> result = getBean().getJob(1);
        printJson(result);
    }

    @Test
    public void testDelete() {
        Result<?> result = getBean().delJob(1, 123);
        printJson(result);
    }

    @Test
    public void testQuery4Page() {
        PageRequestParams params = new PageRequestParams();
        Result<?> result = getBean().queryJobsForPage(params.getParams());
        printJson(result);
    }

    @Test
    public void testTrigger() {
        Result<?> result = getBean().triggerJob(1);
        printJson(result);
    }

    @Test
    public void testQueryLogsForPage() {
        PageRequestParams params = new PageRequestParams();
        //params.put("jobName", "test job");
        params.put("pageSize", 10);
        Result<?> result = getBean().queryLogsForPage(params.getParams());
        printJson(result);
    }

    @Test
    public void run() throws InterruptedException {
        Thread.sleep(9999999999L);
    }
}

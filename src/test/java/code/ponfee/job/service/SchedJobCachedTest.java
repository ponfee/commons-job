package code.ponfee.job.service;

import java.io.IOException;

import org.junit.Test;

import code.ponfee.job.BaseTest;
import code.ponfee.job.dao.cache.SchedJobCached;

public class SchedJobCachedTest extends BaseTest<SchedJobCached> {


    @Test
    public void getSchedJob() throws IOException, InterruptedException {
        for (int i = 0; i < 100; i++) {
            printJson(getBean().getSchedJob(1L));
            Thread.sleep(300);
        }
        
    }

}

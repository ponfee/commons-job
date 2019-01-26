package code.ponfee.job.service;

import org.junit.Test;

import code.ponfee.job.BaseTest;
import code.ponfee.job.dao.mapper.SchedJobMapper;


public class SchedJobMapperTest extends BaseTest<SchedJobMapper> {

    @Test
    public void test3() {
        printJson(getBean().query4list(null));
    }

    @Test
    public void test2() {
        printJson(getBean().get(1));
    }
}

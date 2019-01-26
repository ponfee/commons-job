package code.ponfee.job.service;

import org.junit.Test;

import code.ponfee.job.BaseTest;
import code.ponfee.job.dao.mapper.SchedLogMapper;


public class SchedLogMapperTest extends BaseTest<SchedLogMapper> {

    @Test
    public void test1() {
        //Assert.assertNotNull(getBean().selectByPrimaryKey(1));
        printJson(getBean().selectByPrimaryKey(1L));
        printJson(getBean().selectByPrimaryKey(2L));
    }

    @Test
    public void test2() {
        printJson(getBean().selectAll());
    }

    @Test
    public void test3() {
        printJson(getBean().query4list(null));
    }
}

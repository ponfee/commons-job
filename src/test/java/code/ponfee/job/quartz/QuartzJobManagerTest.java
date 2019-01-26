package code.ponfee.job.quartz;

import java.util.Date;

import code.ponfee.commons.util.Dates;
import code.ponfee.job.BaseTest;
import code.ponfee.job.enums.TriggerType;
import code.ponfee.job.model.SchedJob;
import org.joda.time.DateTime;
import org.junit.Test;

public class QuartzJobManagerTest extends BaseTest<QuartzJobManager> {

    @Test
    public void test1() throws InterruptedException {
        SchedJob job = new SchedJob(1, 10);
        job.setConcurrentSupport(false);
        job.setHandler("com.sf.sids.job.handler.DefaultJobHandler");
        job.triggerType(TriggerType.CRON);
        job.setTriggerSched("0/7 * * * * ?");
        job.setName("调度测试1");
        job.setScore(1);
        try {
            getBean().addOrUpdateJob(job);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.sleep(99999999999L);
    }

    @Test
    public void test2() throws InterruptedException {
        SchedJob job = new SchedJob(2, 10);
        job.setConcurrentSupport(false);
        job.setHandler("com.sf.sids.job.handler.DefaultJobHandler");
        job.triggerType(TriggerType.CRON);
        job.setTriggerSched("0 09,10,12 * * * ?");
        job.setName("调度测试2");
        job.setScore(1);
        try {
            getBean().addOrUpdateJob(job);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.sleep(99999999999L);
    }

    @Test
    public void test3() throws InterruptedException {
        Date start = null;
        TriggerType tKey = TriggerType.CRON;
        System.out.println(Dates.format(QuartzUtils.getNextFireTime(tKey, "0 30,31,39 * * * ?", start)));
    }

    @Test
    public void test4() throws InterruptedException {
        DateTime dt = new DateTime(new Date());
        System.out.println(dt.getYear());
        System.out.println(dt.getYearOfCentury());
        System.out.println(dt.getYearOfEra());
    }
}

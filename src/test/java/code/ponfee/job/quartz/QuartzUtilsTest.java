package code.ponfee.job.quartz;

import java.util.Date;

import code.ponfee.commons.util.Dates;
import code.ponfee.job.common.CommonUtils;
import code.ponfee.job.enums.TriggerType;
import code.ponfee.job.model.SchedJob;

import org.junit.Test;
import org.quartz.CalendarIntervalScheduleBuilder;
import org.quartz.CronScheduleBuilder;
import org.quartz.DailyTimeIntervalScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.triggers.CalendarIntervalTriggerImpl;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.impl.triggers.DailyTimeIntervalTriggerImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.quartz.spi.OperableTrigger;


public class QuartzUtilsTest {

    @Test
    public void test1() {
        int step = 7;
        Date date = Dates.toDate("2008-01-01 02:00:00");
        SimpleTriggerImpl trigger1 = (SimpleTriggerImpl) TriggerBuilder.newTrigger().withSchedule(
                SimpleScheduleBuilder.repeatHourlyForever(step)
        ).startAt(date).build();

        DailyTimeIntervalTriggerImpl trigger2 = (DailyTimeIntervalTriggerImpl) TriggerBuilder.newTrigger().withSchedule(
                DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule().withIntervalInHours(step)
        ).startAt(date).build();

        CalendarIntervalTriggerImpl trigger3 = (CalendarIntervalTriggerImpl) TriggerBuilder.newTrigger().withSchedule(
                CalendarIntervalScheduleBuilder.calendarIntervalSchedule().withIntervalInHours(step)
        ).startAt(date).build();

        Date d1 = new Date(), d2 = new Date(), d3 = new Date(), d4 = new Date();

        for (int i = 0; i < 100; i++) {
            d1 = trigger1.getFireTimeAfter(d1);
            d2 = trigger2.getFireTimeAfter(d2);
            d3 = trigger3.getFireTimeAfter(d3);
            d4 = QuartzUtils.getNextFireTime(TriggerType.HOURLY, "{\"origin\":\"2008-01-01 02:00:00\",\"step\":7}", d4);
            System.out.println(Dates.format(d1));
            System.out.println(Dates.format(d2));
            System.out.println(Dates.format(d3));
            System.out.println(Dates.format(d4));
            System.out.println("============================");
        }
    }

    @Test
    public void test2() {
        QuartzUtils.listNextFireTime("0 0 0/7 * * ?", Dates.toDate("2008-01-01 1:00:00"), 2).forEach(
                d -> System.out.println(Dates.format(d))
        );
    }

    @Test
    public void test3() {
        QuartzUtils.listNextFireTime(TriggerType.HOURLY, "{\"origin\":\"2008-01-01 00:00:00\",\"step\":7}", Dates.toDate("2018-12-06 22:00:00"), 10).forEach(
                d -> System.out.println(Dates.format(d))
        );
    }

    @Test
    public void test4() {
        QuartzUtils.listNextFireTime(TriggerType.ONCE, "2008-01-01 00:00:00", Dates.toDate("2018-12-06 22:00:00"), 10).forEach(
                d -> System.out.println(Dates.format(d))
        );
    }

    @Test
    public void test5() {
        QuartzUtils.listNextFireTime(TriggerType.CRON, "0 0 0/7 * * ?", Dates.toDate("2018-12-06 22:00:00"), 10).forEach(
                d -> System.out.println(Dates.format(d))
        );
    }

    @Test
    public void test6() {
        System.out.println(QuartzUtils.isValidExpression("0 26,29,33 * * * ? 2017"));
    }

    @Test
    public void test7() {
        System.out.println(Dates.format(QuartzUtils.getNextFireTime("0 26,29,33 * * * ? 2018", new Date())));
    }

    @Test
    public void test8() {
        //String cron = "0 26,29,33 * * * ? 2018";
        String cron = "*/30 * * * * ?";
        for (Date date : QuartzUtils.listNextFireTime(cron, new Date(), 6)) {
            System.out.println(Dates.format(date));
        }
    }

    @Test
    public void test9() {
        System.out.println(QuartzUtils.verifyTrigger(TriggerType.CRON, "0 0 0/7 * * ?"));
        System.out.println(QuartzUtils.verifyTrigger(TriggerType.ONCE, "2018-01-01 12:00:02"));
        System.out.println(QuartzUtils.verifyTrigger(TriggerType.DAILY, "{'origin':'2000-01-31 00:00:00','step':1}"));
    }

    @Test
    public void test10() {
        System.out.println(Dates.format(QuartzUtils.getNextFireTime(TriggerType.CRON, "0 0 0/7 * * ?", new Date())));
        System.out.println(Dates.format(QuartzUtils.getNextFireTime(TriggerType.ONCE, "2018-12-11 12:00:02", new Date())));
        System.out.println(Dates.format(QuartzUtils.getNextFireTime(TriggerType.HOURLY, "{'origin':'2000-01-31 00:00:00','step':17, 'abc':1}", new Date())));
    }

    @Test
    public void test11() {
        Date start = new Date();
        String cron = "0 10,44 14 ? 3 WED";
        OperableTrigger trigger = (CronTriggerImpl) TriggerBuilder
                .newTrigger().withSchedule(
                        CronScheduleBuilder.cronSchedule(cron)
                ).startAt(
                        start
                ).build();
        QuartzUtils.listNextFireTime(trigger, start, 5).forEach(d -> System.out.println(Dates.format(d)));
        System.out.println("\n==================================");
        QuartzUtils.listNextFireTime(cron, start, 5).forEach(d -> System.out.println(Dates.format(d)));
    }


    @Test
    public void test13() {
        Date start = new Date();
        OperableTrigger trigger = (OperableTrigger) TriggerBuilder
                .newTrigger().withSchedule(
                        QuartzUtils.toScheduleBuilder(TriggerType.HOURLY, "{\"origin\":\"2008-01-01 00:00:00\",\"step\":7}")
                ).startAt(
                        Dates.toDate("2008-01-01 00:00:00")
                ).build();
        QuartzUtils.listNextFireTime(trigger, start, 5).forEach(d -> System.out.println(Dates.format(d)));
    }


    @Test
    public void test12() {
        // 获取基准时间点
        Date begin = Dates.toDate("2018-12-11 11:02:30");
        Date date = QuartzUtils.getNextFireTime(TriggerType.CRON, "0/30 * * * * ?", begin);
        System.out.println(Dates.format(date));
    }
    
    @Test
    public void test14() {
        SchedJob job = new SchedJob();
        job.setTriggerType(2);
        job.setTriggerSched("2019-01-01 00:00:00");
        job.setConcurrentSupport(false);
        job.setRecoverySupport(false);
        job.setStartTime(Dates.toDate("2018-12-10 20:09:28"));
        job.setEndTime(Dates.toDate("2019-12-28 22:33:00"));
        job.setLastSchedTime(null);
        job.setNextSchedTime(Dates.toDate("2018-12-31 10:00:00"));
        
        Date nextFireTime = CommonUtils.computeNextFireTime(job);
        Trigger t = TriggerBuilder.newTrigger()
        .withSchedule(
             QuartzUtils.toScheduleBuilder(TriggerType.of(job.getTriggerType()), job.getTriggerSched())
        ).startAt(
            nextFireTime
        ).endAt(
            job.getEndTime()
        ).build();
        System.out.println(QuartzUtils.listNextFireTime((OperableTrigger)t, job.getStartTime(), 1));
    }
    
    @Test
    public void test15() {
        System.out.println(QuartzUtils.toCronExpression(new Date()));
    }
}

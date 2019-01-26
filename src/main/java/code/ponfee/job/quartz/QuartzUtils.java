package code.ponfee.job.quartz;

import static code.ponfee.commons.util.Strings.BLANK_CHAR;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.EnumUtils;
import org.quartz.Calendar;
import org.quartz.CalendarIntervalScheduleBuilder;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerUtils;
import org.quartz.impl.calendar.BaseCalendar;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.spi.MutableTrigger;
import org.quartz.spi.OperableTrigger;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.json.TypeReferences;
import code.ponfee.commons.math.Numbers;
import code.ponfee.commons.util.DatePeriods;
import code.ponfee.commons.util.Dates;
import code.ponfee.job.enums.TriggerType;

/**
 * Quartz utility
 * 
 * @author Ponfee
 */
public class QuartzUtils {

    private static final String PARAM_ORIGIN = "origin"; // require
    private static final String PARAM_STEP   = "step";   // optional
    private static final List<String> PARAMS = ImmutableList.of(PARAM_STEP, PARAM_ORIGIN);

    // ------------------------------------------------------------------------------cron expressoin
    /**
     * Checks the cron expression is valid
     * 
     * @param cronExp the cron expression
     * @return {@code ture} means valid cron expression
     */
    public static boolean isValidExpression(String cronExp) {
        return CronExpression.isValidExpression(cronExp);
    }

    /**
     * Gets the next cron fire datetime
     * 
     * @param cronExp the cron expression
     * @param start the start time
     * @return a datetime of next cron fire datetime
     */
    public static Date getNextFireTime(String cronExp, Date start) {
        List<Date> list = listNextFireTime(cronExp, start, 1);
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    /**
     * Lists the next fire datetime reference the spec start datetime
     * 
     * @param start the start time
     * @param cronExp the cron expression
     * @param numTimes the next count
     * @return a list datetime of next cron fire datetime
     */
    public static List<Date> listNextFireTime(String cronExp, Date start, int numTimes) {
        Preconditions.checkArgument(numTimes > 0, "Argument numTimes must be greater than zero.");
        start = (start == null) ? new Date() : Dates.plusSeconds(start, 1);
        try {
            CronTriggerImpl trigger = (CronTriggerImpl) TriggerBuilder
            .newTrigger().withSchedule(
                CronScheduleBuilder.cronSchedule(cronExp)
            ).startAt(
                start
            ).build();

            // compute the first fire time
            start = trigger.computeFirstFireTime(new BaseCalendar());
            if (start == null) {
                return Collections.emptyList();
            }

            List<Date> result = new LinkedList<>();
            result.add(start);
            while (--numTimes > 0) {
                // CronExpression.getNextValidTimeAfter(), CronExpression.getTimeAfter()
                if ((start = trigger.getFireTimeAfter(start)) == null) {
                    break;
                }
                result.add(start);
            }
            return result;
        } catch (Exception ignored) {
            return null; // invalid cron expression
        }
    }

    /**
     * Lists the next fire datetime reference the spec start datetime
     * 
     * <code>
     *  OperableTrigger trigg = (OperableTrigger) TriggerBuilder.newTrigger()
     *  .withSchedule(
     *      toScheduleBuilder(trigger, schedule)
     *  ).startAt(
     *      start
     *  ).build();
     * </code>
     * 
     * @param trigger  the quartz OperableTrigger
     * @param start    the start time
     * @param numTimes the number of times
     * @return a list datetime of next cron fire datetime
     * 
     * @see TriggerUtils#computeFireTimesBetween(OperableTrigger, Calendar, Date, Date)
     */
    public static List<Date> listNextFireTime(OperableTrigger trigger, 
                                              Date start, int numTimes) {
        start = (start == null) ? new Date() : Dates.plusSeconds(start, 1);
        trigger = (OperableTrigger) cloneTrigger(trigger);
        if (trigger.getStartTime() == null) {
            trigger.setStartTime(start);
        }

        List<Date> result = new LinkedList<>();
        while (numTimes-- > 0) {
            start = trigger.getFireTimeAfter(start);
            if (start == null/* || start.after(end)*/) {
                break;
            }
            result.add(start);
        }
        return result;
    }

    // ------------------------------------------------------------------------------trigger sched
    /**
     * Verifys trigger schedule
     * 
     * @param trigger the TriggerType
     * @param schedule the triggerSched
     * @return {@code ture} means is valid trigger sched
     */
    public static boolean verifyTrigger(TriggerType trigger, String schedule) {
        //return getNextFireTime(trigger, schedule, new Date()) != null;
        switch (trigger) {
            case CRON:
                return isValidExpression(schedule);
            case ONCE:
                try {
                    Dates.toDate(schedule);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            default:
                try {
                    Map<String, String> params = Jsons.fromJson(schedule, TypeReferences.MAP_STRING);
                    if (!PARAMS.containsAll(params.keySet())) {
                        return false;
                    }

                    Dates.toDate(params.get(PARAM_ORIGIN));

                    Object step = params.get(PARAM_STEP);
                    if (step != null) {
                        Integer.parseInt(step.toString());
                    }

                    return true;
                } catch (Exception e) {
                    return false;
                }
        }
    }

    public static Date getNextFireTime(TriggerType trigger, String schedule, Date start) {
        List<Date> list = listNextFireTime(trigger, schedule, start, 1);
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    /**
     * Lists the next fire datetime reference the spec start datetime
     * 
     * @param trigger the TriggerType
     * @param schedule  the schedule like as {"origin":"2018-12-06 00:00:00","step":1}
     * @param numTimes the next count
     * @param start the start datetime
     * @return a list datetime of next cron fire datetime
     */
    public static List<Date> listNextFireTime(TriggerType trigger, String schedule,
                                              Date start, int numTimes) {
        Preconditions.checkArgument(numTimes > 0, "Argument count must be generate than zero.");
        switch (trigger) {
            case CRON:
                return listNextFireTime(schedule, start, numTimes);
            case ONCE:
                Date sched = Dates.toDate(schedule);
                return sched.before(start == null ? new Date() : Dates.plusSeconds(start, 1)) 
                       ? Collections.emptyList() : Collections.singletonList(sched);
            default:
                Date origin; int step = 1;
                try {
                    Map<String, String> params = Jsons.fromJson(schedule, TypeReferences.MAP_STRING);
                    step = Numbers.toInt(params.get(PARAM_STEP), 1);
                    origin = Dates.toDate(params.get(PARAM_ORIGIN));
                } catch (Exception e) {
                    origin = Dates.toDate(schedule);
                }

                // plan one
                DatePeriods periods = EnumUtils.getEnum(DatePeriods.class, trigger.name());
                List<Date> result = new LinkedList<>();
                start = (start == null) ? new Date() : Dates.plusSeconds(start, 1);

                if (start.before(origin)) {
                    result.add(start = origin);
                    numTimes--;
                }
                while (numTimes-- > 0) {
                    result.add(start = periods.next(origin, start, step, 1).begin());
                }
                return result;

                /*// plan two
                OperableTrigger trigg = (OperableTrigger) TriggerBuilder.newTrigger()
                .withSchedule(
                    toScheduleBuilder(trigger, schedule)
                ).startAt(
                    origin
                ).build();
                return listNextFireTime(trigg, start, numTimes);*/
        }
    }

    /**
     * Builds trigger schedule
     * 
     * Misfire policy: https://www.cnblogs.com/drift-ice/p/3817269.html
     * 
     * MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY：
     *   忽略MisFire策略，它会在资源合适的时候重新触发所有的MisFire任务，且不会影响现有的调度时间
     * 
     * @param trigger the TriggerType
     * @param schedule the trigger schedule
     * @return a ScheduleBuilder of quartz
     */
    public static ScheduleBuilder<? extends Trigger> toScheduleBuilder(TriggerType trigger, String schedule) {
        switch (trigger) {
            case CRON:
                return CronScheduleBuilder.cronSchedule(schedule)
                                          .withMisfireHandlingInstructionIgnoreMisfires();
            case ONCE:
                return CronScheduleBuilder.cronSchedule(toCronExpression(Dates.toDate(schedule)))
                                          .withMisfireHandlingInstructionIgnoreMisfires();
            default:
                int step = 1;
                try {
                    Map<String, String> params = Jsons.fromJson(schedule, TypeReferences.MAP_STRING);
                    step = Numbers.toInt(params.get(PARAM_STEP), 1);
                } catch (Exception ignored) {
                }

                // DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule();
                CalendarIntervalScheduleBuilder build = CalendarIntervalScheduleBuilder.calendarIntervalSchedule()
                                                                   .withMisfireHandlingInstructionIgnoreMisfires();
                switch (EnumUtils.getEnum(DatePeriods.class, trigger.name())) {
                    case HOURLY:
                        return build.withIntervalInHours(step); // SimpleScheduleBuilder.repeatHourlyForever(step)
                    case DAILY:
                        return build.withIntervalInDays(step);
                    case WEEKLY:
                        return build.withIntervalInWeeks(step);
                    case MONTHLY:
                        return build.withIntervalInMonths(step);
                    case QUARTERLY:
                        return build.withIntervalInMonths(step * 3);
                    case SEMIANNUAL:
                        return build.withIntervalInMonths(step * 6);
                    case ANNUAL:
                        return build.withIntervalInYears(step);
                    default:
                        return null;
                }
        }
    }

    /**
     * Converts a date to cron expression
     * 
     * @param date the date
     * @return a cron expression of the spec date
     */
    public static String toCronExpression(Date date) {
        LocalDateTime ldt = Dates.toLocalDateTime(date);
        return new StringBuilder(22)
            .append(ldt.getSecond()    ).append(BLANK_CHAR) // second
            .append(ldt.getMinute()    ).append(BLANK_CHAR) // minute
            .append(ldt.getHour()      ).append(BLANK_CHAR) // hour
            .append(ldt.getDayOfMonth()).append(BLANK_CHAR) // day
            .append(ldt.getMonthValue()).append(BLANK_CHAR) // month
            .append('?'                ).append(BLANK_CHAR) // week
            .append(ldt.getYear()      )                    // year
            .toString();
    }

    /**
     * Clone trigger if it type of MutableTrigger
     * 
     * @param trigger the Trigger
     * @return other a duplicate trigger
     */
    public static Trigger cloneTrigger(Trigger trigger) {
        if (trigger instanceof MutableTrigger) {
            trigger = (Trigger) ((MutableTrigger) trigger).clone();
        }
        return trigger;
    }

}

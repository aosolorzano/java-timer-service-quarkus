package com.hiperium.timer.service.utils;

import com.hiperium.timer.service.jobs.TaskJob;
import com.hiperium.timer.service.model.Task;
import com.hiperium.timer.service.utils.enums.TaskDaysEnum;
import org.jboss.logging.Logger;
import org.quartz.*;

import java.time.ZoneId;
import java.util.Calendar;
import java.util.*;

/**
 * @author Andres Solorzano
 */
public final class TaskJobUtil {

    private static final Logger LOGGER = Logger.getLogger(TaskJobUtil.class.getName());
    public static final String TASK_JOBS_GROUP = "Task#JobsGroup";
    public static final String TASK_TRIGGERS_GROUP = "Task#TriggersGroup";
    public static final String TASK_ID_DATA_KEY = "taskId";

    private TaskJobUtil() {
    }

    public static JobDetail getJobDetailFromTask(Task task) {
        return JobBuilder.newJob(TaskJob.class)
                .withIdentity(task.getId(), TASK_JOBS_GROUP)
                .usingJobData(TASK_ID_DATA_KEY, task.getId())
                .build();
    }

    public static CronTrigger getTriggerFromTask(Task task, String zoneId) {
        LOGGER.debug("getTriggerFromTask() - zone ID: " + zoneId);
        TriggerBuilder<CronTrigger> triggerBuilder = TriggerBuilder.newTrigger()
                .withIdentity(task.getId(), TASK_TRIGGERS_GROUP)
                .startNow()
                .withSchedule(
                        CronScheduleBuilder.atHourAndMinuteOnGivenDaysOfWeek(
                                        task.getHour(), task.getMinute(),
                                        getIntsFromDaysOfWeek(task.getDaysOfWeek()))
                                .inTimeZone(TimeZone.getTimeZone(ZoneId.of(zoneId))));
        if (Objects.nonNull(task.getExecuteUntil())) {
            Calendar executeUntil = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of(zoneId)));
            executeUntil.set(
                    task.getExecuteUntil().getYear(),
                    task.getExecuteUntil().getMonthValue() - 1,
                    task.getExecuteUntil().getDayOfMonth(),
                    task.getExecuteUntil().getHour(),
                    task.getExecuteUntil().getMinute()
            );
            triggerBuilder.endAt(executeUntil.getTime());
        }
        return triggerBuilder.build();
    }

    public static Integer[] getIntsFromDaysOfWeek(List<String> daysOfWeek) {
        List<Integer> intsDaysOfWeek = new ArrayList<>();
        for (String dayOfWeek : daysOfWeek) {
            TaskDaysEnum daysEnum = TaskDaysEnum.getEnumFromString(dayOfWeek);
            switch (daysEnum) {
                case MON -> intsDaysOfWeek.add(DateBuilder.MONDAY);
                case TUE -> intsDaysOfWeek.add(DateBuilder.TUESDAY);
                case WED -> intsDaysOfWeek.add(DateBuilder.WEDNESDAY);
                case THU -> intsDaysOfWeek.add(DateBuilder.THURSDAY);
                case FRI -> intsDaysOfWeek.add(DateBuilder.FRIDAY);
                case SAT -> intsDaysOfWeek.add(DateBuilder.SATURDAY);
                case SUN -> intsDaysOfWeek.add(DateBuilder.SUNDAY);
                default -> throw new IllegalArgumentException(
                        "The day of the week does not match with the accepted ones: " + daysEnum);
            }
        }
        return intsDaysOfWeek.toArray(Integer[]::new);
    }

}

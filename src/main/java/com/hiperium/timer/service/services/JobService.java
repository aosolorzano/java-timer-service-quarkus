package com.hiperium.timer.service.services;

import com.hiperium.timer.service.model.Task;
import com.hiperium.timer.service.utils.TaskJobUtil;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Date;
import java.util.Objects;

/**
 * @author Andres Solorzano
 */
@ApplicationScoped
public class JobService {

    private static final Logger LOGGER = Logger.getLogger(JobService.class.getName());

    @ConfigProperty(name = "task.time.zone.id")
    String zoneId;

    @Inject
    Scheduler scheduler;

    public Uni<Task> create(Task task) {
        return Uni.createFrom().emitter(emitter -> {
            try {
                this.createAndScheduleJob(task);
                LOGGER.info("Successfully created Job for task with ID: " + task.getId());
                emitter.complete(task);
            } catch (SchedulerException e) {
                LOGGER.error("Error trying to schedule the Job for Task ID: " + task.getId(), e);
                emitter.fail(new UnsupportedOperationException(e.getMessage()));
            }
        });
    }

    public Uni<Task> update(Task task) {
        return Uni.createFrom().emitter(emitter -> {
            try {
                Trigger actualTrigger = this.getTrigger(task);
                if (Objects.isNull(actualTrigger)) {
                    LOGGER.warn("Cannot found a Trigger to reschedule for updated task with ID: " + task.getId());
                    this.createAndScheduleJob(task);
                } else {
                    LOGGER.debug("Actual trigger to update: " + actualTrigger);
                    Trigger newTrigger = TaskJobUtil.getTriggerFromTask(task, this.zoneId);
                    Date newTriggerFirstFire = this.scheduler.rescheduleJob(actualTrigger.getKey(), newTrigger);
                    if (Objects.isNull(newTriggerFirstFire)) {
                        LOGGER.error("Cannot create new Trigger for updated task with ID: " + task.getId());
                    } else {
                        LOGGER.info("Successfully rescheduled trigger for task with ID: " + task.getId());
                    }
                }
                emitter.complete(task);
            } catch (SchedulerException e) {
                LOGGER.error(e.getMessage(), e);
                emitter.fail(new UnsupportedOperationException(e.getMessage()));
            }
        });
    }

    public Uni<Task> delete(Task task) {
        return Uni.createFrom().emitter(emitter -> {
            try {
                if(this.scheduler.deleteJob(JobKey.jobKey(task.getId(), TaskJobUtil.TASK_JOBS_GROUP))) {
                    LOGGER.info("Successfully deleted Job for task with ID: " + task.getId());
                } else {
                    LOGGER.warn("Cannot found a Job to delete for task with ID: " + task.getId());
                }
                emitter.complete(task);
            } catch (SchedulerException e) {
                LOGGER.error(e.getMessage(), e);
                emitter.fail(new UnsupportedOperationException(e.getMessage()));
            }
        });
    }

    private void createAndScheduleJob(Task task) throws SchedulerException {
        JobDetail job = TaskJobUtil.getJobDetailFromTask(task);
        Trigger trigger = TaskJobUtil.getTriggerFromTask(task, this.zoneId);
        this.scheduler.scheduleJob(job, trigger);
    }

    private Trigger getTrigger(Task task) throws SchedulerException {
        Trigger trigger = null;
        for (JobKey jobKey : this.scheduler.getJobKeys(GroupMatcher.jobGroupEquals(TaskJobUtil.TASK_JOBS_GROUP))) {
            if (jobKey.getName().equals(task.getId())) {
                TriggerKey triggerKey = TriggerKey.triggerKey(task.getId(), TaskJobUtil.TASK_TRIGGERS_GROUP);
                trigger = this.scheduler.getTrigger(triggerKey);
            }
        }
        return trigger;
    }
}

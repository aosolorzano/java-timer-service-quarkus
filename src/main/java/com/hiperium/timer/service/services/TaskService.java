package com.hiperium.timer.service.services;

import com.hiperium.timer.service.common.AbstractTaskService;
import com.hiperium.timer.service.jobs.TaskJob;
import com.hiperium.timer.service.model.Task;
import com.hiperium.timer.service.utils.TaskUtil;
import io.smallrye.mutiny.Uni;
import org.quartz.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Andres Solorzano
 */
@ApplicationScoped
public class TaskService extends AbstractTaskService {

    @Inject
    DynamoDbAsyncClient dynamoDBClient;

    @Inject
    Scheduler scheduler;

    public Uni<List<Task>> findAllTasks() {
        return Uni.createFrom()
                .completionStage(() -> this.dynamoDBClient.scan(super.getScanRequest()))
                .onItem()
                .transform(response -> response.items()
                        .stream()
                        .map(TaskUtil::getTaskFromAttributeValues)
                        .collect(Collectors.toList()));
    }

    public Uni<Task> findTask(String id) {
        return Uni.createFrom()
                .completionStage(() -> this.dynamoDBClient.getItem(super.getItemRequest(id)))
                .onItem().transform(itemResponse -> TaskUtil.getTaskFromAttributeValues(itemResponse.item()));
    }

    public Uni<Task> addTask(Task task) throws SchedulerException {
        JobDetail job = JobBuilder.newJob(TaskJob.class)
                .withIdentity(task.getId(), "TaskJobGroup")
                .usingJobData("command", task.getExecutionCommand())
                .build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(task.getId(), "TaskTriggerGroup")
                .startNow()
                .withSchedule(
                        CronScheduleBuilder.atHourAndMinuteOnGivenDaysOfWeek(
                                task.getHour(), task.getMinute(),
                                task.getDaysOfWeek().toArray(Integer[]::new))
                ).build();
        this.scheduler.scheduleJob(job, trigger);

        // INSERT TASK INTO DYNAMODB
        task.setId(TaskUtil.generateUUID(25));
        return Uni.createFrom()
                .completionStage(() -> this.dynamoDBClient.putItem(super.getPutRequest(task)))
                .onItem().transform(response -> task);
    }

    public Uni<Task> updateTask(Task updatedTask) {
        updatedTask.setUpdatedAt(Instant.now());
        return Uni.createFrom()
                .completionStage(() -> this.dynamoDBClient.updateItem(super.getUpdateRequest(updatedTask)))
                .onItem().transform(response -> updatedTask);
    }

    public Uni<Task> deleteTask(Task task) {
        return Uni.createFrom()
                .completionStage(() -> this.dynamoDBClient.deleteItem(super.getDeleteRequest(task)))
                .onItem().transform(response -> task);
    }
}

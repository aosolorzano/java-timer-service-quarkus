package com.hiperium.timer.service.services;

import com.hiperium.timer.service.common.AbstractTaskService;
import com.hiperium.timer.service.model.Task;
import com.hiperium.timer.service.utils.TaskDataUtil;
import com.hiperium.timer.service.utils.TaskJobUtil;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

/**
 * @author Andres Solorzano
 */
@ApplicationScoped
public class TaskService extends AbstractTaskService {

    private static final Logger LOGGER = Logger.getLogger(TaskService.class.getName());

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
                        .map(TaskDataUtil::getTaskFromAttributeValues)
                        .toList());
    }

    public Uni<Task> findTask(String id) {
        return Uni.createFrom()
                .completionStage(() -> this.dynamoDBClient.getItem(super.getItemRequest(id)))
                .invoke(itemResponse -> LOGGER.debug("findTask() - item response: " + itemResponse.item()))
                .onItem()
                .transform(itemResponse -> TaskDataUtil.getTaskFromAttributeValues(itemResponse.item()));
    }

    public Uni<Task> addTask(Task task) {
        task.setId(TaskDataUtil.generateUUID(25));
        return Uni.createFrom()
                .completionStage(() -> this.dynamoDBClient.putItem(super.getPutItemRequest(task)))
                .invoke(() -> this.createTaskJob(task))
                .onItem()
                .transform(response -> task);
    }

    public Uni<Task> updateTask(Task actualTask, Task updatedTask) {
        return Uni.createFrom()
                .completionStage(() -> this.dynamoDBClient.updateItem(
                        super.getUpdateItemRequest(actualTask, updatedTask)))
                .onItem()
                .transform(response -> updatedTask);
    }

    public Uni<Task> deleteTask(Task task) {
        return Uni.createFrom()
                .completionStage(() -> this.dynamoDBClient.deleteItem(super.getDeleteItemRequest(task)))
                .onItem().transform(response -> task);
    }

    private void createTaskJob(Task task) {
        JobDetail job = TaskJobUtil.getJobDetailFromTask(task);
        Trigger trigger = TaskJobUtil.getTriggerFromTask(task, super.zoneId);
        try {
            this.scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            String errorMessage = "Error trying to schedule the Job for Task ID: " + task.getId();
            LOGGER.error(errorMessage, e);
            throw new UnsupportedOperationException(errorMessage);
        }
    }
}

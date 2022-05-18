package com.hiperium.timer.service.services;

import com.hiperium.timer.service.common.AbstractTaskService;
import com.hiperium.timer.service.model.Task;
import com.hiperium.timer.service.utils.TaskDataUtil;
import com.hiperium.timer.service.utils.enums.TaskColumnsEnum;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andres Solorzano
 */
@ApplicationScoped
public class TaskService extends AbstractTaskService {

    private static final Logger LOGGER = Logger.getLogger(TaskService.class.getName());

    @Inject
    DynamoDbAsyncClient dynamoAsyncClient;

    @Inject
    JobService jobService;

    public Uni<Task> create(Task task) {
        LOGGER.debug("create() - START");
        return Uni.createFrom()
                .completionStage(() -> this.dynamoAsyncClient.putItem(
                        super.getPutItemRequest(task)))
                .flatMap(putItemResponse -> this.jobService.create(task));
    }

    public Uni<Task> update(Task actualTask, Task updatedTask) {
        LOGGER.debug("update() - START");
        return Uni.createFrom()
                .completionStage(() -> this.dynamoAsyncClient.updateItem(
                        super.getUpdateItemRequest(actualTask, updatedTask)))
                .flatMap(updateItemResponse -> this.jobService.update(updatedTask));
    }

    public Uni<Task> delete(Task task) {
        LOGGER.debug("delete() - START");
        return Uni.createFrom()
                .completionStage(() -> this.dynamoAsyncClient.deleteItem(
                        super.getDeleteItemRequest(task)))
                .flatMap(deleteItemResponse -> this.jobService.delete(task));
    }

    public Uni<Task> find(String id) {
        LOGGER.debug("find() - START");
        return Uni.createFrom()
                .completionStage(() -> this.dynamoAsyncClient.getItem(
                        super.getItemRequest(id)))
                .map(itemResponse -> TaskDataUtil.getTaskFromAttributeValues(itemResponse.item()));
    }

    public Uni<List<Task>> findAll() {
        LOGGER.debug("findAll() - START");
        return Uni.createFrom()
                .completionStage(() -> this.dynamoAsyncClient.scan(
                        super.getScanRequest()))
                .map(response -> response.items()
                        .stream()
                        .map(TaskDataUtil::getTaskFromAttributeValues)
                        .toList());
    }

    public void executeTask(String taskId) {
        LOGGER.info("executeTask() - START: " + taskId);
        this.find(taskId)
                .subscribe().with(
                    taskResult -> LOGGER.info("Command to execute: " + taskResult.getExecutionCommand()),
                    failureResult -> LOGGER.error("Error to find the task ID => " + failureResult.getMessage()));
    }
}

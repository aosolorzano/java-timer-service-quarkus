package com.hiperium.timer.service.common;

import com.hiperium.timer.service.model.Task;
import com.hiperium.timer.service.utils.TaskBeanUtil;
import com.hiperium.timer.service.utils.TaskDataUtil;
import com.hiperium.timer.service.utils.enums.TaskColumnsEnum;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Andres Solorzano
 */
public abstract class AbstractTaskService {

    private static final Logger LOGGER = Logger.getLogger(AbstractTaskService.class.getName());

    private static final HashMap<String, AttributeValue> TASK_ITEM_KEY = new HashMap<>();

    private static String dynamodbTableName = "Task";

    @ConfigProperty(name = "task.time.zone.id")
    protected String zoneId;

    static {
        String dynamoTable = System.getenv("TABLE_TASK_NAME");
        if (Objects.nonNull(dynamoTable) && !dynamoTable.isBlank()) {
            dynamodbTableName = dynamoTable;
        }
    }

    protected ScanRequest getScanRequest() {
        return ScanRequest.builder()
                .tableName(dynamodbTableName)
                .attributesToGet(TaskDataUtil.COLUM_NAMES)
                .build();
    }

    protected GetItemRequest getItemRequest(String id) {
        LOGGER.debug("getItemRequest() - START: " + id);
        Map<String, AttributeValue> key = new HashMap<>();
        key.put(TaskColumnsEnum.TASK_ID_COL.columnName(), AttributeValue.builder().s(id).build());
        return GetItemRequest.builder()
                .tableName(dynamodbTableName)
                .key(key)
                .attributesToGet(TaskDataUtil.COLUM_NAMES)
                .build();
    }

    protected PutItemRequest getPutItemRequest(Task task) {
        LOGGER.debug("getPutItemRequest() - START: " + task);
        Map<String, AttributeValue> item = new HashMap<>();
        task.setId(TaskDataUtil.generateUUID(25));
        task.setCreatedAt(ZonedDateTime.now(ZoneId.of(this.zoneId)));
        for (TaskColumnsEnum columnNameEnum : TaskColumnsEnum.values()) {
            if (columnNameEnum.equals(TaskColumnsEnum.TASK_UPDATED_AT_COL)) {
                continue;
            }
            item.put(columnNameEnum.columnName(), TaskDataUtil.getAttributeValueFromTask(columnNameEnum, task));
        }
        LOGGER.debug("getPutItemRequest() - Item values: " + item);
        return PutItemRequest.builder()
                .tableName(dynamodbTableName)
                .item(item)
                .build();
    }

    protected UpdateItemRequest getUpdateItemRequest(Task actualTask, Task updatedTask) {
        LOGGER.debug("getUpdateItemRequest() - START: " + updatedTask);
        Map<String, AttributeValueUpdate> updatedValues = new HashMap<>();
        try {
            // ASSIGN ONLY UPDATED COLUMNS
            updatedTask.setUpdatedAt(ZonedDateTime.now(ZoneId.of(this.zoneId)));
            List<TaskColumnsEnum> updatedColumnsEnum = TaskBeanUtil.getModifiedFields(actualTask, updatedTask);
            if (updatedColumnsEnum.isEmpty()) {
                throw new IllegalArgumentException("No modified fields were found in the requested task.");
            }
            for (TaskColumnsEnum columnNameEnum : updatedColumnsEnum) {
                if (columnNameEnum.equals(TaskColumnsEnum.TASK_ID_COL)
                        || columnNameEnum.equals(TaskColumnsEnum.TASK_CREATED_AT_COL)) {
                    continue;
                }
                updatedValues.put(columnNameEnum.columnName(), AttributeValueUpdate.builder()
                        .value(TaskDataUtil.getAttributeValueFromTask(columnNameEnum, updatedTask))
                        .action(AttributeAction.PUT)
                        .build());
            }
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Cannot obtain updated fields using reflection: " + e.getMessage());
            throw new UnsupportedOperationException(
                    "Cannot verify updated fields against actual object in database.");
        }
        LOGGER.debug("getUpdateItemRequest() - Updated values: " + updatedValues);
        return UpdateItemRequest.builder()
                .tableName(dynamodbTableName)
                .key(this.getTaskIdItemKey(updatedTask))
                .attributeUpdates(updatedValues)
                .build();
    }

    protected DeleteItemRequest getDeleteItemRequest(Task task) {
        LOGGER.debug("getDeleteItemRequest() - START: " + task.getId());
        return DeleteItemRequest.builder()
                .tableName(dynamodbTableName)
                .key(this.getTaskIdItemKey(task))
                .build();
    }

    private HashMap<String, AttributeValue> getTaskIdItemKey(Task task) {
        TASK_ITEM_KEY.put(TaskColumnsEnum.TASK_ID_COL.columnName(),
                AttributeValue.builder().s(task.getId()).build());
        return TASK_ITEM_KEY;
    }
}

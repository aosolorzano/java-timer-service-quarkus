package com.hiperium.timer.service.common;

import com.hiperium.timer.service.model.Task;
import com.hiperium.timer.service.utils.TaskColumnNameEnum;
import com.hiperium.timer.service.utils.TaskUtil;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andres Solorzano
 */
public abstract class AbstractTaskService {

    protected ScanRequest getScanRequest() {
        return ScanRequest.builder()
                .tableName(Task.TASK_TABLE_NAME)
                .attributesToGet(TaskUtil.COLUM_NAMES)
                .build();
    }

    protected GetItemRequest getItemRequest(String id) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put(TaskColumnNameEnum.TASK_ID_COL.getColumnName(), AttributeValue.builder().s(id).build());
        return GetItemRequest.builder()
                .tableName(Task.TASK_TABLE_NAME)
                .key(key)
                .attributesToGet(TaskUtil.COLUM_NAMES)
                .build();
    }

    protected PutItemRequest getPutRequest(Task task) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(TaskColumnNameEnum.TASK_ID_COL.getColumnName(),
                AttributeValue.builder().s(task.getId()).build());
        for (TaskColumnNameEnum columnNameEnum : TaskColumnNameEnum.values()) {
            item.put(columnNameEnum.getColumnName(),
                    TaskUtil.getTaskAttributeValue(columnNameEnum, task));
        }
        return PutItemRequest.builder()
                .tableName(Task.TASK_TABLE_NAME)
                .item(item)
                .build();
    }

    protected UpdateItemRequest getUpdateRequest(Task task) {
        HashMap<String, AttributeValue> itemKey = new HashMap<>();
        itemKey.put(TaskColumnNameEnum.TASK_ID_COL.getColumnName(),
                AttributeValue.builder().s(task.getId()).build());
        // ASSIGN COLUMNS TO UPDATE
        Map<String, AttributeValueUpdate> updatedValues = new HashMap<>();
        for (TaskColumnNameEnum columnNameEnum : TaskColumnNameEnum.values()) {
            updatedValues.put(columnNameEnum.getColumnName(),
                    AttributeValueUpdate.builder().value(
                            TaskUtil.getTaskAttributeValue(columnNameEnum, task))
                    .action(AttributeAction.PUT)
                    .build());
        }
        return UpdateItemRequest.builder()
                .tableName(Task.TASK_TABLE_NAME)
                .key(itemKey)
                .attributeUpdates(updatedValues)
                .build();
    }

    protected DeleteItemRequest getDeleteRequest(Task task) {
        HashMap<String, AttributeValue> itemKey = new HashMap<>();
        itemKey.put(TaskColumnNameEnum.TASK_ID_COL.getColumnName(),
                AttributeValue.builder().s(task.getId()).build());
        return DeleteItemRequest.builder()
                .tableName(Task.TASK_TABLE_NAME)
                .key(itemKey)
                .build();
    }
}

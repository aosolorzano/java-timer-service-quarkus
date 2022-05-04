package com.hiperium.timer.service.utils;

import com.hiperium.timer.service.model.Task;
import com.hiperium.timer.service.utils.enums.TaskColumnsEnum;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * @author Andres Solorzano
 */
public final class TaskDataUtil {

    private static final char[] hexArray = "JavaTimerService".toCharArray();

    public static final List<String> COLUM_NAMES = List.of(Arrays
            .stream(TaskColumnsEnum.values())
            .map(TaskColumnsEnum::columnName)
            .toArray(String[]::new));

    private TaskDataUtil() {
        // Nothing to implement
    }

    public static Task getTaskFromAttributeValues(Map<String, AttributeValue> item) {
        Task task = new Task();
        if (Objects.nonNull(item) && !item.isEmpty()) {
            // REQUIRED COLUMNS
            task.setId(
                    item.get(TaskColumnsEnum.TASK_ID_COL.columnName()).s());
            task.setName(
                    item.get(TaskColumnsEnum.TASK_NAME_COL.columnName()).s());
            task.setHour(Integer.valueOf(
                    item.get(TaskColumnsEnum.TASK_HOUR_COL.columnName()).n()));
            task.setMinute(Integer.valueOf(
                    item.get(TaskColumnsEnum.TASK_MINUTE_COL.columnName()).n()));
            task.setDaysOfWeek(
                    item.get(TaskColumnsEnum.TASK_DAYS_OF_WEEK_COL.columnName()).ss());
            task.setExecutionCommand(
                    item.get(TaskColumnsEnum.TASK_EXEC_COMMAND_COL.columnName()).s());
            task.setDescription(
                    item.get(TaskColumnsEnum.TASK_DESCRIPTION_COL.columnName()).s());
            task.setCreatedAt(TaskDataUtil.getZonedDateTimeFromString(
                    item.get(TaskColumnsEnum.TASK_CREATED_AT_COL.columnName()).s()));
            // OPTIONAL COLUMNS
            if (Objects.nonNull(item.get(TaskColumnsEnum.TASK_EXEC_UNTIL_COL.columnName()))) {
                task.setExecuteUntil(TaskDataUtil.getZonedDateTimeFromString(
                        item.get(TaskColumnsEnum.TASK_EXEC_UNTIL_COL.columnName()).s()));
            }
            if (Objects.nonNull(item.get(TaskColumnsEnum.TASK_UPDATED_AT_COL.columnName()))) {
                task.setUpdatedAt(TaskDataUtil.getZonedDateTimeFromString(
                        item.get(TaskColumnsEnum.TASK_UPDATED_AT_COL.columnName()).s()));
            }
        }
        return task;
    }

    public static AttributeValue getAttributeValueFromTask(TaskColumnsEnum columName, Task task) {
        AttributeValue.Builder builder = AttributeValue.builder();
        switch (columName) {
            case TASK_ID_COL -> builder.s(task.getId());
            case TASK_NAME_COL -> builder.s(task.getName());
            case TASK_HOUR_COL -> builder.n(task.getHour().toString());
            case TASK_MINUTE_COL -> builder.n(task.getMinute().toString());
            case TASK_DAYS_OF_WEEK_COL -> builder.ss(task.getDaysOfWeek());
            case TASK_EXEC_COMMAND_COL -> builder.s(task.getExecutionCommand());
            case TASK_EXEC_UNTIL_COL -> {
                if (Objects.isNull(task.getExecuteUntil())) {
                    builder.nul(true);
                } else {
                    builder.s(task.getExecuteUntil().toString());
                }
            }
            case TASK_DESCRIPTION_COL -> builder.s(task.getDescription());
            case TASK_CREATED_AT_COL -> builder.s(task.getCreatedAt().toString());
            case TASK_UPDATED_AT_COL -> builder.s(task.getUpdatedAt().toString());
            default -> throw new UnsupportedOperationException(
                    "Cannot assign the corresponding AttributeValue from Task colum: " + columName);
        }
        return builder.build();
    }

    public static ZonedDateTime getZonedDateTimeFromString(final String date) {
        if (Objects.isNull(date) || date.isEmpty()) {
            return null;
        }
        return ZonedDateTime.parse(date);
    }

    public static String generateUUID(int maxLength) {
        MessageDigest salt;
        try {
            salt = MessageDigest.getInstance("SHA-256");
            salt.update(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e.getMessage());
        }
        String uuid = bytesToHex(salt.digest());
        return maxLength > 0 ? uuid.substring(0, maxLength) : uuid;
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}

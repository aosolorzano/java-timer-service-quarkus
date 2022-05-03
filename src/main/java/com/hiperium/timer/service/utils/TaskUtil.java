package com.hiperium.timer.service.utils;

import com.hiperium.timer.service.model.Task;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;

/**
 * @author Andres Solorzano
 */
public final class TaskUtil {

    private static final char[] hexArray = "JavaTimerService".toCharArray();

    public static final String[] COLUM_NAMES = Arrays
            .stream(TaskColumnNameEnum.values())
            .map(Enum::name)
            .toArray(String[]::new);

    private TaskUtil() {}

    public static Task getTaskFromAttributeValues(Map<String, AttributeValue> item) {
        Task task = new Task();
        if (Objects.nonNull(item) && !item.isEmpty()) {
            task.setId(
                    item.get(TaskColumnNameEnum.TASK_ID_COL.getColumnName()).s());
            task.setName(
                    item.get(TaskColumnNameEnum.TASK_NAME_COL.getColumnName()).s());
            task.setHour(Integer.valueOf(
                    item.get(TaskColumnNameEnum.TASK_HOUR_COL.getColumnName()).n()));
            task.setMinute(Integer.valueOf(
                    item.get(TaskColumnNameEnum.TASK_MINUTE_COL.getColumnName()).n()));
            task.setDaysOfWeek(
                    item.get(TaskColumnNameEnum.TASK_DAYS_OF_WEEK_COL.getColumnName()).ns().stream().map(Integer::valueOf).toList());
            task.setExecutionCommand(
                    item.get(TaskColumnNameEnum.TASK_EXEC_COMMAND_COL.getColumnName()).s());
            task.setDescription(
                    item.get(TaskColumnNameEnum.TASK_DESCRIPTION_COL.getColumnName()).s());
            task.setCreatedAt(TaskUtil.getInstantFromString(
                    item.get(TaskColumnNameEnum.TASK_CREATED_AT_COL.getColumnName()).s()));
            task.setUpdatedAt(TaskUtil.getInstantFromString(
                    item.get(TaskColumnNameEnum.TASK_UPDATED_AT_COL.getColumnName()).s()));
        }
        return task;
    }

    public static AttributeValue getTaskAttributeValue(TaskColumnNameEnum columName, Task task) {
        AttributeValue attributeValue = null;
        switch (columName) {
            // case TASK_ID_COL -> Task ID is assigned separately.
            case TASK_NAME_COL ->
                    attributeValue = AttributeValue.builder().s(task.getName()).build();
            case TASK_HOUR_COL ->
                    attributeValue = AttributeValue.builder().n(task.getHour().toString()).build();
            case TASK_MINUTE_COL ->
                    attributeValue = AttributeValue.builder().n(task.getMinute().toString()).build();
            case TASK_DAYS_OF_WEEK_COL ->
                    attributeValue = AttributeValue.builder().ns(TaskUtil.getStringList(task.getDaysOfWeek())).build();
            case TASK_EXEC_COMMAND_COL ->
                    attributeValue = AttributeValue.builder().s(task.getExecutionCommand()).build();
            case TASK_DESCRIPTION_COL ->
                    attributeValue = AttributeValue.builder().s(task.getDescription()).build();
            case TASK_CREATED_AT_COL ->
                    attributeValue = AttributeValue.builder().s(task.getCreatedAt().toString()).build();
            case TASK_UPDATED_AT_COL ->
                    attributeValue = AttributeValue.builder().s(task.getUpdatedAt().toString()).build();
        }
        return attributeValue;
    }

    public static Instant getInstantFromString(final String date) {
        if (Objects.isNull(date) || date.isEmpty()) {
            return null;
        }
        return Instant.parse(date);
    }

    public static List<String> getStringList(List<Integer> integerList) {
        return integerList.stream()
                .map(Object::toString)
                .toList();
    }

    public static String generateUUID(int maxLength) {
        MessageDigest salt = null;
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

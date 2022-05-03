package com.hiperium.timer.service.utils;

public enum TaskColumnNameEnum {
    TASK_ID_COL("id"),
    TASK_NAME_COL("name"),
    TASK_HOUR_COL("hour"),
    TASK_MINUTE_COL("minute"),
    TASK_DAYS_OF_WEEK_COL("daysOfWeek"),
    TASK_EXEC_COMMAND_COL("executionCommand"),
    TASK_DESCRIPTION_COL("description"),
    TASK_CREATED_AT_COL("createdAt"),
    TASK_UPDATED_AT_COL("updatedAt");

    private final String columnName;
    private TaskColumnNameEnum(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }
}

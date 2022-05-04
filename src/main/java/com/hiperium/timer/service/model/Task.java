package com.hiperium.timer.service.model;

import com.hiperium.timer.service.annotations.ColumnName;
import com.hiperium.timer.service.utils.enums.TaskColumnsEnum;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author Andres Solorzano
 */
@RegisterForReflection
public class Task {

    public static final String TASK_TABLE_NAME = "Task";

    private String id;
    private String name;
    private Integer hour;
    private Integer minute;
    private List<String> daysOfWeek;
    private String executionCommand;
    private ZonedDateTime executeUntil;
    private String description;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public Task() {
        // Nothing to implement
    }

    public Task(String name, Integer hour, Integer minute, List<String> daysOfWeek,
                String executionCommand, ZonedDateTime executeUntil, String description) {
        this.name = name;
        this.hour = hour;
        this.minute = minute;
        this.daysOfWeek = daysOfWeek;
        this.executionCommand = executionCommand;
        this.executeUntil = executeUntil;
        this.description = description;
    }

    @ColumnName(name = TaskColumnsEnum.TASK_ID_COL)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ColumnName(name = TaskColumnsEnum.TASK_NAME_COL)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ColumnName(name = TaskColumnsEnum.TASK_EXEC_COMMAND_COL)
    public String getExecutionCommand() {
        return executionCommand;
    }

    public void setExecutionCommand(String executionCommand) {
        this.executionCommand = executionCommand;
    }

    @ColumnName(name = TaskColumnsEnum.TASK_EXEC_UNTIL_COL)
    public ZonedDateTime getExecuteUntil() {
        return executeUntil;
    }

    public void setExecuteUntil(ZonedDateTime executeUntil) {
        this.executeUntil = executeUntil;
    }

    @ColumnName(name = TaskColumnsEnum.TASK_HOUR_COL)
    public Integer getHour() {
        return hour;
    }

    public void setHour(Integer hour) {
        this.hour = hour;
    }

    @ColumnName(name = TaskColumnsEnum.TASK_MINUTE_COL)
    public Integer getMinute() {
        return minute;
    }

    public void setMinute(Integer minute) {
        this.minute = minute;
    }

    @ColumnName(name = TaskColumnsEnum.TASK_DAYS_OF_WEEK_COL)
    public List<String> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(List<String> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    @ColumnName(name = TaskColumnsEnum.TASK_DESCRIPTION_COL)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id.equals(task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", hour=" + hour +
                ", minute=" + minute +
                ", daysOfWeek=" + daysOfWeek +
                ", executionCommand='" + executionCommand + '\'' +
                ", executeUntil=" + executeUntil +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

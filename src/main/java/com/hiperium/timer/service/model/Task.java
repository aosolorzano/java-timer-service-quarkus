package com.hiperium.timer.service.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Instant;
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
    private List<Integer> daysOfWeek;
    private String executionCommand;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    public Task() {
        this.createdAt = Instant.now();
    }

    public Task(String id, String name, Integer hour, Integer minute, List<Integer> daysOfWeek,
                String executionCommand, String description) {
        this.id = id;
        this.name = name;
        this.hour = hour;
        this.minute = minute;
        this.daysOfWeek = daysOfWeek;
        this.executionCommand = executionCommand;
        this.description = description;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExecutionCommand() {
        return executionCommand;
    }

    public void setExecutionCommand(String executionCommand) {
        this.executionCommand = executionCommand;
    }

    public Integer getHour() {
        return hour;
    }

    public void setHour(Integer hour) {
        this.hour = hour;
    }

    public Integer getMinute() {
        return minute;
    }

    public void setMinute(Integer minute) {
        this.minute = minute;
    }

    public List<Integer> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(List<Integer> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
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
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

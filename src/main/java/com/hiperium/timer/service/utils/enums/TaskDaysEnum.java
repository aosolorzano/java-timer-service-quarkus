package com.hiperium.timer.service.utils.enums;

public enum TaskDaysEnum {
    MON,
    TUE,
    WED,
    THU,
    FRI,
    SAT,
    SUN;

    public static TaskDaysEnum getEnumFromString(String dayOfWeek) {
        TaskDaysEnum result = null;
        for (TaskDaysEnum daysEnum : TaskDaysEnum.values()) {
            if (daysEnum.name().equals(dayOfWeek)) {
                result = daysEnum;
                break;
            }
        }
        return result;
    }
}

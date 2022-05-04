package com.hiperium.timer.service.utils;

import com.hiperium.timer.service.annotations.ColumnName;
import com.hiperium.timer.service.model.Task;
import com.hiperium.timer.service.utils.enums.TaskColumnsEnum;
import org.jboss.logging.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Andres Solorzano
 */
public final class TaskBeanUtil {

    private static final Logger LOGGER = Logger.getLogger(TaskBeanUtil.class.getName());

    private TaskBeanUtil() {
    }

    public static List<TaskColumnsEnum> getModifiedFields(Task actualTask, Task updatedTask)
            throws ReflectiveOperationException {
        LOGGER.debug("getModifiedFields() - START");
        List<TaskColumnsEnum> changedProperties = new ArrayList<>();
        for (Method method : actualTask.getClass().getDeclaredMethods()) {
            if (method.getName().startsWith("get") && method.isAnnotationPresent(ColumnName.class)) {
                Object actualObjectValue;
                Object updatedObjectValue;
                try {
                    actualObjectValue = method.invoke(actualTask);
                    updatedObjectValue = method.invoke(updatedTask);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new ReflectiveOperationException(e.getMessage());
                }
                if (!Objects.equals(actualObjectValue, updatedObjectValue)) {
                    ColumnName taskFieldColumnName = method.getAnnotation(ColumnName.class);
                    changedProperties.add(taskFieldColumnName.name());
                }
            }
        }
        LOGGER.debug("getModifiedFields() - Changed columns Enum: " + changedProperties);
        return changedProperties;
    }
}

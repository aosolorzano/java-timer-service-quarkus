package com.hiperium.timer.service.resources;

import com.hiperium.timer.service.model.Task;
import com.hiperium.timer.service.utils.TaskBeanUtil;
import com.hiperium.timer.service.utils.TaskDataUtil;
import com.hiperium.timer.service.utils.enums.TaskColumnsEnum;
import com.hiperium.timer.service.utils.enums.TaskDaysEnum;
import io.quarkus.test.junit.QuarkusTest;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class TaskResourceTest {

    private static final Logger LOGGER = Logger.getLogger(TaskResourceTest.class.getName());

    @Test
    void mustGetAcceptedResponse() {
        given()
          .when().get("/tasks")
          .then()
             .statusCode(200);
    }

    @Test
    void mustParseZonedDateTime() {
        String dateStr = "2022-05-03T15:54:59.586951-05:00";
        ZonedDateTime zonedDateTime = TaskDataUtil.getZonedDateTimeFromString(dateStr);
        LOGGER.debug("ZonedDateTime object from string: " + zonedDateTime);
        Assertions.assertNotNull(zonedDateTime);
    }

    @Test
    void mustVerifyTaskColumnNumber() {
        TaskDataUtil.COLUM_NAMES
                .forEach(columnName -> LOGGER.debug("Task column name: " + columnName));
        Assertions.assertEquals(10, TaskDataUtil.COLUM_NAMES.size());
    }

    @Test
    void mustVerifyChangedTaskFields() throws ReflectiveOperationException {
        Task actual = new Task("Task name", 10, 10,
                List.of(TaskDaysEnum.TUE.name(), TaskDaysEnum.THU.name(), TaskDaysEnum.SAT.name()),
                "Activate garbage collector robot.",
                null,
                "Execute command to start the Task.");
        Task updated = new Task("Task name", 12, 10,
                List.of(TaskDaysEnum.TUE.name(), TaskDaysEnum.THU.name(), TaskDaysEnum.FRI.name()),
                "Activate garbage collector robot.",
                ZonedDateTime.now(),
                "Execute command to start the Task.");
        List<TaskColumnsEnum> changedProperties;
        try {
            changedProperties = new ArrayList<>(TaskBeanUtil.getModifiedFields(actual, updated));
            LOGGER.debug("Changed properties result: " + changedProperties);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("Cannot execute object field value difference: " + e.getMessage());
            throw new ReflectiveOperationException(e.getMessage());
        }
        Assertions.assertEquals(3, changedProperties.size());
    }
}
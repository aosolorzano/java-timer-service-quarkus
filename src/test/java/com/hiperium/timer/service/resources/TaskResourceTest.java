package com.hiperium.timer.service.resources;

import com.hiperium.timer.service.model.Task;
import com.hiperium.timer.service.services.JobService;
import com.hiperium.timer.service.utils.TaskBeanUtil;
import com.hiperium.timer.service.utils.TaskDataUtil;
import com.hiperium.timer.service.utils.enums.TaskColumnsEnum;
import com.hiperium.timer.service.utils.enums.TaskDaysEnum;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.quartz.Trigger;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class TaskResourceTest {

    private static final Logger LOGGER = Logger.getLogger(TaskResourceTest.class.getName());

    @Inject
    JobService jobService;

    private static Task testTask;

    @BeforeAll
    static void beforeAll() {
        testTask = new Task("Task name", 15, 15,
                List.of(TaskDaysEnum.WED.name()),
                "java -jar execute-robot.jar",
                null,
                "Task description");
    }

    @Test
    @Order(1)
    void mustCreateTask() {
        given()
                .contentType(ContentType.JSON)
                .body(testTask)
                .when().post("/tasks")
                .then()
                .statusCode(201);
    }

    @Test
    @Order(2)
    void mustFindAllJobs() {
        List<Trigger> triggers = this.jobService.findAll();
        Assertions.assertFalse(triggers.isEmpty());
    }

    @Test
    @Order(3)
    void mustFindAllTasks() {
        given()
                .when()
                .get("/tasks")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(4)
    void mustParseZonedDateTime() {
        String dateStr = "2022-05-03T15:54:59.586951-05:00";
        ZonedDateTime zonedDateTime = TaskDataUtil.getZonedDateTimeFromString(dateStr);
        LOGGER.debug("ZonedDateTime object from string: " + zonedDateTime);
        Assertions.assertNotNull(zonedDateTime);
    }

    @Test
    @Order(5)
    void mustVerifyTaskColumnNumber() {
        TaskDataUtil.COLUM_NAMES
                .forEach(columnName -> LOGGER.debug("Task column name: " + columnName));
        Assertions.assertEquals(10, TaskDataUtil.COLUM_NAMES.size());
    }

    @Test
    @Order(6)
    void mustVerifyChangedTaskFields() throws ReflectiveOperationException {
        Task updated = new Task("Task name", 12, 10,
                List.of(TaskDaysEnum.TUE.name(), TaskDaysEnum.THU.name(), TaskDaysEnum.FRI.name()),
                "Activate garbage collector robot.",
                ZonedDateTime.now(),
                "Execute command to start the Task.");
        List<TaskColumnsEnum> changedProperties;
        try {
            changedProperties = new ArrayList<>(TaskBeanUtil.getModifiedFields(testTask, updated));
            LOGGER.debug("Changed properties result: " + changedProperties);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("Cannot execute object field value difference: " + e.getMessage());
            throw new ReflectiveOperationException(e.getMessage());
        }
        Assertions.assertEquals(6, changedProperties.size());
    }
}
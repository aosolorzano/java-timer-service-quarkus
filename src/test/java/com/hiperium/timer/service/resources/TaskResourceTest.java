package com.hiperium.timer.service.resources;

import com.hiperium.timer.service.utils.TaskUtil;
import io.quarkus.test.junit.QuarkusTest;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class TaskResourceTest {

    private static final Logger LOGGER = Logger.getLogger(TaskResourceTest.class.getName());

    @Test
    void testHelloEndpoint() {
        given()
          .when().get("/tasks")
          .then()
             .statusCode(200);
    }

    @Test
    void parseExecutionDateTime() {
        String dateStr = "2022-05-01T20:00:56.627634Z";
        Instant instant = TaskUtil.getInstantFromString(dateStr);
        LOGGER.debug("Instant object from string: " + instant);
        Assertions.assertNotNull(instant);
    }
}
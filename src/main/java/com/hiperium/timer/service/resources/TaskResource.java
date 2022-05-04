package com.hiperium.timer.service.resources;

import com.hiperium.timer.service.model.Task;
import com.hiperium.timer.service.services.TaskService;
import com.hiperium.timer.service.utils.enums.TaskDaysEnum;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;
import org.quartz.SchedulerException;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CREATED;

/**
 * @author Andres Solorzano
 */
@Path("/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TaskResource {

    private static final Logger LOGGER = Logger.getLogger(TaskResource.class.getName());

    @Inject
    TaskService service;

    @GET
    public Uni<List<Task>> findAllTasks() {
        LOGGER.debug("findAllTasks() - START");
        return service.findAllTasks();
    }

    @GET
    @Path("{id}")
    public Uni<Response> findTask(@PathParam("id") String id) {
        LOGGER.debug("findTask() - START: " + id);
        return service.findTask(id).onItem().ifNotNull()
                .transform(entity -> Response.ok(entity).build());
    }

    @POST
    public Uni<Response> addTask(Task task) throws SchedulerException {
        LOGGER.debug("addTask() - START: " + task);
        if (Objects.isNull(task) || Objects.nonNull(task.getId())) {
            throw new WebApplicationException(
                    "Resource was not set properly for this request.", BAD_REQUEST);
        }
        return this.service.addTask(task).onItem()
                .transform(response -> Response.ok(task).status(CREATED).build());
    }

    @PUT
    @Path("{id}")
    public Uni<Response> updateTask(@PathParam("id") String id, Task updatedTask) {
        LOGGER.debug("updateTask() - START: " + updatedTask);
        if (Objects.isNull(id) || id.isEmpty() || Objects.isNull(updatedTask)) {
            throw new WebApplicationException(
                    "Resource attributes was not set properly on the request.", BAD_REQUEST);
        }
        return this.service.findTask(id).onItem()
                .transformToUni(actualTask -> this.service.updateTask(actualTask, updatedTask))
                .onItem().ifNotNull().transform(task -> Response.ok().build());
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> deleteTask(@PathParam("id") String id) {
        LOGGER.debug("deleteTask() - START: " + id);
        if (Objects.isNull(id) || id.isEmpty()) {
            throw new WebApplicationException(
                    "Resource ID was not set properly on the request.", BAD_REQUEST);
        }
        return this.service.findTask(id).onItem()
                .transformToUni(actualTask -> this.service.deleteTask(actualTask))
                .onItem().ifNotNull().transform(task -> Response.ok().build());
    }

    @GET
    @Path("getJsonTemplate")
    public Response getJsonTemplate() {
        LOGGER.debug("getJsonTemplate() - START");
        Task task = new Task("Task name", 10, 10,
                List.of(TaskDaysEnum.TUE.name(), TaskDaysEnum.THU.name(), TaskDaysEnum.SAT.name()),
                "Activate garbage collector robot.",
                ZonedDateTime.now().plusYears(5),
                "Execute command to start the Task.");
        return Response.ok(task).build();
    }
}
package com.hiperium.timer.service.providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hiperium.timer.service.utils.TaskExceptionUtil;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Create an HTTP response from an exception.
 * <p>
 * Response Example:
 *
 * <pre>
 *     HTTP/1.1 422 Unprocessable Entity
 *     Content-Length: 111
 *     Content-Type: application/json
 *     {
 *         "code": 422,
 *         "error": "Task ID was not set on request.",
 *         "exceptionType": "javax.ws.rs.WebApplicationException"
 *      }
 * </pre>
 */
@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    private static final Logger LOGGER = Logger.getLogger(WebApplicationExceptionMapper.class.getName());

    @Inject
    ObjectMapper objectMapper;

    @Override
    public Response toResponse(WebApplicationException exception) {
        LOGGER.error("Failed to handle the Task request: " + exception.getMessage(), exception);
        String appCode = "ERR-001";
        ObjectNode exceptionJson = TaskExceptionUtil.getExceptionDetail(exception, this.objectMapper, appCode);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(exceptionJson)
                .build();
    }
}

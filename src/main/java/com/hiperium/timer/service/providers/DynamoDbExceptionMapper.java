package com.hiperium.timer.service.providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hiperium.timer.service.utils.TaskExceptionUtil;
import org.jboss.logging.Logger;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import javax.inject.Inject;
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
 *         "exceptionType": "software.amazon.awssdk.services.dynamodb.model.DynamoDbException"
 *      }
 * </pre>
 */
@Provider
public class DynamoDbExceptionMapper implements ExceptionMapper<DynamoDbException> {

    private static final Logger LOGGER = Logger.getLogger(DynamoDbExceptionMapper.class.getName());

    @Inject
    ObjectMapper objectMapper;

    @Override
    public Response toResponse(DynamoDbException exception) {
        LOGGER.error("Failed to process the Task on DynamoDB: " + exception.getMessage(), exception);
        String appCode = "ERR-004";
        ObjectNode exceptionJson = TaskExceptionUtil.getExceptionDetail(exception, this.objectMapper, appCode);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(exceptionJson)
                .build();
    }
}

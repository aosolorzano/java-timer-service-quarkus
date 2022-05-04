package com.hiperium.timer.service.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Objects;

/**
 * @author Andres Solorzano
 */
public final class TaskExceptionUtil {

    private TaskExceptionUtil() {}

    public static ObjectNode getExceptionDetail(Exception exception, ObjectMapper objectMapper, String code) {
        ObjectNode exceptionJson = objectMapper.createObjectNode();
        exceptionJson.put("exceptionType", exception.getClass().getName());
        exceptionJson.put("code", code);
        if (Objects.nonNull(exception.getMessage())) {
            exceptionJson.put("error", exception.getMessage());
        }
        return exceptionJson;
    }

}

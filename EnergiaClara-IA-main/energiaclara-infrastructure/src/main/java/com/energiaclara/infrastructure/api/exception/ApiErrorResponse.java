package com.energiaclara.infrastructure.api.exception;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        String errorCode,
        String message,
        Instant timestamp,
        String path,
        List<String> details,
        String correlationId
) {
    public static ApiErrorResponse of(String code, String message, String path, String correlationId) {
        return new ApiErrorResponse(code, message, Instant.now(), path, List.of(), correlationId);
    }

    public static ApiErrorResponse of(String code, String message, String path, List<String> details, String correlationId) {
        return new ApiErrorResponse(code, message, Instant.now(), path, details, correlationId);
    }
}

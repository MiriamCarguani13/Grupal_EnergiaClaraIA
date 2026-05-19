package com.energiaclara.infrastructure.api.exception;

public final class ErrorCodes {
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String AUTH_FAILED = "AUTH_FAILED";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String CONFLICT = "CONFLICT";
    public static final String RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED";
    public static final String DOMAIN_ERROR = "DOMAIN_ERROR";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    private ErrorCodes() {}
}

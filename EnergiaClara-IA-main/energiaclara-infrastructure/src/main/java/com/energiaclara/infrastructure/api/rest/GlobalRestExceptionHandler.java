package com.energiaclara.infrastructure.api.rest;

import com.energiaclara.core.domain.shared.DomainException;
import com.energiaclara.infrastructure.api.exception.ApiErrorResponse;
import com.energiaclara.infrastructure.api.exception.ErrorCodes;
import com.energiaclara.infrastructure.security.context.CorrelationIdContext;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalRestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalRestExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiErrorResponse> handleDomain(DomainException ex, HttpServletRequest req) {
        log.warn("DomainException: {}", ex.getMessage());
        HttpStatus status = resolveDomainStatus(ex);
        String code = status == HttpStatus.UNAUTHORIZED ? ErrorCodes.AUTH_FAILED : ErrorCodes.DOMAIN_ERROR;
        return body(status, code, ex.getMessage(), req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return body(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN, "Acceso denegado", req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        ApiErrorResponse body = ApiErrorResponse.of(
                ErrorCodes.VALIDATION_ERROR,
                "Errores de validación",
                req.getRequestURI(),
                details,
                CorrelationIdContext.get()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArg(IllegalArgumentException ex, HttpServletRequest req) {
        return body(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, ex.getMessage(), req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception", ex);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.INTERNAL_ERROR, "Error interno del servidor", req);
    }

    private HttpStatus resolveDomainStatus(DomainException ex) {
        String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
        if (msg.contains("credenciales") || msg.contains("token")) return HttpStatus.UNAUTHORIZED;
        if (msg.contains("autorizad") || msg.contains("denegado")) return HttpStatus.FORBIDDEN;
        if (msg.contains("no encontrado") || msg.contains("not found")) return HttpStatus.NOT_FOUND;
        if (msg.contains("ya registrado") || msg.contains("ya existe")) return HttpStatus.CONFLICT;
        return HttpStatus.BAD_REQUEST;
    }

    private ResponseEntity<ApiErrorResponse> body(HttpStatus status, String code, String message, HttpServletRequest req) {
        ApiErrorResponse body = ApiErrorResponse.of(code, message, req.getRequestURI(), CorrelationIdContext.get());
        return ResponseEntity.status(status).body(body);
    }
}

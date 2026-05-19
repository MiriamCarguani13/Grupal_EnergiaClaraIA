package com.energiaclara.infrastructure.config.ratelimit;

import com.energiaclara.infrastructure.api.exception.ApiErrorResponse;
import com.energiaclara.infrastructure.api.exception.ErrorCodes;
import com.energiaclara.infrastructure.security.context.CorrelationIdContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class InMemoryRateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitProperties props;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, Window> buckets = new ConcurrentHashMap<>();

    public InMemoryRateLimitingFilter(RateLimitProperties props, ObjectMapper objectMapper) {
        this.props = props;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!props.enabled() || !pathMatches(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        String clientId = clientId(request);
        String key = clientId + ":" + request.getRequestURI();
        long now = System.currentTimeMillis();
        long windowMs = props.windowSeconds() * 1000L;

        Window window = buckets.compute(key, (k, existing) -> {
            if (existing == null || now - existing.startMs >= windowMs) {
                return new Window(now, new AtomicInteger(0));
            }
            return existing;
        });

        int current = window.counter.incrementAndGet();
        long resetSec = (window.startMs + windowMs - now) / 1000L;

        response.setHeader("X-RateLimit-Limit", String.valueOf(props.maxRequests()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, props.maxRequests() - current)));
        response.setHeader("X-RateLimit-Reset", String.valueOf(Math.max(0, resetSec)));

        if (current > props.maxRequests()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ApiErrorResponse body = ApiErrorResponse.of(
                    ErrorCodes.RATE_LIMIT_EXCEEDED,
                    "Demasiadas solicitudes. Reintente más tarde.",
                    request.getRequestURI(),
                    CorrelationIdContext.get()
            );
            objectMapper.writeValue(response.getOutputStream(), body);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean pathMatches(String uri) {
        List<String> paths = props.paths();
        if (paths == null || paths.isEmpty()) return false;
        for (String p : paths) {
            if (uri.equals(p) || uri.startsWith(p)) return true;
        }
        return false;
    }

    private String clientId(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            int comma = xf.indexOf(',');
            return comma > 0 ? xf.substring(0, comma).trim() : xf.trim();
        }
        return request.getRemoteAddr();
    }

    private record Window(long startMs, AtomicInteger counter) {}
}

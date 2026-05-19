package com.energiaclara.infrastructure.config.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "iam.rate-limit")
public record RateLimitProperties(boolean enabled, int maxRequests, int windowSeconds, List<String> paths) {
}

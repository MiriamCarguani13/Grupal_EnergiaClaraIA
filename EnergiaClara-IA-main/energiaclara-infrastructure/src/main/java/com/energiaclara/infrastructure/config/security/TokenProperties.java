package com.energiaclara.infrastructure.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.token")
public record TokenProperties(String issuer, String secret, long accessTokenMinutes) {
}

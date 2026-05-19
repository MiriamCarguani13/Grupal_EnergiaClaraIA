package com.energiaclara.infrastructure.api.rest.dto;

import java.util.List;
import java.util.UUID;

public record AuthTokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UUID tenantId,
        UUID userId,
        String email,
        List<String> roles
) {
}

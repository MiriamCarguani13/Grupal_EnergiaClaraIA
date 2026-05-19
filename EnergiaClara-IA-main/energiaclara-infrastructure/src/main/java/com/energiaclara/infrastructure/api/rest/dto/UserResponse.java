package com.energiaclara.infrastructure.api.rest.dto;

import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID userId,
        UUID tenantId,
        String email,
        List<String> roles
) {
}

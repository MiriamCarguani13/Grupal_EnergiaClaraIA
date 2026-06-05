package com.energiaclara.core.security;

import java.util.UUID;

public record AuthenticatedUser(UUID userId, UUID tenantId, String email) {
}


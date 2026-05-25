package com.energiaclara.application.security;

import java.util.UUID;

public record AuthenticatedUser(UUID userId, UUID tenantId, String email) {
}

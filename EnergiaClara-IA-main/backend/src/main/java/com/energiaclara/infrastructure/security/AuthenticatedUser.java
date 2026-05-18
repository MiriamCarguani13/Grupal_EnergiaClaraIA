package com.energiaclara.infrastructure.security;

import java.util.UUID;

public record AuthenticatedUser(UUID userId, UUID tenantId, String email) {}

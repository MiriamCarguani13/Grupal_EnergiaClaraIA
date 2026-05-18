package com.energiaclara.core.domain.shared;

import java.time.Instant;
import java.util.Objects;

public record AuditInfo(UserId createdBy, Instant createdAt, UserId updatedBy, Instant updatedAt) {
    public AuditInfo {
        Objects.requireNonNull(createdBy, "El usuario creador no puede ser nulo");
        Objects.requireNonNull(createdAt, "La fecha de creación no puede ser nula");
    }

    public static AuditInfo created(UserId userId) {
        return new AuditInfo(userId, Instant.now(), null, null);
    }

    public AuditInfo markUpdated(UserId userId) {
        Objects.requireNonNull(userId, "El usuario actualizador no puede ser nulo");
        return new AuditInfo(createdBy, createdAt, userId, Instant.now());
    }
}

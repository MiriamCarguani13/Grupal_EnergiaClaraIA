package com.energiaclara.core.domain.energy;

import java.util.Objects;
import java.util.UUID;

public record AnomalyId(UUID value) {
    public AnomalyId {
        Objects.requireNonNull(value, "AnomalyId no puede ser nulo");
    }

    public static AnomalyId generate() {
        return new AnomalyId(UUID.randomUUID());
    }
}

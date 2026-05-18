package com.energiaclara.core.domain.energy;

import java.util.Objects;
import java.util.UUID;

public record EnergyReadingId(UUID value) {
    public EnergyReadingId {
        Objects.requireNonNull(value, "EnergyReadingId no puede ser nulo");
    }

    public static EnergyReadingId generate() {
        return new EnergyReadingId(UUID.randomUUID());
    }
}

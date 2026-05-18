package com.energiaclara.core.domain.shared;

import java.util.Objects;
import java.util.UUID;

public record FacilityId(UUID value) {
    public FacilityId {
        Objects.requireNonNull(value, "FacilityId no puede ser nulo");
    }

    public static FacilityId generate() {
        return new FacilityId(UUID.randomUUID());
    }

    public static FacilityId of(UUID value) {
        return new FacilityId(value);
    }

    public static FacilityId of(String value) {
        return new FacilityId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}

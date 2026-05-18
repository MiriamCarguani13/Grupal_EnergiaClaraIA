package com.energiaclara.core.domain.challenge;

import java.util.Objects;
import java.util.UUID;

public record EnergyChallengeId(UUID value) {
    public EnergyChallengeId {
        Objects.requireNonNull(value, "EnergyChallengeId no puede ser nulo");
    }

    public static EnergyChallengeId generate() {
        return new EnergyChallengeId(UUID.randomUUID());
    }
}

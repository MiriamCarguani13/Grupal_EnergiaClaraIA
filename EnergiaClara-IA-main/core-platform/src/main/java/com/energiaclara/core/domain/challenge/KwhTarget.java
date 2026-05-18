package com.energiaclara.core.domain.challenge;

import com.energiaclara.core.domain.energy.KwhValue;
import com.energiaclara.core.domain.shared.DomainException;

import java.util.Objects;

public final class KwhTarget {
    private final KwhValue value;

    public KwhTarget(KwhValue value) {
        this.value = Objects.requireNonNull(value, "KwhTarget no puede ser nulo");
        if (value.getValue() <= 0) {
            throw new DomainException("La meta de ahorro debe ser mayor a cero");
        }
    }

    public KwhValue getValue() {
        return value;
    }
}

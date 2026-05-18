package com.energiaclara.core.domain.shared;

import java.time.Instant;
import java.util.Objects;

public record DateRange(Instant from, Instant to) {
    public DateRange {
        Objects.requireNonNull(from, "La fecha inicial no puede ser nula");
        Objects.requireNonNull(to, "La fecha final no puede ser nula");
        if (to.isBefore(from)) {
            throw new DomainException("La fecha final no puede ser anterior a la inicial");
        }
    }

    public boolean contains(Instant instant) {
        Objects.requireNonNull(instant, "La fecha a evaluar no puede ser nula");
        return !instant.isBefore(from) && !instant.isAfter(to);
    }
}

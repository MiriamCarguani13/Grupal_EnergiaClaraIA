package com.energiaclara.ai.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record EnergyAiHistoricalReading(
        UUID tenantId,
        UUID meterId,
        Instant measuredAt,
        BigDecimal kwh
) {
    public EnergyAiHistoricalReading {
        Objects.requireNonNull(measuredAt, "measuredAt cannot be null");
        Objects.requireNonNull(kwh, "kwh cannot be null");
        if (kwh.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("kwh cannot be negative");
        }
    }
}


package com.energiaclara.ai.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record EnergyAiAnalysisCommand(
        UUID tenantId,
        UUID meterId,
        String facilityLabel,
        String meterLabel,
        Instant measuredAt,
        BigDecimal kwh,
        BigDecimal staticBaselineKwh,
        BigDecimal tolerancePercent,
        BigDecimal costPerKwh,
        BigDecimal co2KgPerKwh,
        BigDecimal voltage,
        BigDecimal powerFactor
) {
    public EnergyAiAnalysisCommand {
        Objects.requireNonNull(measuredAt, "measuredAt cannot be null");
        Objects.requireNonNull(kwh, "kwh cannot be null");
        if (kwh.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("kwh cannot be negative");
        }
    }
}


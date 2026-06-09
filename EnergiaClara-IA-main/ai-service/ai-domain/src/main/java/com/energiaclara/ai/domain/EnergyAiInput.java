package com.energiaclara.ai.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record EnergyAiInput(
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
    public EnergyAiInput {
        Objects.requireNonNull(measuredAt, "measuredAt cannot be null");
        Objects.requireNonNull(kwh, "kwh cannot be null");
        if (kwh.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("kwh cannot be negative");
        }
    }

    public BigDecimal effectiveTolerancePercent() {
        return positiveOrDefault(tolerancePercent, new BigDecimal("15.00"));
    }

    public BigDecimal effectiveCostPerKwh() {
        return nonNegativeOrDefault(costPerKwh, BigDecimal.ZERO);
    }

    public BigDecimal effectiveCo2KgPerKwh() {
        return nonNegativeOrDefault(co2KgPerKwh, BigDecimal.ZERO);
    }

    private static BigDecimal positiveOrDefault(BigDecimal value, BigDecimal fallback) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0 ? value : fallback;
    }

    private static BigDecimal nonNegativeOrDefault(BigDecimal value, BigDecimal fallback) {
        return value != null && value.compareTo(BigDecimal.ZERO) >= 0 ? value : fallback;
    }
}


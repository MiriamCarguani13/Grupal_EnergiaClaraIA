package com.energiaclara.application.energyops.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EnergyReadingHistoryResult(
        UUID id,
        String meterId,
        BigDecimal consumptionKwh,
        BigDecimal voltage,
        BigDecimal powerFactor,
        BigDecimal baselineKwh,
        BigDecimal deviationPercentage,
        String result,
        Instant createdAt
) {
}

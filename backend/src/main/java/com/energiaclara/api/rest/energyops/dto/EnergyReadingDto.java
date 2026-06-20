package com.energiaclara.api.rest.energyops.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EnergyReadingDto(
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

package com.energiaclara.application.energyops.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EnergyAiPredictionInput(
        UUID tenantId,
        UUID medidorId,
        String facilityId,
        String meterId,
        Instant measuredAt,
        BigDecimal kwh,
        BigDecimal voltage,
        BigDecimal powerFactor,
        BigDecimal baselineKwh,
        BigDecimal tolerancePercent
) {
}
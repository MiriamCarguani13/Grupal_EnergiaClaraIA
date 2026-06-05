package com.energiaclara.application.energyops.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EnergyAiAnalysisRequest(
        UUID tenantId,
        UUID medidorId,
        String facilityId,
        String meterId,
        Instant measuredAt,
        BigDecimal kwh,
        BigDecimal staticBaselineKwh,
        BigDecimal tolerancePercent,
        BigDecimal costPerKwh,
        BigDecimal co2KgPerKwh,
        BigDecimal voltage,
        BigDecimal powerFactor
) {
}


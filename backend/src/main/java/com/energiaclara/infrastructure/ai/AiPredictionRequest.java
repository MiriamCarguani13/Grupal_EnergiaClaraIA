package com.energiaclara.infrastructure.ai;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AiPredictionRequest(
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

package com.energiaclara.application.analytics.dto;

import com.energiaclara.domain.energyops.AnomalySeverity;
import com.energiaclara.domain.energyops.AnomalyType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AnomalyResult(
        UUID id,
        UUID readingId,
        String facilityId,
        String meterId,
        Instant measuredAt,
        AnomalyType type,
        AnomalySeverity severity,
        BigDecimal deviationPercent,
        String explanation,
        String recommendation,
        BigDecimal estimatedCostImpact,
        BigDecimal estimatedCo2Impact
) {
}

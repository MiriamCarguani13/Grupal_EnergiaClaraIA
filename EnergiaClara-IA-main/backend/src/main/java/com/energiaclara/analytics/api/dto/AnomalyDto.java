package com.energiaclara.analytics.api.dto;

import com.energiaclara.energyops.domain.AnomalySeverity;
import com.energiaclara.energyops.domain.AnomalyType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AnomalyDto(
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

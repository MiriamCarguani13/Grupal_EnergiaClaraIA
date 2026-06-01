package com.energiaclara.api.rest.analytics.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AnomalyDto(
        UUID id,
        UUID readingId,
        String facilityId,
        String meterId,
        Instant measuredAt,
        String type,
        String severity,
        BigDecimal deviationPercent,
        String explanation,
        String recommendation,
        BigDecimal estimatedCostImpact,
        BigDecimal estimatedCo2Impact,
        String estado,
        Instant resolvedAt,
        UUID resolvedBy
) {
}

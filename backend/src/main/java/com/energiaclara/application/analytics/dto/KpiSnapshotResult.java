package com.energiaclara.application.analytics.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record KpiSnapshotResult(
        UUID id,
        UUID readingId,
        String facilityId,
        String meterId,
        Instant measuredAt,
        BigDecimal kwh,
        BigDecimal baselineKwh,
        BigDecimal deviationPercent,
        boolean anomalyDetected,
        BigDecimal estimatedCostImpact,
        BigDecimal estimatedCo2Impact
) {
}

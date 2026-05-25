package com.energiaclara.application.energyops.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EnergyKpiSnapshotRecord(
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

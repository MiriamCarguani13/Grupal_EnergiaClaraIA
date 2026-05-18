package com.energiaclara.energyops.api.dto;

import com.energiaclara.energyops.domain.AnomalySeverity;

import java.math.BigDecimal;
import java.util.UUID;

public record AnalyzeReadingResponse(
        UUID readingId,
        UUID anomalyId,
        boolean anomalyDetected,
        AnomalySeverity severity,
        BigDecimal deviationPercent,
        String recommendation,
        BigDecimal estimatedCostImpact,
        BigDecimal estimatedCo2Impact
) {
}

package com.energiaclara.api.rest.energyops.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AnalyzeReadingResponse(
        UUID readingId,
        UUID anomalyId,
        boolean anomalyDetected,
        String severity,
        BigDecimal deviationPercent,
        String recommendation,
        BigDecimal estimatedCostImpact,
        BigDecimal estimatedCo2Impact
) {
}

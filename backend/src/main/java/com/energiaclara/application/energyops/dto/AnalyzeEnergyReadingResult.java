package com.energiaclara.application.energyops.dto;

import com.energiaclara.domain.energyops.AnomalySeverity;

import java.math.BigDecimal;
import java.util.UUID;

public record AnalyzeEnergyReadingResult(
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

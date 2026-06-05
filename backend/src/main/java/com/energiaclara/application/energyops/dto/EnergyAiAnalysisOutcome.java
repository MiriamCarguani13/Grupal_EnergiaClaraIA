package com.energiaclara.application.energyops.dto;

import com.energiaclara.domain.energyops.AnomalySeverity;

import java.math.BigDecimal;

public record EnergyAiAnalysisOutcome(
        boolean aiUsed,
        boolean fallback,
        boolean anomalyDetected,
        AnomalySeverity severity,
        BigDecimal deviationPercent,
        BigDecimal score,
        BigDecimal confidence,
        BigDecimal zScore,
        BigDecimal expectedKwh,
        int sampleCount,
        String baselineSource,
        BigDecimal estimatedCostImpact,
        BigDecimal estimatedCo2Impact,
        String explanation,
        String recommendation,
        String modelVersion
) {
}

package com.energiaclara.ai.application;

import com.energiaclara.ai.domain.DynamicBaseline;
import com.energiaclara.ai.domain.EnergyAiResult;
import com.energiaclara.ai.domain.HybridSeverity;

import java.math.BigDecimal;
import java.util.Objects;

public record EnergyAiAnalysisResponse(
        BigDecimal expectedKwh,
        BigDecimal movingAverageKwh,
        BigDecimal standardDeviationKwh,
        int sampleCount,
        DynamicBaseline.Source baselineSource,
        BigDecimal deviationPercent,
        BigDecimal zScore,
        BigDecimal excessKwh,
        BigDecimal anomalyScore,
        HybridSeverity severity,
        BigDecimal confidence,
        boolean anomalyDetected,
        boolean fallback,
        String explanation,
        String recommendation,
        BigDecimal estimatedCostImpact,
        BigDecimal estimatedCo2Impact,
        String modelVersion
) {
    public EnergyAiAnalysisResponse {
        Objects.requireNonNull(expectedKwh, "expectedKwh cannot be null");
        Objects.requireNonNull(baselineSource, "baselineSource cannot be null");
        Objects.requireNonNull(severity, "severity cannot be null");
        Objects.requireNonNull(explanation, "explanation cannot be null");
        Objects.requireNonNull(recommendation, "recommendation cannot be null");
        Objects.requireNonNull(modelVersion, "modelVersion cannot be null");
    }

    public static EnergyAiAnalysisResponse from(EnergyAiResult result) {
        Objects.requireNonNull(result, "result cannot be null");
        return new EnergyAiAnalysisResponse(
                result.baseline().expectedKwh(),
                result.baseline().movingAverageKwh(),
                result.baseline().standardDeviationKwh(),
                result.baseline().sampleCount(),
                result.baseline().source(),
                result.anomalyScore().deviationPercent(),
                result.anomalyScore().zScore(),
                result.anomalyScore().excessKwh(),
                result.anomalyScore().anomalyScore(),
                result.anomalyScore().severity(),
                result.anomalyScore().confidence(),
                result.anomalyScore().anomalyDetected(),
                result.fallback(),
                result.explainableRecommendation().explanation(),
                result.explainableRecommendation().recommendation(),
                result.anomalyScore().estimatedCostImpact(),
                result.anomalyScore().estimatedCo2Impact(),
                result.modelVersion()
        );
    }
}


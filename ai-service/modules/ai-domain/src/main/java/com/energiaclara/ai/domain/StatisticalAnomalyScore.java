package com.energiaclara.ai.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record StatisticalAnomalyScore(
        BigDecimal deviationPercent,
        BigDecimal zScore,
        BigDecimal excessKwh,
        BigDecimal anomalyScore,
        HybridSeverity severity,
        BigDecimal confidence,
        boolean anomalyDetected,
        BigDecimal estimatedCostImpact,
        BigDecimal estimatedCo2Impact
) {
    public StatisticalAnomalyScore {
        Objects.requireNonNull(deviationPercent, "deviationPercent cannot be null");
        Objects.requireNonNull(zScore, "zScore cannot be null");
        Objects.requireNonNull(excessKwh, "excessKwh cannot be null");
        Objects.requireNonNull(anomalyScore, "anomalyScore cannot be null");
        Objects.requireNonNull(severity, "severity cannot be null");
        Objects.requireNonNull(confidence, "confidence cannot be null");
        Objects.requireNonNull(estimatedCostImpact, "estimatedCostImpact cannot be null");
        Objects.requireNonNull(estimatedCo2Impact, "estimatedCo2Impact cannot be null");
    }
}


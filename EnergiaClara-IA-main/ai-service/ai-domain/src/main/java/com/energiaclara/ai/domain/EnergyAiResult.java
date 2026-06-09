package com.energiaclara.ai.domain;

import java.util.Objects;

public record EnergyAiResult(
        DynamicBaseline baseline,
        StatisticalAnomalyScore anomalyScore,
        ExplainableRecommendation explainableRecommendation,
        boolean fallback,
        String modelVersion
) {
    public EnergyAiResult {
        Objects.requireNonNull(baseline, "baseline cannot be null");
        Objects.requireNonNull(anomalyScore, "anomalyScore cannot be null");
        Objects.requireNonNull(explainableRecommendation, "explainableRecommendation cannot be null");
        Objects.requireNonNull(modelVersion, "modelVersion cannot be null");
    }
}


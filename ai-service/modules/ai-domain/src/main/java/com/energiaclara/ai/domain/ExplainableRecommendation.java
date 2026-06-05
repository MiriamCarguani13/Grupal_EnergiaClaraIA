package com.energiaclara.ai.domain;

import java.util.Objects;

public record ExplainableRecommendation(
        String explanation,
        String recommendation
) {
    public ExplainableRecommendation {
        Objects.requireNonNull(explanation, "explanation cannot be null");
        Objects.requireNonNull(recommendation, "recommendation cannot be null");
    }
}


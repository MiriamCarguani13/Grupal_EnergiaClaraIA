package com.energiaclara.infrastructure.ai;

import java.math.BigDecimal;

public record AiPredictionResponse(
        String modelVersion,
        BigDecimal predictedKwh,
        BigDecimal confidence,
        boolean anomalyDetected,
        BigDecimal anomalyScore,
        String explanation,
        String recommendation
) {
}

package com.energiaclara.application.energyops.dto;

import java.math.BigDecimal;

public record EnergyAiPredictionResult(
        String modelVersion,
        BigDecimal predictedKwh,
        BigDecimal confidence,
        boolean anomalyDetected,
        BigDecimal anomalyScore,
        String explanation,
        String recommendation,
        long latencyMs,
        String inputHash,
        String outputHash
) {
}

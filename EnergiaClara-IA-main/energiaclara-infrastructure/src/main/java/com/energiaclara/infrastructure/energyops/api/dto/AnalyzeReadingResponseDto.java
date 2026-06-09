package com.energiaclara.infrastructure.energyops.api.dto;

import com.energiaclara.ai.application.EnergyAiAnalysisResponse;

import java.util.UUID;

/**
 * Respuesta de {@code POST /api/energyops/analyze-reading}.
 * {@code anomalyId} es null cuando el motor no detectó anomalía (no se persiste nada).
 */
public record AnalyzeReadingResponseDto(
        UUID anomalyId,
        boolean anomalyDetected,
        EnergyAiAnalysisResponse analysis
) {
}

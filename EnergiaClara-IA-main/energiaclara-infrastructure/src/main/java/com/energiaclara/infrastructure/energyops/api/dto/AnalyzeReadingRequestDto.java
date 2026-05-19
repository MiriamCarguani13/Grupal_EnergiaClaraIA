package com.energiaclara.infrastructure.energyops.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;
import java.util.UUID;

/**
 * SKELETON. Request DTO endpoint {@code POST /api/energyops/analyze-reading}.
 * Orquesta register + detect.
 */
public record AnalyzeReadingRequestDto(
        @NotNull UUID facilityId,
        @NotNull UUID meterId,
        @PositiveOrZero double kwhValue,
        @NotNull Instant timestamp,
        String anomalyTypeHint
) {
}

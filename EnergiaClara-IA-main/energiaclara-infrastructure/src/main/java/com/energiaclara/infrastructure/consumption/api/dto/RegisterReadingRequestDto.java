package com.energiaclara.infrastructure.consumption.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;
import java.util.UUID;

/**
 * SKELETON. DTO HTTP para registrar lectura.
 * Maps al {@code RegisterEnergyReadingCommand} de application.
 */
public record RegisterReadingRequestDto(
        @NotNull UUID facilityId,
        @NotNull UUID meterId,
        @PositiveOrZero double kwhValue,
        @NotNull Instant timestamp
) {
}

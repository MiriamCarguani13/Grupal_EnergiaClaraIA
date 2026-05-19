package com.energiaclara.infrastructure.maintenance.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * SKELETON. Request DTO crear ticket de mantenimiento.
 */
public record CreateTicketRequestDto(
        @NotNull UUID facilityId,
        @NotNull UUID anomalyId,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 1000) String description,
        @NotBlank String severity,
        Double estimatedWastedKwh
) {
}

package com.energiaclara.energyops.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record AnalyzeReadingRequest(
        @NotBlank(message = "facilityId es obligatorio")
        String facilityId,
        @NotBlank(message = "meterId es obligatorio")
        String meterId,
        @NotNull(message = "measuredAt es obligatorio")
        Instant measuredAt,
        @NotNull(message = "kwh es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "kwh debe ser mayor a 0")
        BigDecimal kwh,
        BigDecimal voltage,
        BigDecimal powerFactor
) {
}

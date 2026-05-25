package com.energiaclara.application.energyops.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EnergyReadingRecord(
        UUID id,
        UUID tenantId,
        UUID medidorId,
        UUID registradaPor,
        String facilityId,
        String meterId,
        Instant measuredAt,
        Instant periodoInicio,
        BigDecimal kwh,
        BigDecimal voltage,
        BigDecimal powerFactor
) {
}

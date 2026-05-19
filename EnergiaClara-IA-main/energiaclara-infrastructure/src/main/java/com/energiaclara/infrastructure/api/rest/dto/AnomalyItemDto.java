package com.energiaclara.infrastructure.api.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AnomalyItemDto(
        UUID id,
        String facilityId,
        String meterId,
        String severity,
        String status,
        BigDecimal deviationPercent,
        Instant detectedAt
) {
}

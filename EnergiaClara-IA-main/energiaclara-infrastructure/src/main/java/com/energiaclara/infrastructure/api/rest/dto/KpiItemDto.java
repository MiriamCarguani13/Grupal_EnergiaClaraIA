package com.energiaclara.infrastructure.api.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record KpiItemDto(
        UUID id,
        Instant measuredAt,
        BigDecimal kwh,
        BigDecimal estimatedCostImpact,
        BigDecimal estimatedCo2Impact
) {
}

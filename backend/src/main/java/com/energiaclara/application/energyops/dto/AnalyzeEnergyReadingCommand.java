package com.energiaclara.application.energyops.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AnalyzeEnergyReadingCommand(
        String facilityId,
        String meterId,
        Instant measuredAt,
        BigDecimal kwh,
        BigDecimal voltage,
        BigDecimal powerFactor
) {
}

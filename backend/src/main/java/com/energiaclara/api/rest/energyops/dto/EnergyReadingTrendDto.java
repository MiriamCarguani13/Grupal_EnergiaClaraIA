package com.energiaclara.api.rest.energyops.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record EnergyReadingTrendDto(
        Instant date,
        BigDecimal consumptionKwh,
        BigDecimal baselineKwh,
        BigDecimal deviationPercentage,
        String result
) {
}

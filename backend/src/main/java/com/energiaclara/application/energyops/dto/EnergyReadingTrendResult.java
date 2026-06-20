package com.energiaclara.application.energyops.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record EnergyReadingTrendResult(
        Instant date,
        BigDecimal consumptionKwh,
        BigDecimal baselineKwh,
        BigDecimal deviationPercentage,
        String result
) {
}

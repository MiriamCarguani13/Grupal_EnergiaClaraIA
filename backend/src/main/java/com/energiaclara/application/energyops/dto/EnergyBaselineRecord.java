package com.energiaclara.application.energyops.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record EnergyBaselineRecord(
        UUID id,
        UUID medidorId,
        BigDecimal expectedKwh,
        BigDecimal tolerancePercent
) {
}

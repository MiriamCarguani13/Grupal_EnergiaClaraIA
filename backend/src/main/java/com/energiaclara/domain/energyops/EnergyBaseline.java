package com.energiaclara.domain.energyops;

import java.math.BigDecimal;
import java.util.Objects;

public record EnergyBaseline(BigDecimal expectedKwh, BigDecimal tolerancePercent) {
    public EnergyBaseline {
        Objects.requireNonNull(expectedKwh, "expectedKwh cannot be null");
        Objects.requireNonNull(tolerancePercent, "tolerancePercent cannot be null");
        if (expectedKwh.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Baseline invalido: expectedKwh debe ser mayor a 0");
        }
    }
}

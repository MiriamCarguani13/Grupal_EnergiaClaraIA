package com.energiaclara.domain.energyops;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class EnergyAnalysisPolicy {

    private EnergyAnalysisPolicy() {
    }

    public static BigDecimal calculateDeviationPercent(BigDecimal kwh, BigDecimal expectedKwh) {
        if (expectedKwh == null || expectedKwh.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Baseline invalido: expectedKwh debe ser mayor a 0");
        }
        return kwh.subtract(expectedKwh)
                .divide(expectedKwh, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public static BigDecimal calculateDeviationPercentOrZero(BigDecimal kwh, BigDecimal expectedKwh) {
        if (expectedKwh == null || expectedKwh.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return roundPercent(calculateDeviationPercent(kwh, expectedKwh));
    }

    public static boolean exceedsTolerance(BigDecimal deviationPercent, BigDecimal tolerancePercent) {
        return deviationPercent.compareTo(tolerancePercent) > 0;
    }

    public static BigDecimal excessKwh(BigDecimal kwh, BigDecimal expectedKwh) {
        return kwh.subtract(expectedKwh).max(BigDecimal.ZERO);
    }

    public static AnomalySeverity severityFor(BigDecimal deviationPercent) {
        if (deviationPercent.compareTo(BigDecimal.valueOf(100)) >= 0) {
            return AnomalySeverity.CRITICAL;
        }
        if (deviationPercent.compareTo(BigDecimal.valueOf(50)) >= 0) {
            return AnomalySeverity.HIGH;
        }
        if (deviationPercent.compareTo(BigDecimal.valueOf(25)) >= 0) {
            return AnomalySeverity.MEDIUM;
        }
        return AnomalySeverity.LOW;
    }

    public static BigDecimal scoreFor(AnomalySeverity severity) {
        return switch (severity) {
            case CRITICAL -> new BigDecimal("0.9500");
            case HIGH -> new BigDecimal("0.7500");
            case MEDIUM -> new BigDecimal("0.5000");
            case LOW -> new BigDecimal("0.2500");
        };
    }

    public static BigDecimal roundPercent(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal roundImpact(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}

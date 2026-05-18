package com.energiaclara.core.domain.energy;

public class AnomalyPolicy {
    public static boolean isAnomaly(EnergyReading reading, EnergyBaseline baseline) {
        KwhValue expected = baseline.getExpectedFor(reading.getFacilityId(), reading.getTimestamp());
        KwhValue delta = reading.getKwhValue().subtract(expected);
        return delta.isGreaterThan(baseline.getToleranceThreshold());
    }

    public static AnomalySeverity calculateSeverity(KwhValue delta, EnergyBaseline baseline) {
        double threshold = baseline.getToleranceThreshold().getValue();
        if (threshold == 0) {
            return AnomalySeverity.CRITICAL;
        }
        double ratio = delta.getValue() / threshold;
        if (ratio > 3.0) return AnomalySeverity.CRITICAL;
        if (ratio > 1.5) return AnomalySeverity.WARNING;
        return AnomalySeverity.LOW;
    }
}

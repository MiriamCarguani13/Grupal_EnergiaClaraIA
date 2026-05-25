package com.energiaclara.application.analytics.dto;

import java.math.BigDecimal;
import java.util.List;

public record AnalyticsDashboardResult(
        long totalReadings,
        long totalAnomalies,
        BigDecimal latestKwh,
        BigDecimal latestDeviationPercent,
        boolean latestAnomalyDetected,
        List<KpiSnapshotResult> kpis,
        List<AnomalyResult> anomalies
) {
}

package com.energiaclara.analytics.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        long totalReadings,
        long totalAnomalies,
        BigDecimal latestKwh,
        BigDecimal latestDeviationPercent,
        boolean latestAnomalyDetected,
        List<KpiSnapshotDto> kpis,
        List<AnomalyDto> anomalies
) {
}

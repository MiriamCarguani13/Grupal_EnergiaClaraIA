package com.energiaclara.application.analytics.dto;

public record AnalyticsDashboardMetricsResult(
        long totalReadings,
        long totalAnomalies
) {
}

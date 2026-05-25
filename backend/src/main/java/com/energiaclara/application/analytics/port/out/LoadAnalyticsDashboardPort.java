package com.energiaclara.application.analytics.port.out;

import com.energiaclara.application.analytics.dto.AnalyticsDashboardMetricsResult;

public interface LoadAnalyticsDashboardPort {
    AnalyticsDashboardMetricsResult loadDashboardMetrics();
}

package com.energiaclara.analytics.api;

import com.energiaclara.analytics.api.dto.AnomalyDto;
import com.energiaclara.analytics.api.dto.DashboardResponse;
import com.energiaclara.analytics.api.dto.KpiSnapshotDto;
import com.energiaclara.analytics.application.AnalyticsQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsQueryService analyticsQueryService;

    public AnalyticsController(AnalyticsQueryService analyticsQueryService) {
        this.analyticsQueryService = analyticsQueryService;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return analyticsQueryService.dashboard();
    }

    @GetMapping("/kpis")
    public List<KpiSnapshotDto> kpis() {
        return analyticsQueryService.kpis();
    }

    @GetMapping("/anomalies")
    public List<AnomalyDto> anomalies() {
        return analyticsQueryService.anomalies();
    }
}

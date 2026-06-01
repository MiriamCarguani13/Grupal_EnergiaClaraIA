package com.energiaclara.api.rest.analytics;

import com.energiaclara.api.rest.analytics.dto.AnomalyDto;
import com.energiaclara.api.rest.analytics.dto.DashboardResponse;
import com.energiaclara.api.rest.analytics.dto.KpiSnapshotDto;
import com.energiaclara.application.analytics.dto.AnalyticsDashboardResult;
import com.energiaclara.application.analytics.dto.AnomalyResult;
import com.energiaclara.application.analytics.dto.KpiSnapshotResult;
import com.energiaclara.application.port.in.GetAnalyticsDashboardUseCase;
import com.energiaclara.application.port.in.GetAnomaliesUseCase;
import com.energiaclara.application.port.in.GetKpiSnapshotsUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final GetAnalyticsDashboardUseCase getAnalyticsDashboardUseCase;
    private final GetKpiSnapshotsUseCase getKpiSnapshotsUseCase;
    private final GetAnomaliesUseCase getAnomaliesUseCase;

    public AnalyticsController(GetAnalyticsDashboardUseCase getAnalyticsDashboardUseCase,
                               GetKpiSnapshotsUseCase getKpiSnapshotsUseCase,
                               GetAnomaliesUseCase getAnomaliesUseCase) {
        this.getAnalyticsDashboardUseCase = getAnalyticsDashboardUseCase;
        this.getKpiSnapshotsUseCase = getKpiSnapshotsUseCase;
        this.getAnomaliesUseCase = getAnomaliesUseCase;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return toResponse(getAnalyticsDashboardUseCase.dashboard());
    }

    @GetMapping("/kpis")
    public List<KpiSnapshotDto> kpis() {
        return getKpiSnapshotsUseCase.kpis().stream()
                .map(this::toDto)
                .toList();
    }

    @GetMapping("/anomalies")
    public List<AnomalyDto> anomalies() {
        return getAnomaliesUseCase.anomalies().stream()
                .map(this::toDto)
                .toList();
    }

    private DashboardResponse toResponse(AnalyticsDashboardResult result) {
        return new DashboardResponse(
                result.totalReadings(),
                result.totalAnomalies(),
                result.latestKwh(),
                result.latestDeviationPercent(),
                result.latestAnomalyDetected(),
                result.kpis().stream().map(this::toDto).toList(),
                result.anomalies().stream().map(this::toDto).toList()
        );
    }

    private KpiSnapshotDto toDto(KpiSnapshotResult result) {
        return new KpiSnapshotDto(
                result.id(),
                result.readingId(),
                result.facilityId(),
                result.meterId(),
                result.measuredAt(),
                result.kwh(),
                result.baselineKwh(),
                result.deviationPercent(),
                result.anomalyDetected(),
                result.estimatedCostImpact(),
                result.estimatedCo2Impact()
        );
    }

    private AnomalyDto toDto(AnomalyResult result) {
        return new AnomalyDto(
                result.id(),
                result.readingId(),
                result.facilityId(),
                result.meterId(),
                result.measuredAt(),
                result.type().name(),
                result.severity().name(),
                result.deviationPercent(),
                result.explanation(),
                result.recommendation(),
                result.estimatedCostImpact(),
                result.estimatedCo2Impact(),
                result.estado(),
                result.resolvedAt(),
                result.resolvedBy()
        );
    }
}

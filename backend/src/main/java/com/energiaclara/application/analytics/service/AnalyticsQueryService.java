package com.energiaclara.application.analytics.service;

import com.energiaclara.application.analytics.dto.AnalyticsDashboardMetricsResult;
import com.energiaclara.application.analytics.dto.AnalyticsDashboardResult;
import com.energiaclara.application.analytics.dto.AnomalyResult;
import com.energiaclara.application.analytics.dto.KpiSnapshotResult;
import com.energiaclara.application.port.in.GetAnalyticsDashboardUseCase;
import com.energiaclara.application.port.in.GetAnomaliesUseCase;
import com.energiaclara.application.port.in.GetKpiSnapshotsUseCase;
import com.energiaclara.application.port.out.LoadAnalyticsDashboardPort;
import com.energiaclara.application.port.out.LoadAnomaliesPort;
import com.energiaclara.application.port.out.LoadKpiSnapshotsPort;
import com.energiaclara.application.energyops.dto.EnergyAnomalyRecord;
import com.energiaclara.application.energyops.dto.EnergyBaselineRecord;
import com.energiaclara.application.energyops.dto.EnergyReadingRecord;
import com.energiaclara.application.port.out.FindEnergyBaselinePort;
import com.energiaclara.domain.energyops.EnergyAnalysisPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AnalyticsQueryService implements GetAnalyticsDashboardUseCase, GetKpiSnapshotsUseCase, GetAnomaliesUseCase {

    private final LoadAnalyticsDashboardPort loadAnalyticsDashboardPort;
    private final LoadKpiSnapshotsPort loadKpiSnapshotsPort;
    private final LoadAnomaliesPort loadAnomaliesPort;
    private final FindEnergyBaselinePort findEnergyBaselinePort;
    private final UUID demoMedidorId;
    private final BigDecimal demoDefaultBaselineKwh;
    private final BigDecimal costPerKwh;
    private final BigDecimal co2KgPerKwh;

    public AnalyticsQueryService(
            LoadAnalyticsDashboardPort loadAnalyticsDashboardPort,
            LoadKpiSnapshotsPort loadKpiSnapshotsPort,
            LoadAnomaliesPort loadAnomaliesPort,
            FindEnergyBaselinePort findEnergyBaselinePort,
            @Value("${app.energyops.demo-medidor-id:33333333-3333-3333-3333-333333333333}") UUID demoMedidorId,
            @Value("${app.energyops.demo-default-baseline-kwh:100}") BigDecimal demoDefaultBaselineKwh,
            @Value("${app.energyops.cost-per-kwh:1.50625}") BigDecimal costPerKwh,
            @Value("${app.energyops.co2-kg-per-kwh:0.44}") BigDecimal co2KgPerKwh
    ) {
        this.loadAnalyticsDashboardPort = loadAnalyticsDashboardPort;
        this.loadKpiSnapshotsPort = loadKpiSnapshotsPort;
        this.loadAnomaliesPort = loadAnomaliesPort;
        this.findEnergyBaselinePort = findEnergyBaselinePort;
        this.demoMedidorId = demoMedidorId;
        this.demoDefaultBaselineKwh = demoDefaultBaselineKwh;
        this.costPerKwh = costPerKwh;
        this.co2KgPerKwh = co2KgPerKwh;
    }

    @Transactional(readOnly = true)
    @Override
    public AnalyticsDashboardResult dashboard() {
        List<KpiSnapshotResult> kpis = kpis();
        List<AnomalyResult> anomalies = anomalies();
        KpiSnapshotResult latest = kpis.isEmpty() ? null : kpis.get(0);
        AnalyticsDashboardMetricsResult metrics = loadAnalyticsDashboardPort.loadDashboardMetrics();
        return new AnalyticsDashboardResult(
                metrics.totalReadings(),
                metrics.totalAnomalies(),
                latest == null ? BigDecimal.ZERO : latest.kwh(),
                latest == null ? BigDecimal.ZERO : latest.deviationPercent(),
                latest != null && latest.anomalyDetected(),
                kpis,
                anomalies
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<KpiSnapshotResult> kpis() {
        BigDecimal baselineKwh = resolveBaselineKwh();
        Map<UUID, EnergyAnomalyRecord> anomaliesByReading = indexAnomaliesByReading();

        return loadKpiSnapshotsPort.loadRecentReadings().stream()
                .map(reading -> toKpiDto(reading, baselineKwh, anomaliesByReading.get(reading.id())))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<AnomalyResult> anomalies() {
        return loadAnomaliesPort.loadRecentAnomalies().stream()
                .map(this::toAnomalyDto)
                .toList();
    }

    private BigDecimal resolveBaselineKwh() {
        return findEnergyBaselinePort.findActiveByMedidorId(demoMedidorId)
                .map(EnergyBaselineRecord::expectedKwh)
                .orElse(demoDefaultBaselineKwh);
    }

    private Map<UUID, EnergyAnomalyRecord> indexAnomaliesByReading() {
        Map<UUID, EnergyAnomalyRecord> map = new HashMap<>();
        for (EnergyAnomalyRecord a : loadAnomaliesPort.loadRecentAnomalies()) {
            if (a.readingId() != null) {
                map.putIfAbsent(a.readingId(), a);
            }
        }
        return map;
    }

    private KpiSnapshotResult toKpiDto(EnergyReadingRecord reading, BigDecimal baselineKwh, EnergyAnomalyRecord matched) {
        BigDecimal deviation = EnergyAnalysisPolicy.calculateDeviationPercentOrZero(reading.kwh(), baselineKwh);
        BigDecimal excess = EnergyAnalysisPolicy.excessKwh(reading.kwh(), baselineKwh);
        BigDecimal cost = Optional.ofNullable(matched).map(EnergyAnomalyRecord::estimatedCostImpact)
                .orElse(EnergyAnalysisPolicy.roundImpact(excess.multiply(costPerKwh)));
        BigDecimal co2 = Optional.ofNullable(matched).map(EnergyAnomalyRecord::estimatedCo2Impact)
                .orElse(EnergyAnalysisPolicy.roundImpact(excess.multiply(co2KgPerKwh)));

        return new KpiSnapshotResult(
                reading.id(),
                reading.id(),
                reading.facilityId(),
                reading.meterId(),
                reading.measuredAt(),
                reading.kwh(),
                baselineKwh,
                deviation,
                matched != null,
                cost,
                co2
        );
    }

    private AnomalyResult toAnomalyDto(EnergyAnomalyRecord entity) {
        return new AnomalyResult(
                entity.id(),
                entity.readingId(),
                entity.facilityId(),
                entity.meterId(),
                entity.measuredAt(),
                entity.type(),
                entity.severity(),
                entity.deviationPercent(),
                entity.explanation(),
                entity.recommendation(),
                entity.estimatedCostImpact(),
                entity.estimatedCo2Impact(),
                entity.estado(),
                entity.resolvedAt(),
                entity.resolvedBy()
        );
    }
}

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AnalyticsQueryService implements GetAnalyticsDashboardUseCase, GetKpiSnapshotsUseCase, GetAnomaliesUseCase {

    private static final Pattern Z_SCORE_PATTERN = Pattern.compile("zScore=([+-]?\\d+(?:\\.\\d+)?)");
    private static final Pattern EXPECTED_KWH_PATTERN = Pattern.compile("(?:expectedKwh|esperado)=([+-]?\\d+(?:\\.\\d+)?)");
    private static final Pattern SAMPLE_COUNT_PATTERN = Pattern.compile("(?:sampleCount=(\\d+)|con (\\d+) lecturas historicas)");
    private static final Pattern CONFIDENCE_PATTERN = Pattern.compile("confidence=([+-]?\\d+(?:\\.\\d+)?)");
    private static final Pattern BASELINE_SOURCE_PATTERN = Pattern.compile("baselineSource=([A-Z_]+)");
    private static final Pattern MODEL_VERSION_PATTERN = Pattern.compile("modelVersion=([A-Za-z0-9._-]+)");

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
        String explanation = entity.explanation();
        return new AnomalyResult(
                entity.id(),
                entity.readingId(),
                entity.facilityId(),
                entity.meterId(),
                entity.measuredAt(),
                entity.type(),
                entity.severity(),
                entity.deviationPercent(),
                extractDecimal(CONFIDENCE_PATTERN, explanation),
                extractDecimal(Z_SCORE_PATTERN, explanation),
                extractDecimal(EXPECTED_KWH_PATTERN, explanation),
                extractSampleCount(explanation),
                extractBaselineSource(explanation),
                firstNonBlank(entity.modelVersion(), extractText(MODEL_VERSION_PATTERN, explanation)),
                explanation,
                entity.recommendation(),
                entity.estimatedCostImpact(),
                entity.estimatedCo2Impact(),
                entity.estado(),
                entity.ticketId(),
                entity.responsibleTechnicianId(),
                entity.responsibleTechnicianName(),
                entity.resolvedAt()
        );
    }

    private static BigDecimal extractDecimal(Pattern pattern, String text) {
        String value = extractText(pattern, text);
        if (value == null) {
            return null;
        }
        return new BigDecimal(value);
    }

    private static Integer extractSampleCount(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        Matcher matcher = SAMPLE_COUNT_PATTERN.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        String explicit = matcher.group(1);
        String fromExplanation = matcher.group(2);
        return Integer.valueOf(explicit != null ? explicit : fromExplanation);
    }

    private static String extractBaselineSource(String text) {
        String explicit = extractText(BASELINE_SOURCE_PATTERN, text);
        if (explicit != null) {
            return explicit;
        }
        if (text == null) {
            return null;
        }
        String normalized = text.toLowerCase();
        if (normalized.contains("baseline dinamico")) {
            return "HISTORY";
        }
        if (normalized.contains("baseline fijo")) {
            return "STATIC_BASELINE";
        }
        if (normalized.contains("lectura actual como referencia")) {
            return "INPUT_AS_EXPECTED";
        }
        return null;
    }

    private static String extractText(Pattern pattern, String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String firstNonBlank(String primary, String fallback) {
        return primary == null || primary.isBlank() ? fallback : primary;
    }
}

package com.energiaclara.analytics.application;

import com.energiaclara.analytics.api.dto.AnomalyDto;
import com.energiaclara.analytics.api.dto.DashboardResponse;
import com.energiaclara.analytics.api.dto.KpiSnapshotDto;
import com.energiaclara.energyops.infrastructure.persistence.EnergyAnomalyEntity;
import com.energiaclara.energyops.infrastructure.persistence.EnergyAnomalyRepository;
import com.energiaclara.energyops.infrastructure.persistence.EnergyBaselineEntity;
import com.energiaclara.energyops.infrastructure.persistence.EnergyBaselineRepository;
import com.energiaclara.energyops.infrastructure.persistence.EnergyReadingEntity;
import com.energiaclara.energyops.infrastructure.persistence.EnergyReadingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AnalyticsQueryService {

    private final EnergyReadingRepository readingRepository;
    private final EnergyAnomalyRepository anomalyRepository;
    private final EnergyBaselineRepository baselineRepository;
    private final UUID demoMedidorId;
    private final BigDecimal demoDefaultBaselineKwh;
    private final BigDecimal costPerKwh;
    private final BigDecimal co2KgPerKwh;

    public AnalyticsQueryService(
            EnergyReadingRepository readingRepository,
            EnergyAnomalyRepository anomalyRepository,
            EnergyBaselineRepository baselineRepository,
            @Value("${app.energyops.demo-medidor-id:33333333-3333-3333-3333-333333333333}") UUID demoMedidorId,
            @Value("${app.energyops.demo-default-baseline-kwh:100}") BigDecimal demoDefaultBaselineKwh,
            @Value("${app.energyops.cost-per-kwh:1.50625}") BigDecimal costPerKwh,
            @Value("${app.energyops.co2-kg-per-kwh:0.44}") BigDecimal co2KgPerKwh
    ) {
        this.readingRepository = readingRepository;
        this.anomalyRepository = anomalyRepository;
        this.baselineRepository = baselineRepository;
        this.demoMedidorId = demoMedidorId;
        this.demoDefaultBaselineKwh = demoDefaultBaselineKwh;
        this.costPerKwh = costPerKwh;
        this.co2KgPerKwh = co2KgPerKwh;
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        List<KpiSnapshotDto> kpis = kpis();
        List<AnomalyDto> anomalies = anomalies();
        KpiSnapshotDto latest = kpis.isEmpty() ? null : kpis.get(0);
        return new DashboardResponse(
                readingRepository.count(),
                anomalyRepository.count(),
                latest == null ? BigDecimal.ZERO : latest.kwh(),
                latest == null ? BigDecimal.ZERO : latest.deviationPercent(),
                latest != null && latest.anomalyDetected(),
                kpis,
                anomalies
        );
    }

    @Transactional(readOnly = true)
    public List<KpiSnapshotDto> kpis() {
        BigDecimal baselineKwh = resolveBaselineKwh();
        Map<UUID, EnergyAnomalyEntity> anomaliesByReading = indexAnomaliesByReading();

        return readingRepository.findTop20ByOrderByMeasuredAtDesc().stream()
                .map(reading -> toKpiDto(reading, baselineKwh, anomaliesByReading.get(reading.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AnomalyDto> anomalies() {
        return anomalyRepository.findTop20ByOrderByMeasuredAtDesc().stream()
                .map(this::toAnomalyDto)
                .toList();
    }

    private BigDecimal resolveBaselineKwh() {
        return baselineRepository.findFirstByMedidorIdAndActiveTrue(demoMedidorId)
                .map(EnergyBaselineEntity::getExpectedKwh)
                .orElse(demoDefaultBaselineKwh);
    }

    private Map<UUID, EnergyAnomalyEntity> indexAnomaliesByReading() {
        Map<UUID, EnergyAnomalyEntity> map = new HashMap<>();
        for (EnergyAnomalyEntity a : anomalyRepository.findTop20ByOrderByMeasuredAtDesc()) {
            if (a.getReadingId() != null) {
                map.putIfAbsent(a.getReadingId(), a);
            }
        }
        return map;
    }

    private KpiSnapshotDto toKpiDto(EnergyReadingEntity reading, BigDecimal baselineKwh, EnergyAnomalyEntity matched) {
        BigDecimal deviation = computeDeviationPercent(reading.getKwh(), baselineKwh);
        BigDecimal excess = reading.getKwh().subtract(baselineKwh).max(BigDecimal.ZERO);
        BigDecimal cost = Optional.ofNullable(matched).map(EnergyAnomalyEntity::getEstimatedCostImpact)
                .orElse(money(excess.multiply(costPerKwh)));
        BigDecimal co2 = Optional.ofNullable(matched).map(EnergyAnomalyEntity::getEstimatedCo2Impact)
                .orElse(money(excess.multiply(co2KgPerKwh)));

        return new KpiSnapshotDto(
                reading.getId(),
                reading.getId(),
                reading.getFacilityId(),
                reading.getMeterId(),
                reading.getMeasuredAt(),
                reading.getKwh(),
                baselineKwh,
                deviation,
                matched != null,
                cost,
                co2
        );
    }

    private AnomalyDto toAnomalyDto(EnergyAnomalyEntity entity) {
        return new AnomalyDto(
                entity.getId(),
                entity.getReadingId(),
                entity.getFacilityId(),
                entity.getMeterId(),
                entity.getMeasuredAt(),
                entity.getType(),
                entity.getSeverity(),
                entity.getDeviationPercent(),
                entity.getExplanation(),
                entity.getRecommendation(),
                entity.getEstimatedCostImpact(),
                entity.getEstimatedCo2Impact()
        );
    }

    private BigDecimal computeDeviationPercent(BigDecimal kwh, BigDecimal baselineKwh) {
        if (baselineKwh == null || baselineKwh.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return kwh.subtract(baselineKwh)
                .divide(baselineKwh, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}

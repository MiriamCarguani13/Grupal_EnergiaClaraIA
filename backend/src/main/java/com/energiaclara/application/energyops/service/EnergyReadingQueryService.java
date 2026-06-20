package com.energiaclara.application.energyops.service;

import com.energiaclara.application.energyops.dto.EnergyAnomalyRecord;
import com.energiaclara.application.energyops.dto.EnergyBaselineRecord;
import com.energiaclara.application.energyops.dto.EnergyReadingHistoryResult;
import com.energiaclara.application.energyops.dto.EnergyReadingRecord;
import com.energiaclara.application.energyops.dto.EnergyReadingTrendResult;
import com.energiaclara.application.port.in.GetEnergyReadingsUseCase;
import com.energiaclara.application.port.out.FindEnergyBaselinePort;
import com.energiaclara.application.port.out.LoadAnomaliesPort;
import com.energiaclara.application.port.out.LoadEnergyReadingsPort;
import com.energiaclara.domain.energyops.EnergyAnalysisPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class EnergyReadingQueryService implements GetEnergyReadingsUseCase {

    private static final int TREND_BASELINE_WINDOW_SIZE = 7;

    private final LoadEnergyReadingsPort loadEnergyReadingsPort;
    private final LoadAnomaliesPort loadAnomaliesPort;
    private final FindEnergyBaselinePort findEnergyBaselinePort;
    private final BigDecimal defaultBaselineKwh;

    public EnergyReadingQueryService(
            LoadEnergyReadingsPort loadEnergyReadingsPort,
            LoadAnomaliesPort loadAnomaliesPort,
            FindEnergyBaselinePort findEnergyBaselinePort,
            @Value("${app.energyops.demo-default-baseline-kwh:100}") BigDecimal defaultBaselineKwh
    ) {
        this.loadEnergyReadingsPort = loadEnergyReadingsPort;
        this.loadAnomaliesPort = loadAnomaliesPort;
        this.findEnergyBaselinePort = findEnergyBaselinePort;
        this.defaultBaselineKwh = defaultBaselineKwh;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnergyReadingHistoryResult> readings() {
        return calculateHistoricalReadings().stream()
                .sorted(Comparator.comparing(EnergyReadingHistoryResult::createdAt).reversed())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnergyReadingTrendResult> trends() {
        return calculateHistoricalReadings().stream()
                .map(reading -> new EnergyReadingTrendResult(
                        reading.createdAt(),
                        reading.consumptionKwh(),
                        reading.baselineKwh(),
                        reading.deviationPercentage(),
                        reading.result()
                ))
                .toList();
    }

    private List<EnergyReadingHistoryResult> calculateHistoricalReadings() {
        List<EnergyReadingRecord> chronologicalReadings = loadEnergyReadingsPort.loadRecentEnergyReadings().stream()
                .sorted(Comparator.comparing(EnergyReadingRecord::measuredAt))
                .toList();
        Map<UUID, EnergyAnomalyRecord> anomaliesByReading = anomaliesByReadingId(chronologicalReadings);
        Map<UUID, BigDecimal> staticBaselineByMeter = new HashMap<>();
        Map<UUID, List<BigDecimal>> previousKwhByMeter = new HashMap<>();
        List<EnergyReadingHistoryResult> results = new ArrayList<>();

        for (EnergyReadingRecord reading : chronologicalReadings) {
            List<BigDecimal> previousKwh = previousKwhByMeter.computeIfAbsent(reading.medidorId(), ignored -> new ArrayList<>());
            BigDecimal baseline = movingAverageOrStaticBaseline(
                    previousKwh,
                    staticBaselineByMeter.computeIfAbsent(reading.medidorId(), this::baselineFor)
            );

            results.add(toHistory(reading, anomaliesByReading.get(reading.id()), baseline));
            previousKwh.add(reading.kwh());
        }

        return results;
    }

    private EnergyReadingHistoryResult toHistory(
            EnergyReadingRecord reading,
            EnergyAnomalyRecord anomaly,
            BigDecimal baseline
    ) {
        BigDecimal deviation = EnergyAnalysisPolicy.calculateDeviationPercentOrZero(reading.kwh(), baseline);
        String result = anomaly == null || anomaly.severity() == null ? "NORMAL" : anomaly.severity().name();

        return new EnergyReadingHistoryResult(
                reading.id(),
                reading.meterId(),
                reading.kwh(),
                reading.voltage(),
                reading.powerFactor(),
                baseline,
                deviation,
                result,
                reading.measuredAt()
        );
    }

    private BigDecimal movingAverageOrStaticBaseline(List<BigDecimal> previousKwh, BigDecimal staticBaseline) {
        if (previousKwh.isEmpty()) {
            return staticBaseline;
        }
        int fromIndex = Math.max(0, previousKwh.size() - TREND_BASELINE_WINDOW_SIZE);
        List<BigDecimal> window = previousKwh.subList(fromIndex, previousKwh.size());
        BigDecimal sum = window.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(window.size()), 6, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private Map<UUID, EnergyAnomalyRecord> anomaliesByReadingId(List<EnergyReadingRecord> readings) {
        List<UUID> readingIds = readings.stream()
                .map(EnergyReadingRecord::id)
                .toList();
        Map<UUID, EnergyAnomalyRecord> anomalies = new HashMap<>();
        for (EnergyAnomalyRecord anomaly : loadAnomaliesPort.loadAnomaliesByReadingIds(readingIds)) {
            if (anomaly.readingId() != null) {
                anomalies.putIfAbsent(anomaly.readingId(), anomaly);
            }
        }
        return anomalies;
    }

    private BigDecimal baselineFor(UUID medidorId) {
        return findEnergyBaselinePort.findActiveByMedidorId(medidorId)
                .map(EnergyBaselineRecord::expectedKwh)
                .orElse(defaultBaselineKwh);
    }
}

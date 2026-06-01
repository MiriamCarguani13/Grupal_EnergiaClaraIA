package com.energiaclara.application.energyops.service;

import com.energiaclara.application.energyops.dto.AnalyzeEnergyReadingCommand;
import com.energiaclara.application.energyops.dto.AnalyzeEnergyReadingResult;
import com.energiaclara.application.energyops.dto.EnergyAnomalyRecord;
import com.energiaclara.application.energyops.dto.EnergyBaselineRecord;
import com.energiaclara.application.energyops.dto.EnergyReadingRecord;
import com.energiaclara.application.energyops.dto.EnergyAiPredictionInput;
import com.energiaclara.application.energyops.dto.EnergyAiPredictionResult;
import com.energiaclara.application.port.in.AnalyzeEnergyReadingUseCase;
import com.energiaclara.application.port.out.FindEnergyBaselinePort;
import com.energiaclara.application.port.out.SaveEnergyAnomalyPort;
import com.energiaclara.application.port.out.SaveEnergyReadingPort;
import com.energiaclara.application.port.out.EnergyAiPredictionPort;
import com.energiaclara.domain.energyops.AnomalySeverity;
import com.energiaclara.domain.energyops.AnomalyType;
import com.energiaclara.domain.energyops.EnergyAnalysisPolicy;
import com.energiaclara.domain.energyops.EnergyBaseline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class EnergyAnalysisService implements AnalyzeEnergyReadingUseCase {

    private static final Logger log = LoggerFactory.getLogger(EnergyAnalysisService.class);

    private final SaveEnergyReadingPort saveEnergyReadingPort;
    private final FindEnergyBaselinePort findEnergyBaselinePort;
    private final SaveEnergyAnomalyPort saveEnergyAnomalyPort;
    private final EnergyAiPredictionPort energyAiPredictionPort;
    private final UUID demoTenantId;
    private final UUID demoMedidorId;
    private final UUID demoUserId;
    private final BigDecimal demoDefaultBaselineKwh;
    private final BigDecimal demoDefaultTolerancePercent;
    private final BigDecimal costPerKwh;
    private final BigDecimal co2KgPerKwh;

    public EnergyAnalysisService(
            SaveEnergyReadingPort saveEnergyReadingPort,
            FindEnergyBaselinePort findEnergyBaselinePort,
            SaveEnergyAnomalyPort saveEnergyAnomalyPort,
            EnergyAiPredictionPort energyAiPredictionPort,
            @Value("${app.energyops.demo-tenant-id:11111111-1111-1111-1111-111111111111}") UUID demoTenantId,
            @Value("${app.energyops.demo-medidor-id:33333333-3333-3333-3333-333333333333}") UUID demoMedidorId,
            @Value("${app.energyops.demo-user-id:44444444-4444-4444-4444-444444444444}") UUID demoUserId,
            @Value("${app.energyops.demo-default-baseline-kwh:100}") BigDecimal demoDefaultBaselineKwh,
            @Value("${app.energyops.demo-default-tolerance-percent:15}") BigDecimal demoDefaultTolerancePercent,
            @Value("${app.energyops.cost-per-kwh:1.50625}") BigDecimal costPerKwh,
            @Value("${app.energyops.co2-kg-per-kwh:0.44}") BigDecimal co2KgPerKwh
    ) {
        this.saveEnergyReadingPort = saveEnergyReadingPort;
        this.findEnergyBaselinePort = findEnergyBaselinePort;
        this.saveEnergyAnomalyPort = saveEnergyAnomalyPort;
        this.energyAiPredictionPort = energyAiPredictionPort;
        this.demoTenantId = demoTenantId;
        this.demoMedidorId = demoMedidorId;
        this.demoUserId = demoUserId;
        this.demoDefaultBaselineKwh = demoDefaultBaselineKwh;
        this.demoDefaultTolerancePercent = demoDefaultTolerancePercent;
        this.costPerKwh = costPerKwh;
        this.co2KgPerKwh = co2KgPerKwh;
    }

    @Transactional
    @Override
    public AnalyzeEnergyReadingResult analyze(AnalyzeEnergyReadingCommand command) {
        log.info("Lectura energetica recibida facilityId={} meterId={} measuredAt={} kwh={}",
                command.facilityId(), command.meterId(), command.measuredAt(), command.kwh());

        EnergyBaseline baseline = resolveBaseline();

        EnergyReadingRecord reading = saveEnergyReadingPort.save(new EnergyReadingRecord(
                null,
                demoTenantId,
                demoMedidorId,
                demoUserId,
                command.facilityId(),
                command.meterId(),
                command.measuredAt(),
                command.measuredAt(),
                command.kwh(),
                command.voltage(),
                command.powerFactor()
        ));

        EnergyAiPredictionInput aiInput = new EnergyAiPredictionInput(
                demoTenantId,
                demoMedidorId,
                command.facilityId(),
                command.meterId(),
                command.measuredAt(),
                command.kwh(),
                command.voltage(),
                command.powerFactor(),
                baseline.expectedKwh(),
                baseline.tolerancePercent()
        );

        Optional<EnergyAiPredictionResult> aiPrediction = energyAiPredictionPort.predict(aiInput);

        BigDecimal deviationPercent;
        boolean anomalyDetected;
        BigDecimal excessKwh;
        String recommendation;
        String explanation;
        boolean iaUtilizada;
        AnomalySeverity severity = null;

        if (aiPrediction.isPresent()) {
            EnergyAiPredictionResult result = aiPrediction.get();
            iaUtilizada = true;
            anomalyDetected = result.anomalyDetected();
            deviationPercent = EnergyAnalysisPolicy.calculateDeviationPercent(command.kwh(), result.predictedKwh());
            excessKwh = EnergyAnalysisPolicy.excessKwh(command.kwh(), result.predictedKwh());
            recommendation = result.recommendation();
            explanation = result.explanation();
            if (anomalyDetected) {
                severity = EnergyAnalysisPolicy.severityFor(deviationPercent);
                if (severity == null) {
                    severity = AnomalySeverity.MEDIUM;
                }
            }
        } else {
            iaUtilizada = false;
            deviationPercent = EnergyAnalysisPolicy.calculateDeviationPercent(command.kwh(), baseline.expectedKwh());
            anomalyDetected = EnergyAnalysisPolicy.exceedsTolerance(deviationPercent, baseline.tolerancePercent());
            excessKwh = EnergyAnalysisPolicy.excessKwh(command.kwh(), baseline.expectedKwh());
            recommendation = anomalyDetected ? "Revisar equipos activos fuera de horario o consumo superior al baseline." : "Consumo dentro del rango esperado del baseline.";
            explanation = anomalyDetected ? "La lectura supera el baseline configurado para el medidor." : "Lectura normal basada en heuristica local.";
            if (anomalyDetected) {
                severity = EnergyAnalysisPolicy.severityFor(deviationPercent);
            }
        }

        BigDecimal estimatedCostImpact = EnergyAnalysisPolicy.roundImpact(excessKwh.multiply(costPerKwh));
        BigDecimal estimatedCo2Impact = EnergyAnalysisPolicy.roundImpact(excessKwh.multiply(co2KgPerKwh));

        EnergyAnomalyRecord anomaly = null;

        if (anomalyDetected) {
            anomaly = buildAnomaly(reading, deviationPercent, severity, explanation, recommendation, estimatedCostImpact, estimatedCo2Impact, iaUtilizada);
            anomaly = saveEnergyAnomalyPort.save(anomaly);
            log.info("Anomalia detectada readingId={} anomalyId={} severity={} deviationPercent={} iaUtilizada={}",
                    reading.id(), anomaly.id(), severity, deviationPercent, iaUtilizada);
        } else {
            log.info("Sin anomalia readingId={} deviationPercent={} tolerancePercent={} iaUtilizada={}",
                    reading.id(), deviationPercent, baseline.tolerancePercent(), iaUtilizada);
        }

        return new AnalyzeEnergyReadingResult(
                reading.id(),
                anomaly == null ? null : anomaly.id(),
                anomalyDetected,
                severity,
                EnergyAnalysisPolicy.roundPercent(deviationPercent),
                recommendation,
                estimatedCostImpact,
                estimatedCo2Impact
        );
    }

    private EnergyBaseline resolveBaseline() {
        return findEnergyBaselinePort.findActiveByMedidorId(demoMedidorId)
                .map(record -> {
                    BigDecimal tolerance = record.tolerancePercent() != null
                            ? record.tolerancePercent()
                            : demoDefaultTolerancePercent;
                    log.info("Baseline encontrado medidorId={} expectedKwh={} tolerancePercent={}",
                            demoMedidorId, record.expectedKwh(), tolerance);
                    return new EnergyBaseline(record.expectedKwh(), tolerance);
                })
                .orElseGet(() -> {
                    log.warn("No existe baseline activo para medidorId={}; usando fallback demo expectedKwh={} tolerancePercent={}",
                            demoMedidorId, demoDefaultBaselineKwh, demoDefaultTolerancePercent);
                    return new EnergyBaseline(demoDefaultBaselineKwh, demoDefaultTolerancePercent);
                });
    }

    private EnergyAnomalyRecord buildAnomaly(
            EnergyReadingRecord reading,
            BigDecimal deviationPercent,
            AnomalySeverity severity,
            String explanation,
            String recommendation,
            BigDecimal estimatedCostImpact,
            BigDecimal estimatedCo2Impact,
            boolean iaUtilizada
    ) {
        return new EnergyAnomalyRecord(
                null,
                demoTenantId,
                demoMedidorId,
                reading.id(),
                reading.facilityId(),
                reading.meterId(),
                reading.measuredAt(),
                AnomalyType.EXCESS_CONSUMPTION,
                severity,
                EnergyAnalysisPolicy.scoreFor(severity),
                EnergyAnalysisPolicy.roundPercent(deviationPercent),
                explanation,
                recommendation,
                estimatedCostImpact,
                estimatedCo2Impact,
                iaUtilizada,
                "ABIERTA",
                null,
                null
        );
    }
}

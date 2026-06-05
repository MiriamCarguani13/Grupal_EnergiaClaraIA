package com.energiaclara.application.energyops.service;

import com.energiaclara.application.energyops.dto.AnalyzeEnergyReadingCommand;
import com.energiaclara.application.energyops.dto.AnalyzeEnergyReadingResult;
import com.energiaclara.application.energyops.dto.EnergyAiAnalysisOutcome;
import com.energiaclara.application.energyops.dto.EnergyAiAnalysisRequest;
import com.energiaclara.application.energyops.dto.EnergyAnomalyRecord;
import com.energiaclara.application.energyops.dto.EnergyBaselineRecord;
import com.energiaclara.application.energyops.dto.EnergyReadingRecord;
import com.energiaclara.application.port.in.AnalyzeEnergyReadingUseCase;
import com.energiaclara.application.port.out.EnergyAiAnalysisPort;
import com.energiaclara.application.port.out.FindEnergyBaselinePort;
import com.energiaclara.application.port.out.SaveEnergyAnomalyPort;
import com.energiaclara.application.port.out.SaveEnergyReadingPort;
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
import java.util.UUID;

@Service
public class EnergyAnalysisService implements AnalyzeEnergyReadingUseCase {

    private static final Logger log = LoggerFactory.getLogger(EnergyAnalysisService.class);

    private final SaveEnergyReadingPort saveEnergyReadingPort;
    private final FindEnergyBaselinePort findEnergyBaselinePort;
    private final SaveEnergyAnomalyPort saveEnergyAnomalyPort;
    private final EnergyAiAnalysisPort energyAiAnalysisPort;
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
            EnergyAiAnalysisPort energyAiAnalysisPort,
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
        this.energyAiAnalysisPort = energyAiAnalysisPort;
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

        EnergyAiAnalysisOutcome aiOutcome = analyzeWithAiOrFallback(command, baseline);

        BigDecimal deviationPercent = aiOutcome.deviationPercent();
        boolean anomalyDetected = aiOutcome.anomalyDetected();
        BigDecimal estimatedCostImpact = aiOutcome.estimatedCostImpact();
        BigDecimal estimatedCo2Impact = aiOutcome.estimatedCo2Impact();
        AnomalySeverity severity = aiOutcome.severity();
        String recommendation = aiOutcome.recommendation();
        String explanation = enrichAiExplanation(aiOutcome);
        boolean aiUsed = aiOutcome.aiUsed();
        BigDecimal score = aiOutcome.score();
        String modelVersion = aiOutcome.modelVersion();

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

        EnergyAnomalyRecord anomaly = null;

        if (anomalyDetected) {
            anomaly = buildAnomaly(
                    reading,
                    deviationPercent,
                    severity,
                    recommendation,
                    explanation,
                    estimatedCostImpact,
                    estimatedCo2Impact,
                    score,
                    aiUsed,
                    modelVersion
            );
            anomaly = saveEnergyAnomalyPort.save(anomaly);
            log.info("Anomalia detectada readingId={} anomalyId={} severity={} deviationPercent={} aiUsed={}",
                    reading.id(), anomaly.id(), severity, deviationPercent, aiUsed);
        } else {
            log.info("Sin anomalia readingId={} deviationPercent={} tolerancePercent={} aiUsed={}",
                    reading.id(), deviationPercent, baseline.tolerancePercent(), aiUsed);
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

    private EnergyAiAnalysisOutcome analyzeWithAiOrFallback(AnalyzeEnergyReadingCommand command, EnergyBaseline baseline) {
        try {
            EnergyAiAnalysisOutcome outcome = energyAiAnalysisPort.analyze(new EnergyAiAnalysisRequest(
                    demoTenantId,
                    demoMedidorId,
                    command.facilityId(),
                    command.meterId(),
                    command.measuredAt(),
                    command.kwh(),
                    baseline.expectedKwh(),
                    baseline.tolerancePercent(),
                    costPerKwh,
                    co2KgPerKwh,
                    command.voltage(),
                    command.powerFactor()
            ));

            if (!outcome.fallback()) {
                log.info("IA hibrida aplicada modelVersion={} deviationPercent={} severity={}",
                        outcome.modelVersion(), outcome.deviationPercent(), outcome.severity());
                return outcome;
            }

            log.info("IA hibrida en fallback; se mantienen calculos deterministas actuales. modelVersion={}",
                    outcome.modelVersion());
            return deterministicFallback(command, baseline, outcome.explanation());
        } catch (Exception ex) {
            log.warn("Fallo IA hibrida; se usan calculos deterministas actuales: {}", ex.getMessage());
            return deterministicFallback(command, baseline, "IA hibrida no disponible; se uso baseline deterministico actual.");
        }
    }

    private EnergyAiAnalysisOutcome deterministicFallback(
            AnalyzeEnergyReadingCommand command,
            EnergyBaseline baseline,
            String explanation
    ) {
        BigDecimal deviationPercent = EnergyAnalysisPolicy.calculateDeviationPercent(command.kwh(), baseline.expectedKwh());
        boolean anomalyDetected = EnergyAnalysisPolicy.exceedsTolerance(deviationPercent, baseline.tolerancePercent());
        BigDecimal excessKwh = EnergyAnalysisPolicy.excessKwh(command.kwh(), baseline.expectedKwh());
        BigDecimal estimatedCostImpact = EnergyAnalysisPolicy.roundImpact(excessKwh.multiply(costPerKwh));
        BigDecimal estimatedCo2Impact = EnergyAnalysisPolicy.roundImpact(excessKwh.multiply(co2KgPerKwh));
        AnomalySeverity severity = anomalyDetected ? EnergyAnalysisPolicy.severityFor(deviationPercent) : null;
        String recommendation = anomalyDetected
                ? "Revisar equipos activos fuera de horario o consumo superior al baseline."
                : "Consumo dentro del rango esperado del baseline.";

        return new EnergyAiAnalysisOutcome(
                false,
                true,
                anomalyDetected,
                severity,
                EnergyAnalysisPolicy.roundPercent(deviationPercent),
                severity == null ? BigDecimal.ZERO : EnergyAnalysisPolicy.scoreFor(severity),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                baseline.expectedKwh(),
                0,
                "STATIC_BASELINE",
                estimatedCostImpact,
                estimatedCo2Impact,
                explanation == null || explanation.isBlank()
                        ? "Analisis deterministico basado en baseline configurado."
                        : explanation,
                recommendation,
                "deterministic-energyops-v1"
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
            String recommendation,
            String explanation,
            BigDecimal estimatedCostImpact,
            BigDecimal estimatedCo2Impact,
            BigDecimal score,
            boolean aiUsed,
            String modelVersion
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
                score,
                EnergyAnalysisPolicy.roundPercent(deviationPercent),
                explanation,
                recommendation,
                estimatedCostImpact,
                estimatedCo2Impact,
                aiUsed,
                modelVersion,
                "DETECTADA",
                null,
                null,
                null,
                null
        );
    }

    private static String enrichAiExplanation(EnergyAiAnalysisOutcome outcome) {
        String explanation = outcome.explanation();
        if (!outcome.aiUsed()) {
            return explanation;
        }
        return explanation
                + " IA hibrida utilizada=true"
                + ", baselineSource=" + outcome.baselineSource()
                + ", sampleCount=" + outcome.sampleCount()
                + ", expectedKwh=" + outcome.expectedKwh()
                + ", confidence=" + outcome.confidence()
                + ", modelVersion=" + outcome.modelVersion()
                + ".";
    }
}

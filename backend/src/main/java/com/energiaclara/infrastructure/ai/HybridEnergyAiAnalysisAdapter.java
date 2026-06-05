package com.energiaclara.infrastructure.ai;

import com.energiaclara.ai.application.AnalyzeEnergyWithAiService;
import com.energiaclara.ai.application.EnergyAiAnalysisCommand;
import com.energiaclara.ai.application.EnergyAiAnalysisResponse;
import com.energiaclara.ai.application.port.in.AnalyzeEnergyWithAiUseCase;
import com.energiaclara.ai.domain.EnergyAiHistoricalReading;
import com.energiaclara.ai.infrastructure.DefaultEnergyAiEngineAdapter;
import com.energiaclara.application.energyops.dto.EnergyAiAnalysisOutcome;
import com.energiaclara.application.energyops.dto.EnergyAiAnalysisRequest;
import com.energiaclara.application.port.out.EnergyAiAnalysisPort;
import com.energiaclara.domain.energyops.AnomalySeverity;
import com.energiaclara.infrastructure.persistence.entity.EnergyReadingEntity;
import com.energiaclara.infrastructure.persistence.repository.EnergyReadingRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class HybridEnergyAiAnalysisAdapter implements EnergyAiAnalysisPort {

    private final AnalyzeEnergyWithAiUseCase analyzeEnergyWithAiUseCase;

    public HybridEnergyAiAnalysisAdapter(EnergyReadingRepository energyReadingRepository) {
        this.analyzeEnergyWithAiUseCase = new AnalyzeEnergyWithAiService(
                (tenantId, meterId, limit) -> loadHistory(energyReadingRepository, tenantId, meterId),
                new DefaultEnergyAiEngineAdapter()
        );
    }

    @Override
    public EnergyAiAnalysisOutcome analyze(EnergyAiAnalysisRequest request) {
        EnergyAiAnalysisResponse response = analyzeEnergyWithAiUseCase.analyze(new EnergyAiAnalysisCommand(
                request.tenantId(),
                request.medidorId(),
                request.facilityId(),
                request.meterId(),
                request.measuredAt(),
                request.kwh(),
                request.staticBaselineKwh(),
                request.tolerancePercent(),
                request.costPerKwh(),
                request.co2KgPerKwh(),
                request.voltage(),
                request.powerFactor()
        ));

        return new EnergyAiAnalysisOutcome(
                !response.fallback(),
                response.fallback(),
                response.anomalyDetected(),
                toBackendSeverity(response.severity()),
                response.deviationPercent(),
                response.anomalyScore(),
                response.confidence(),
                response.zScore(),
                response.expectedKwh(),
                response.sampleCount(),
                response.baselineSource().name(),
                response.estimatedCostImpact(),
                response.estimatedCo2Impact(),
                response.explanation(),
                response.recommendation(),
                response.modelVersion()
        );
    }

    private static List<EnergyAiHistoricalReading> loadHistory(
            EnergyReadingRepository repository,
            UUID tenantId,
            UUID medidorId
    ) {
        return repository.findTop7ByTenantIdAndMedidorIdOrderByMeasuredAtDesc(tenantId, medidorId).stream()
                .map(HybridEnergyAiAnalysisAdapter::toAiReading)
                .toList();
    }

    private static EnergyAiHistoricalReading toAiReading(EnergyReadingEntity entity) {
        return new EnergyAiHistoricalReading(
                entity.getTenantId(),
                entity.getMedidorId(),
                entity.getMeasuredAt(),
                entity.getKwh()
        );
    }

    private static AnomalySeverity toBackendSeverity(com.energiaclara.ai.domain.HybridSeverity severity) {
        return switch (severity) {
            case NORMAL -> null;
            case LOW -> AnomalySeverity.LOW;
            case MEDIUM -> AnomalySeverity.MEDIUM;
            case HIGH -> AnomalySeverity.HIGH;
            case CRITICAL -> AnomalySeverity.CRITICAL;
        };
    }
}

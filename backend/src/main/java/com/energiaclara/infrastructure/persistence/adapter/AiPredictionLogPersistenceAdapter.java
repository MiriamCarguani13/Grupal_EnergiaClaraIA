package com.energiaclara.infrastructure.persistence.adapter;

import com.energiaclara.application.energyops.dto.EnergyAiPredictionResult;
import com.energiaclara.application.port.out.SaveAiPredictionLogPort;
import com.energiaclara.infrastructure.persistence.entity.AiPredictionLogEntity;
import com.energiaclara.infrastructure.persistence.repository.AiPredictionLogRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AiPredictionLogPersistenceAdapter implements SaveAiPredictionLogPort {

    private final AiPredictionLogRepository repository;

    public AiPredictionLogPersistenceAdapter(AiPredictionLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(UUID tenantId, UUID medidorId, EnergyAiPredictionResult result) {
        AiPredictionLogEntity entity = new AiPredictionLogEntity();
        entity.setTenantId(tenantId);
        entity.setMedidorId(medidorId);
        entity.setModelVersion(result.modelVersion());
        entity.setInputHash(result.inputHash());
        entity.setOutputHash(result.outputHash());
        entity.setPredictedKwh(result.predictedKwh());
        entity.setConfidence(result.confidence());
        entity.setAnomalyDetected(result.anomalyDetected());
        entity.setLatencyMs(Math.toIntExact(result.latencyMs()));

        repository.save(entity);
    }
}

package com.energiaclara.infrastructure.energyops.persistence.adapter;

import com.energiaclara.application.port.out.EnergyAnomalyRepositoryPort;
import com.energiaclara.core.domain.energy.EnergyAnomaly;
import com.energiaclara.infrastructure.energyops.persistence.repository.AnomaliaRepository;
import org.springframework.stereotype.Component;

/**
 * SKELETON. Implementa {@link EnergyAnomalyRepositoryPort}.
 * TODO equipo EnergyOps: mapeo bidireccional AnomaliaEntity ↔ EnergyAnomaly.
 */
@Component
public class EnergyAnomalyRepositoryAdapter implements EnergyAnomalyRepositoryPort {

    private final AnomaliaRepository anomaliaRepository;

    public EnergyAnomalyRepositoryAdapter(AnomaliaRepository anomaliaRepository) {
        this.anomaliaRepository = anomaliaRepository;
    }

    @Override
    public EnergyAnomaly save(EnergyAnomaly anomaly) {
        // TODO equipo: AnomaliaJpaMapper.toEntity(anomaly), anomaliaRepository.save(entity), retornar anomaly
        throw new UnsupportedOperationException("SKELETON: implementar save() en EnergyAnomalyRepositoryAdapter");
    }
}

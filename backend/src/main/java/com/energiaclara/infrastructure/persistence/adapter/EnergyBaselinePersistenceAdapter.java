package com.energiaclara.infrastructure.persistence.adapter;

import com.energiaclara.application.energyops.dto.EnergyBaselineRecord;
import com.energiaclara.application.port.out.FindEnergyBaselinePort;
import com.energiaclara.infrastructure.persistence.entity.EnergyBaselineEntity;
import com.energiaclara.infrastructure.persistence.repository.EnergyBaselineRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class EnergyBaselinePersistenceAdapter implements FindEnergyBaselinePort {

    private final EnergyBaselineRepository baselineRepository;

    public EnergyBaselinePersistenceAdapter(EnergyBaselineRepository baselineRepository) {
        this.baselineRepository = baselineRepository;
    }

    @Override
    public Optional<EnergyBaselineRecord> findActiveByMedidorId(UUID medidorId) {
        return baselineRepository.findFirstByMedidorIdAndActiveTrue(medidorId)
                .map(this::toRecord);
    }

    private EnergyBaselineRecord toRecord(EnergyBaselineEntity entity) {
        return new EnergyBaselineRecord(
                entity.getId(),
                entity.getMedidorId(),
                entity.getExpectedKwh(),
                entity.getTolerancePercent()
        );
    }
}

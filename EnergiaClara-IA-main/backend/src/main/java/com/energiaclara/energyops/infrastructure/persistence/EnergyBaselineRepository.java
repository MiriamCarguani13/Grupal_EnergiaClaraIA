package com.energiaclara.energyops.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EnergyBaselineRepository extends JpaRepository<EnergyBaselineEntity, UUID> {
    Optional<EnergyBaselineEntity> findFirstByMedidorIdAndActiveTrue(UUID medidorId);
}

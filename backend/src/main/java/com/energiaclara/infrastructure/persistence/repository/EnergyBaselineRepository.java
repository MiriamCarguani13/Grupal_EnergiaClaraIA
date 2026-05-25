package com.energiaclara.infrastructure.persistence.repository;

import com.energiaclara.infrastructure.persistence.entity.EnergyBaselineEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EnergyBaselineRepository extends JpaRepository<EnergyBaselineEntity, UUID> {
    Optional<EnergyBaselineEntity> findFirstByMedidorIdAndActiveTrue(UUID medidorId);
}

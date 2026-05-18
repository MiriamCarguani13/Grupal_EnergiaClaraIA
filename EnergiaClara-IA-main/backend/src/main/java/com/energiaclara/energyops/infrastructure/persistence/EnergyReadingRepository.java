package com.energiaclara.energyops.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EnergyReadingRepository extends JpaRepository<EnergyReadingEntity, UUID> {
    List<EnergyReadingEntity> findTop20ByOrderByMeasuredAtDesc();
}

package com.energiaclara.infrastructure.persistence.repository;

import com.energiaclara.infrastructure.persistence.entity.EnergyReadingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EnergyReadingRepository extends JpaRepository<EnergyReadingEntity, UUID> {
    List<EnergyReadingEntity> findTop20ByOrderByMeasuredAtDesc();
}

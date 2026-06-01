package com.energiaclara.infrastructure.persistence.repository;

import com.energiaclara.infrastructure.persistence.entity.EnergyAnomalyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EnergyAnomalyRepository extends JpaRepository<EnergyAnomalyEntity, UUID> {
    List<EnergyAnomalyEntity> findTop20ByOrderByMeasuredAtDesc();
    long countByEstadoNot(String estado);
}

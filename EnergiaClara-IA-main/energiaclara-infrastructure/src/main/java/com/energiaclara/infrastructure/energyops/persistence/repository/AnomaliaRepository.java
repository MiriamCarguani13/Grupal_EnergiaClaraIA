package com.energiaclara.infrastructure.energyops.persistence.repository;

import com.energiaclara.infrastructure.energyops.persistence.entity.AnomaliaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * SKELETON. JPA repository para {@code energiaops.anomalia}.
 */
public interface AnomaliaRepository extends JpaRepository<AnomaliaEntity, UUID> {

    List<AnomaliaEntity> findAllByInquilinoIdAndDetectadaElBetween(UUID inquilinoId, Instant from, Instant to);
    List<AnomaliaEntity> findAllByInquilinoIdAndEstado(UUID inquilinoId, String estado);
}

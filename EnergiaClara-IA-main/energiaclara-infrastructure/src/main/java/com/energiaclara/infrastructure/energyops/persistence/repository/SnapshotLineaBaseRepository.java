package com.energiaclara.infrastructure.energyops.persistence.repository;

import com.energiaclara.infrastructure.energyops.persistence.entity.SnapshotLineaBaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SKELETON. JPA repository para {@code energiaops.snapshot_linea_base}.
 */
public interface SnapshotLineaBaseRepository extends JpaRepository<SnapshotLineaBaseEntity, UUID> {

    List<SnapshotLineaBaseEntity> findAllByInquilinoIdAndActivoTrue(UUID inquilinoId);

    Optional<SnapshotLineaBaseEntity> findFirstByInquilinoIdAndMedidorIdAndActivoTrue(UUID inquilinoId, UUID medidorId);
}

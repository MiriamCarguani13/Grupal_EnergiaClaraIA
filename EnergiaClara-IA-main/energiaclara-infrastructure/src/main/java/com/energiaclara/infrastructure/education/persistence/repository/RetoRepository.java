package com.energiaclara.infrastructure.education.persistence.repository;

import com.energiaclara.infrastructure.education.persistence.entity.RetoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * SKELETON. JPA repository para {@code educacion.reto}.
 */
public interface RetoRepository extends JpaRepository<RetoEntity, UUID> {

    List<RetoEntity> findAllByInquilinoIdAndEstado(UUID inquilinoId, String estado);
}

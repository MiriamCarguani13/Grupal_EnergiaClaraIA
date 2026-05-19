package com.energiaclara.infrastructure.maintenance.persistence.repository;

import com.energiaclara.infrastructure.maintenance.persistence.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * SKELETON. JPA repository para {@code mantenimiento.ticket}.
 */
public interface TicketRepository extends JpaRepository<TicketEntity, UUID> {

    List<TicketEntity> findAllByInquilinoIdAndEstado(UUID inquilinoId, String estado);
    List<TicketEntity> findAllByInquilinoIdAndAsignadoA(UUID inquilinoId, UUID asignadoA);
}

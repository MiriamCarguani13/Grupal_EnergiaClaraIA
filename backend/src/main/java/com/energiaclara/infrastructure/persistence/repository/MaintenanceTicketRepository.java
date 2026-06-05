package com.energiaclara.infrastructure.persistence.repository;

import com.energiaclara.infrastructure.persistence.entity.MaintenanceTicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MaintenanceTicketRepository extends JpaRepository<MaintenanceTicketEntity, UUID> {
    List<MaintenanceTicketEntity> findTop50ByTenantIdAndAssignedToOrderByUpdatedAtDesc(UUID tenantId, UUID assignedTo);

    List<MaintenanceTicketEntity> findTop50ByTenantIdAndAssignedToIsNullOrderByUpdatedAtDesc(UUID tenantId);

    List<MaintenanceTicketEntity> findTop50ByTenantIdOrderByUpdatedAtDesc(UUID tenantId);

    Optional<MaintenanceTicketEntity> findByAnomalyId(UUID anomalyId);
}

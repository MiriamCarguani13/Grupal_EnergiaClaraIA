package com.energiaclara.infrastructure.persistence.repository;

import com.energiaclara.infrastructure.persistence.entity.EnergyAnomalyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnergyAnomalyRepository extends JpaRepository<EnergyAnomalyEntity, UUID> {
    List<EnergyAnomalyEntity> findTop20ByOrderByMeasuredAtDesc();

    List<EnergyAnomalyEntity> findTop20ByEstadoInOrderByMeasuredAtDesc(Collection<String> estados);

    List<EnergyAnomalyEntity> findByReadingIdIn(Collection<UUID> readingIds);

    Optional<EnergyAnomalyEntity> findByTicketId(UUID ticketId);
}

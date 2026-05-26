package com.energiaclara.infrastructure.maintenance.repository;

import com.energiaclara.infrastructure.maintenance.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TicketJpaRepository extends JpaRepository<TicketEntity, UUID> {
}

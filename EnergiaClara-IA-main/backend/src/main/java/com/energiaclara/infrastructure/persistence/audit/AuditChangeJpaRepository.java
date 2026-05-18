package com.energiaclara.infrastructure.persistence.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditChangeJpaRepository extends JpaRepository<AuditChangeEntity, UUID> {
}

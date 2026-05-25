package com.energiaclara.infrastructure.persistence.repository;

import com.energiaclara.infrastructure.persistence.entity.AuditChangeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditChangeJpaRepository extends JpaRepository<AuditChangeEntity, UUID> {
}

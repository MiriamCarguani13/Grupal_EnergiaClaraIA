package com.energiaclara.infrastructure.persistence.repository;

import com.energiaclara.infrastructure.persistence.entity.AiPredictionLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AiPredictionLogRepository extends JpaRepository<AiPredictionLogEntity, UUID> {
}

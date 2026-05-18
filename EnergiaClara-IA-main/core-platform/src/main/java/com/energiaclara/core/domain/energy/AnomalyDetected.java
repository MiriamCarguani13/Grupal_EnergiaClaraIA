package com.energiaclara.core.domain.energy;

import com.energiaclara.core.domain.shared.DomainEvent;
import com.energiaclara.core.domain.shared.TenantId;

import java.time.Instant;

public record AnomalyDetected(
        AnomalyId anomalyId,
        TenantId tenantId,
        AnomalyType type,
        AnomalySeverity severity,
        Instant occurredAt
) implements DomainEvent {
    public AnomalyDetected(AnomalyId anomalyId, TenantId tenantId, AnomalyType type, AnomalySeverity severity) {
        this(anomalyId, tenantId, type, severity, Instant.now());
    }
}

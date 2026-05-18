package com.energiaclara.core.domain.energy;

import com.energiaclara.core.domain.shared.FacilityId;
import com.energiaclara.core.domain.shared.DomainEvent;
import com.energiaclara.core.domain.shared.TenantId;

import java.time.Instant;

public record EnergyReadingRegistered(
        EnergyReadingId readingId,
        TenantId tenantId,
        FacilityId facilityId,
        KwhValue kwhValue,
        Instant readingTimestamp,
        Instant occurredAt
) implements DomainEvent {
    public EnergyReadingRegistered(EnergyReadingId readingId, TenantId tenantId, FacilityId facilityId, KwhValue kwhValue, Instant readingTimestamp) {
        this(readingId, tenantId, facilityId, kwhValue, readingTimestamp, Instant.now());
    }
}

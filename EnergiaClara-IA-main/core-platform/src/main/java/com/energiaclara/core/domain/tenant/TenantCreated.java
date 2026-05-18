package com.energiaclara.core.domain.tenant;

import com.energiaclara.core.domain.shared.DomainEvent;
import com.energiaclara.core.domain.shared.TenantId;

import java.time.Instant;

public record TenantCreated(TenantId tenantId, TenantName name, Instant occurredAt) implements DomainEvent {
    public TenantCreated(TenantId tenantId, TenantName name) {
        this(tenantId, name, Instant.now());
    }
}

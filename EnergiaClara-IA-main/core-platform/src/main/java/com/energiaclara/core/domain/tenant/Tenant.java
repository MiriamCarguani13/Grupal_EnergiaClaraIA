package com.energiaclara.core.domain.tenant;

import com.energiaclara.core.domain.shared.DomainEvent;
import com.energiaclara.core.domain.shared.DomainException;
import com.energiaclara.core.domain.shared.TenantId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Tenant {
    private final TenantId id;
    private final TenantName name;
    private TenantStatus status;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Tenant(TenantId id, TenantName name, TenantStatus status) {
        this.id = Objects.requireNonNull(id, "TenantId no puede ser nulo");
        this.name = Objects.requireNonNull(name, "TenantName no puede ser nulo");
        this.status = Objects.requireNonNull(status, "TenantStatus no puede ser nulo");
    }

    public static Tenant onboard(TenantName name) {
        Tenant tenant = new Tenant(TenantId.generate(), name, TenantStatus.ACTIVE);
        tenant.domainEvents.add(new TenantCreated(tenant.id, tenant.name));
        return tenant;
    }

    public void suspend() {
        if (this.status == TenantStatus.SUSPENDED) {
            throw new DomainException("El tenant ya está suspendido: " + id);
        }
        this.status = TenantStatus.SUSPENDED;
    }

    public void reactivate() {
        if (this.status == TenantStatus.ACTIVE) {
            throw new DomainException("El tenant ya está activo: " + id);
        }
        this.status = TenantStatus.ACTIVE;
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    public TenantId getId() { return id; }
    public TenantName getName() { return name; }
    public TenantStatus getStatus() { return status; }
}

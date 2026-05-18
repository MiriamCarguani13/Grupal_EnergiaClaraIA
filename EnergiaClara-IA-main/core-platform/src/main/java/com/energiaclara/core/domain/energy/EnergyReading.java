package com.energiaclara.core.domain.energy;

import com.energiaclara.core.domain.shared.DomainEvent;
import com.energiaclara.core.domain.shared.FacilityId;
import com.energiaclara.core.domain.shared.TenantId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EnergyReading {
    private final EnergyReadingId id;
    private final TenantId tenantId;
    private final FacilityId facilityId;
    private final KwhValue kwhValue;
    private final Instant timestamp;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private EnergyReading(EnergyReadingId id, TenantId tenantId, FacilityId facilityId, KwhValue kwhValue, Instant timestamp) {
        this.id = Objects.requireNonNull(id, "EnergyReadingId no puede ser nulo");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId no puede ser nulo");
        this.facilityId = Objects.requireNonNull(facilityId, "FacilityId no puede ser nulo");
        this.kwhValue = Objects.requireNonNull(kwhValue, "KwhValue no puede ser nulo");
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp no puede ser nulo");
    }

    public static EnergyReading register(TenantId tenantId, FacilityId facilityId, KwhValue kwhValue, Instant timestamp) {
        EnergyReading reading = new EnergyReading(EnergyReadingId.generate(), tenantId, facilityId, kwhValue, timestamp);
        reading.domainEvents.add(new EnergyReadingRegistered(reading.id, tenantId, facilityId, kwhValue, timestamp));
        return reading;
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    public EnergyReadingId getId() { return id; }
    public TenantId getTenantId() { return tenantId; }
    public FacilityId getFacilityId() { return facilityId; }
    public KwhValue getKwhValue() { return kwhValue; }
    public Instant getTimestamp() { return timestamp; }
}

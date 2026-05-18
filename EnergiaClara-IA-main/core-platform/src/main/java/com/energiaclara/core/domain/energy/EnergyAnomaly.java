package com.energiaclara.core.domain.energy;

import com.energiaclara.core.domain.shared.DomainEvent;
import com.energiaclara.core.domain.shared.DomainException;
import com.energiaclara.core.domain.shared.TenantId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EnergyAnomaly {
    private final AnomalyId id;
    private final EnergyReadingId readingId;
    private final TenantId tenantId;
    private AnomalyStatus status;
    private final AnomalyType type;
    private final AnomalySeverity severity;
    private final KwhValue delta;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private EnergyAnomaly(AnomalyId id, EnergyReadingId readingId, TenantId tenantId, AnomalyStatus status, AnomalyType type, AnomalySeverity severity, KwhValue delta) {
        this.id = Objects.requireNonNull(id, "AnomalyId no puede ser nulo");
        this.readingId = Objects.requireNonNull(readingId, "EnergyReadingId no puede ser nulo");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId no puede ser nulo");
        this.status = Objects.requireNonNull(status, "AnomalyStatus no puede ser nulo");
        this.type = Objects.requireNonNull(type, "AnomalyType no puede ser nulo");
        this.severity = Objects.requireNonNull(severity, "AnomalySeverity no puede ser nulo");
        this.delta = Objects.requireNonNull(delta, "KwhValue delta no puede ser nulo");
    }

    public static EnergyAnomaly detect(EnergyReadingId readingId, TenantId tenantId, AnomalyType type, AnomalySeverity severity, KwhValue delta) {
        EnergyAnomaly anomaly = new EnergyAnomaly(AnomalyId.generate(), readingId, tenantId, AnomalyStatus.NEW, type, severity, delta);
        anomaly.domainEvents.add(new AnomalyDetected(anomaly.id, tenantId, type, severity));
        return anomaly;
    }

    public void acknowledge() {
        if (this.status != AnomalyStatus.NEW) {
            throw new AnomalyAlreadyAcknowledgedException(this.id);
        }
        this.status = AnomalyStatus.ACKNOWLEDGED;
    }

    public void resolve() {
        if (this.status == AnomalyStatus.NEW) {
            throw new DomainException("No se puede resolver una anomalía sin reconocerla primero: " + id.value());
        }
        if (this.status == AnomalyStatus.RESOLVED) {
            throw new DomainException("La anomalía ya está resuelta: " + id.value());
        }
        this.status = AnomalyStatus.RESOLVED;
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    public AnomalyId getId() { return id; }
    public EnergyReadingId getReadingId() { return readingId; }
    public TenantId getTenantId() { return tenantId; }
    public AnomalyStatus getStatus() { return status; }
    public AnomalyType getType() { return type; }
    public AnomalySeverity getSeverity() { return severity; }
    public KwhValue getDelta() { return delta; }
}

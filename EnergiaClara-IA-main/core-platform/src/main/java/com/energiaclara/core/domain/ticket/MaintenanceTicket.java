package com.energiaclara.core.domain.ticket;

import com.energiaclara.core.domain.energy.AnomalyId;
import com.energiaclara.core.domain.shared.DomainException;
import com.energiaclara.core.domain.shared.FacilityId;
import com.energiaclara.core.domain.shared.TenantId;
import com.energiaclara.core.domain.shared.UserId;

import java.time.Instant;
import java.util.Objects;

public class MaintenanceTicket {
    private final MaintenanceTicketId id;
    private final TenantId tenantId;
    private final FacilityId facilityId;
    private final AnomalyId anomalyId;
    private final String description;
    private final TicketPriority priority;
    private TicketStatus status;
    private UserId assignedTo;
    private final Instant createdAt;

    private MaintenanceTicket(MaintenanceTicketId id, TenantId tenantId, FacilityId facilityId, AnomalyId anomalyId, String description, TicketPriority priority, TicketStatus status, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "MaintenanceTicketId no puede ser nulo");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId no puede ser nulo");
        this.facilityId = Objects.requireNonNull(facilityId, "FacilityId no puede ser nulo");
        this.anomalyId = Objects.requireNonNull(anomalyId, "AnomalyId no puede ser nulo");
        this.description = validateDescription(description);
        this.priority = Objects.requireNonNull(priority, "TicketPriority no puede ser nulo");
        this.status = Objects.requireNonNull(status, "TicketStatus no puede ser nulo");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt no puede ser nulo");
    }

    public static MaintenanceTicket openForAnomaly(TenantId tenantId, FacilityId facilityId, AnomalyId anomalyId, String description, TicketPriority priority) {
        return new MaintenanceTicket(MaintenanceTicketId.generate(), tenantId, facilityId, anomalyId, description, priority, TicketStatus.OPEN, Instant.now());
    }

    public void assignTo(UserId userId) {
        if (status == TicketStatus.CLOSED || status == TicketStatus.RESOLVED) {
            throw new DomainException("No se puede asignar un ticket finalizado: " + id.value());
        }
        this.assignedTo = Objects.requireNonNull(userId, "UserId no puede ser nulo");
        this.status = TicketStatus.IN_PROGRESS;
    }

    public void resolve() {
        if (status != TicketStatus.IN_PROGRESS) {
            throw new DomainException("Solo se puede resolver un ticket en progreso: " + id.value());
        }
        this.status = TicketStatus.RESOLVED;
    }

    public void close() {
        if (status != TicketStatus.RESOLVED) {
            throw new DomainException("Solo se puede cerrar un ticket resuelto: " + id.value());
        }
        this.status = TicketStatus.CLOSED;
    }

    private static String validateDescription(String description) {
        Objects.requireNonNull(description, "La descripción no puede ser nula");
        String trimmed = description.trim();
        if (trimmed.length() < 10) {
            throw new DomainException("La descripción del ticket debe tener al menos 10 caracteres");
        }
        return trimmed;
    }

    public MaintenanceTicketId getId() { return id; }
    public TenantId getTenantId() { return tenantId; }
    public FacilityId getFacilityId() { return facilityId; }
    public AnomalyId getAnomalyId() { return anomalyId; }
    public String getDescription() { return description; }
    public TicketPriority getPriority() { return priority; }
    public TicketStatus getStatus() { return status; }
    public UserId getAssignedTo() { return assignedTo; }
    public Instant getCreatedAt() { return createdAt; }
}

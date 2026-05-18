package com.energiaclara.core.domain.ticket;

import java.util.Objects;
import java.util.UUID;

public record MaintenanceTicketId(UUID value) {
    public MaintenanceTicketId {
        Objects.requireNonNull(value, "MaintenanceTicketId no puede ser nulo");
    }

    public static MaintenanceTicketId generate() {
        return new MaintenanceTicketId(UUID.randomUUID());
    }
}

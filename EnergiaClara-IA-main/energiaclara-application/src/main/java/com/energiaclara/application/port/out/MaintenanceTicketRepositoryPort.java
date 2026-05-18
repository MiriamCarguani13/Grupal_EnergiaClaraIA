package com.energiaclara.application.port.out;

import com.energiaclara.core.domain.ticket.MaintenanceTicket;

public interface MaintenanceTicketRepositoryPort {
    MaintenanceTicket save(MaintenanceTicket ticket);
}

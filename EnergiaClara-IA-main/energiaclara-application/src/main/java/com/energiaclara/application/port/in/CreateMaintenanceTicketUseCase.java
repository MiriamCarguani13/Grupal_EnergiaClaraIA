package com.energiaclara.application.port.in;

import com.energiaclara.application.usecase.CreateMaintenanceTicketCommand;
import com.energiaclara.core.domain.ticket.MaintenanceTicketId;

public interface CreateMaintenanceTicketUseCase {
    MaintenanceTicketId create(CreateMaintenanceTicketCommand command);
}

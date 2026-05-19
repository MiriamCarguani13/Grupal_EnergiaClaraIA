package com.energiaclara.infrastructure.maintenance.persistence.adapter;

import com.energiaclara.application.port.out.MaintenanceTicketRepositoryPort;
import com.energiaclara.core.domain.ticket.MaintenanceTicket;
import com.energiaclara.infrastructure.maintenance.persistence.repository.TicketRepository;
import org.springframework.stereotype.Component;

/**
 * SKELETON. Implementa {@link MaintenanceTicketRepositoryPort}.
 * TODO equipo Maintenance: mapper bidireccional TicketEntity ↔ MaintenanceTicket.
 */
@Component
public class MaintenanceTicketRepositoryAdapter implements MaintenanceTicketRepositoryPort {

    private final TicketRepository ticketRepository;

    public MaintenanceTicketRepositoryAdapter(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public MaintenanceTicket save(MaintenanceTicket ticket) {
        // TODO equipo: TicketJpaMapper.toEntity(ticket), ticketRepository.save(entity), retornar ticket
        throw new UnsupportedOperationException("SKELETON: implementar save() en MaintenanceTicketRepositoryAdapter");
    }
}

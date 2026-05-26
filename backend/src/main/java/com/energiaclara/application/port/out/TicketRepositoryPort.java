package com.energiaclara.application.port.out;

import com.energiaclara.domain.maintenance.Ticket;

import java.util.Optional;
import java.util.UUID;

public interface TicketRepositoryPort {
    Ticket save(Ticket ticket);
    Optional<Ticket> findById(UUID ticketId);
    java.util.List<Ticket> findAll();
}

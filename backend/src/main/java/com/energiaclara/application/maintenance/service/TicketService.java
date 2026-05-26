package com.energiaclara.application.maintenance.service;

import com.energiaclara.application.maintenance.dto.CreateTicketCommand;
import com.energiaclara.application.port.out.TicketRepositoryPort;
import com.energiaclara.domain.maintenance.Ticket;
import com.energiaclara.domain.maintenance.TicketStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepositoryPort ticketRepositoryPort;

    @Transactional
    public Ticket createTicket(CreateTicketCommand command) {
        Ticket ticket = Ticket.builder()
                .ticketId(UUID.randomUUID())
                .inquilinoId(command.getInquilinoId())
                .edificioId(command.getEdificioId())
                .anomaliaId(command.getAnomaliaId())
                .politicaSlaId(command.getPoliticaSlaId())
                .titulo(command.getTitulo())
                .descripcion(command.getDescripcion())
                .prioridad(command.getPrioridad())
                .estado(TicketStatus.BORRADOR)
                .creadoPor(command.getCreadoPor())
                .vencimientoSla(LocalDateTime.now().plusHours(24)) // Ejemplo simple, debería usar la política real
                .slaIncumplido(false)
                .conteoReapertura(0)
                .creadoEn(LocalDateTime.now())
                .actualizadoEn(LocalDateTime.now())
                .build();

        return ticketRepositoryPort.save(ticket);
    }

    @Transactional
    public Ticket assignTicket(UUID ticketId, UUID tecnicoId) {
        Ticket ticket = ticketRepositoryPort.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado"));
        
        ticket.asignar(tecnicoId);
        return ticketRepositoryPort.save(ticket);
    }

    @Transactional
    public Ticket closeTicket(UUID ticketId, UUID tecnicoId, String qrHash) {
        // En el futuro, validar el qrHash con el edificio/medidor real
        if (qrHash == null || qrHash.isEmpty()) {
            throw new IllegalArgumentException("El código QR es obligatorio para cerrar el ticket");
        }

        Ticket ticket = ticketRepositoryPort.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado"));
        
        ticket.cerrar(tecnicoId);
        return ticketRepositoryPort.save(ticket);
    }
}

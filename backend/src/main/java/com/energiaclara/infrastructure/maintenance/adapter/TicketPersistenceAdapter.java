package com.energiaclara.infrastructure.maintenance.adapter;

import com.energiaclara.application.port.out.TicketRepositoryPort;
import com.energiaclara.domain.maintenance.Ticket;
import com.energiaclara.domain.maintenance.TicketPriority;
import com.energiaclara.domain.maintenance.TicketStatus;
import com.energiaclara.infrastructure.maintenance.entity.TicketEntity;
import com.energiaclara.infrastructure.maintenance.repository.TicketJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TicketPersistenceAdapter implements TicketRepositoryPort {

    private final TicketJpaRepository ticketJpaRepository;

    @Override
    public Ticket save(Ticket ticket) {
        TicketEntity entity = toEntity(ticket);
        TicketEntity savedEntity = ticketJpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Ticket> findById(UUID ticketId) {
        return ticketJpaRepository.findById(ticketId).map(this::toDomain);
    }

    @Override
    public java.util.List<Ticket> findAll() {
        return ticketJpaRepository.findAllByOrderByCreadoEnDesc().stream().map(this::toDomain).toList();
    }

    private TicketEntity toEntity(Ticket domain) {
        return TicketEntity.builder()
                .ticketId(domain.getTicketId())
                .inquilinoId(domain.getInquilinoId())
                .edificioId(domain.getEdificioId())
                .anomaliaId(domain.getAnomaliaId())
                .politicaSlaId(domain.getPoliticaSlaId())
                .titulo(domain.getTitulo())
                .descripcion(domain.getDescripcion())
                .prioridad(domain.getPrioridad() != null ? domain.getPrioridad().name() : null)
                .estado(domain.getEstado() != null ? domain.getEstado().name() : null)
                .asignadoA(domain.getAsignadoA())
                .creadoPor(domain.getCreadoPor())
                .vencimientoSla(domain.getVencimientoSla())
                .slaIncumplido(domain.isSlaIncumplido())
                .cerradoEl(domain.getCerradoEl())
                .cerradoPor(domain.getCerradoPor())
                .conteoReapertura(domain.getConteoReapertura())
                .creadoEn(domain.getCreadoEn())
                .actualizadoEn(domain.getActualizadoEn())
                .build();
    }

    private Ticket toDomain(TicketEntity entity) {
        return Ticket.builder()
                .ticketId(entity.getTicketId())
                .inquilinoId(entity.getInquilinoId())
                .edificioId(entity.getEdificioId())
                .anomaliaId(entity.getAnomaliaId())
                .politicaSlaId(entity.getPoliticaSlaId())
                .titulo(entity.getTitulo())
                .descripcion(entity.getDescripcion())
                .prioridad(entity.getPrioridad() != null ? TicketPriority.valueOf(entity.getPrioridad()) : null)
                .estado(entity.getEstado() != null ? TicketStatus.valueOf(entity.getEstado()) : null)
                .asignadoA(entity.getAsignadoA())
                .creadoPor(entity.getCreadoPor())
                .vencimientoSla(entity.getVencimientoSla())
                .slaIncumplido(entity.isSlaIncumplido())
                .cerradoEl(entity.getCerradoEl())
                .cerradoPor(entity.getCerradoPor())
                .conteoReapertura(entity.getConteoReapertura())
                .creadoEn(entity.getCreadoEn())
                .actualizadoEn(entity.getActualizadoEn())
                .build();
    }
}

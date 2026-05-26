package com.energiaclara.domain.maintenance;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class Ticket {
    private UUID ticketId;
    private UUID inquilinoId;
    private UUID edificioId;
    private UUID anomaliaId;
    private UUID politicaSlaId;
    
    private String titulo;
    private String descripcion;
    private TicketPriority prioridad;
    private TicketStatus estado;
    
    private UUID asignadoA;
    private UUID creadoPor;
    
    private LocalDateTime vencimientoSla;
    private boolean slaIncumplido;
    
    private LocalDateTime cerradoEl;
    private UUID cerradoPor;
    
    private int conteoReapertura;
    
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    public void asignar(UUID tecnicoId) {
        if (this.estado == TicketStatus.CERRADO) {
            throw new IllegalStateException("No se puede asignar un ticket cerrado");
        }
        this.asignadoA = tecnicoId;
        this.estado = TicketStatus.ASIGNADO;
        this.actualizadoEn = LocalDateTime.now();
    }

    public void cerrar(UUID tecnicoId) {
        if (this.estado == TicketStatus.CERRADO) {
            throw new IllegalStateException("El ticket ya está cerrado");
        }
        this.estado = TicketStatus.CERRADO;
        this.cerradoPor = tecnicoId;
        this.cerradoEl = LocalDateTime.now();
        this.actualizadoEn = LocalDateTime.now();
    }
}

package com.energiaclara.application.maintenance.dto;

import com.energiaclara.domain.maintenance.TicketPriority;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateTicketCommand {
    private UUID inquilinoId;
    private UUID edificioId;
    private UUID anomaliaId;
    private UUID politicaSlaId;
    private String titulo;
    private String descripcion;
    private TicketPriority prioridad;
    private UUID creadoPor;
}

package com.energiaclara.infrastructure.maintenance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(schema = "mantenimiento", name = "ticket")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketEntity {

    @Id
    @Column(name = "ticket_id")
    private UUID ticketId;

    @Column(name = "inquilino_id", nullable = false)
    private UUID inquilinoId;

    @Column(name = "edificio_id", nullable = false)
    private UUID edificioId;

    @Column(name = "anomalia_id")
    private UUID anomaliaId;

    @Column(name = "politica_sla_id", nullable = false)
    private UUID politicaSlaId;

    @Column(name = "titulo", nullable = false, length = 300)
    private String titulo;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "prioridad", nullable = false, length = 10)
    private String prioridad;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "asignado_a")
    private UUID asignadoA;

    @Column(name = "creado_por", nullable = false)
    private UUID creadoPor;

    @Column(name = "vencimiento_sla", nullable = false)
    private LocalDateTime vencimientoSla;

    @Column(name = "sla_incumplido", nullable = false)
    private boolean slaIncumplido;

    @Column(name = "cerrado_el")
    private LocalDateTime cerradoEl;

    @Column(name = "cerrado_por")
    private UUID cerradoPor;

    @Column(name = "conteo_reapertura", nullable = false)
    private int conteoReapertura;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;
}

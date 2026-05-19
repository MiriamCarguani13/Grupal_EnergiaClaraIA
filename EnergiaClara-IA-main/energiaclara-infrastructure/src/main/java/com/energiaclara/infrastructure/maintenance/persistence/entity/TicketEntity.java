package com.energiaclara.infrastructure.maintenance.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

/**
 * SKELETON. Mapea {@code mantenimiento.ticket}.
 * TODO equipo Maintenance: mapeo bidireccional con {@code MaintenanceTicket} domain.
 */
@Entity
@Table(name = "ticket", schema = "mantenimiento")
public class TicketEntity {

    @Id
    @Column(name = "ticket_id", nullable = false, updatable = false)
    private UUID ticketId;

    @Column(name = "inquilino_id", nullable = false)
    private UUID inquilinoId;

    @Column(name = "edificio_id", nullable = false)
    private UUID edificioId;

    @Column(name = "anomalia_id")
    private UUID anomaliaId;

    @Column(name = "politica_sla_id")
    private UUID politicaSlaId;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Column(name = "prioridad", nullable = false, length = 20)
    private String prioridad;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "asignado_a")
    private UUID asignadoA;

    @Column(name = "creado_por", nullable = false)
    private UUID creadoPor;

    @Column(name = "vencimiento_sla")
    private Instant vencimientoSla;

    @Column(name = "sla_incumplido", nullable = false)
    private boolean slaIncumplido;

    @Column(name = "cerrado_el")
    private Instant cerradoEl;

    @Column(name = "cerrado_por")
    private UUID cerradoPor;

    @Column(name = "conteo_reapertura", nullable = false)
    private int conteoReapertura;

    @Version
    @Column(name = "version_fila", nullable = false, insertable = false, updatable = false)
    private byte[] versionFila;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    public TicketEntity() {}

    // TODO equipo: getters/setters
    public UUID getTicketId() { return ticketId; }
    public void setTicketId(UUID v) { this.ticketId = v; }
    public UUID getInquilinoId() { return inquilinoId; }
    public void setInquilinoId(UUID v) { this.inquilinoId = v; }
    public UUID getEdificioId() { return edificioId; }
    public void setEdificioId(UUID v) { this.edificioId = v; }
    public UUID getAnomaliaId() { return anomaliaId; }
    public void setAnomaliaId(UUID v) { this.anomaliaId = v; }
    public UUID getPoliticaSlaId() { return politicaSlaId; }
    public void setPoliticaSlaId(UUID v) { this.politicaSlaId = v; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String v) { this.titulo = v; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String v) { this.descripcion = v; }
    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String v) { this.prioridad = v; }
    public String getEstado() { return estado; }
    public void setEstado(String v) { this.estado = v; }
    public UUID getAsignadoA() { return asignadoA; }
    public void setAsignadoA(UUID v) { this.asignadoA = v; }
    public UUID getCreadoPor() { return creadoPor; }
    public void setCreadoPor(UUID v) { this.creadoPor = v; }
    public Instant getVencimientoSla() { return vencimientoSla; }
    public void setVencimientoSla(Instant v) { this.vencimientoSla = v; }
    public boolean isSlaIncumplido() { return slaIncumplido; }
    public void setSlaIncumplido(boolean v) { this.slaIncumplido = v; }
    public Instant getCerradoEl() { return cerradoEl; }
    public void setCerradoEl(Instant v) { this.cerradoEl = v; }
    public UUID getCerradoPor() { return cerradoPor; }
    public void setCerradoPor(UUID v) { this.cerradoPor = v; }
    public int getConteoReapertura() { return conteoReapertura; }
    public void setConteoReapertura(int v) { this.conteoReapertura = v; }
    public byte[] getVersionFila() { return versionFila; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant v) { this.creadoEn = v; }
    public Instant getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(Instant v) { this.actualizadoEn = v; }
}

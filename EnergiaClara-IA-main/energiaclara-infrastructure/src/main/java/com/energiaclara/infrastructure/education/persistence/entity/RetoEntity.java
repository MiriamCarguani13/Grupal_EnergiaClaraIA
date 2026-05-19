package com.energiaclara.infrastructure.education.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * SKELETON. Mapea {@code educacion.reto}.
 * TODO equipo Education: completar mapping con {@code EnergyChallenge} domain.
 */
@Entity
@Table(name = "reto", schema = "educacion")
public class RetoEntity {

    @Id
    @Column(name = "reto_id", nullable = false, updatable = false)
    private UUID retoId;

    @Column(name = "inquilino_id", nullable = false)
    private UUID inquilinoId;

    @Column(name = "edificio_id")
    private UUID edificioId;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Column(name = "meta_kwh", nullable = false, precision = 18, scale = 4)
    private BigDecimal metaKwh;

    @Column(name = "periodo_inicio", nullable = false)
    private Instant periodoInicio;

    @Column(name = "periodo_fin", nullable = false)
    private Instant periodoFin;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "creado_por", nullable = false)
    private UUID creadoPor;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    public RetoEntity() {}

    // TODO equipo: getters/setters
    public UUID getRetoId() { return retoId; }
    public void setRetoId(UUID v) { this.retoId = v; }
    public UUID getInquilinoId() { return inquilinoId; }
    public void setInquilinoId(UUID v) { this.inquilinoId = v; }
    public UUID getEdificioId() { return edificioId; }
    public void setEdificioId(UUID v) { this.edificioId = v; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String v) { this.titulo = v; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String v) { this.descripcion = v; }
    public BigDecimal getMetaKwh() { return metaKwh; }
    public void setMetaKwh(BigDecimal v) { this.metaKwh = v; }
    public Instant getPeriodoInicio() { return periodoInicio; }
    public void setPeriodoInicio(Instant v) { this.periodoInicio = v; }
    public Instant getPeriodoFin() { return periodoFin; }
    public void setPeriodoFin(Instant v) { this.periodoFin = v; }
    public String getEstado() { return estado; }
    public void setEstado(String v) { this.estado = v; }
    public UUID getCreadoPor() { return creadoPor; }
    public void setCreadoPor(UUID v) { this.creadoPor = v; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant v) { this.creadoEn = v; }
}

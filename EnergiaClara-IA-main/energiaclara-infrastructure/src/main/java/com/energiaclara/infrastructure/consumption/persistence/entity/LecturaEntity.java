package com.energiaclara.infrastructure.consumption.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * SKELETON. Mapea {@code consumo.lectura}.
 * TODO equipo Consumption: completar mapeo bidireccional con {@code EnergyReading} domain.
 */
@Entity
@Table(name = "lectura", schema = "consumo")
public class LecturaEntity {

    @Id
    @Column(name = "lectura_id", nullable = false, updatable = false)
    private UUID lecturaId;

    @Column(name = "inquilino_id", nullable = false)
    private UUID inquilinoId;

    @Column(name = "medidor_id", nullable = false)
    private UUID medidorId;

    @Column(name = "valor", nullable = false, precision = 18, scale = 4)
    private BigDecimal valor;

    @Column(name = "unidad", nullable = false, length = 10)
    private String unidad;

    @Column(name = "fecha_lectura", nullable = false)
    private LocalDate fechaLectura;

    @Column(name = "periodo_inicio", nullable = false)
    private Instant periodoInicio;

    @Column(name = "periodo_fin", nullable = false)
    private Instant periodoFin;

    @Column(name = "origen", nullable = false, length = 20)
    private String origen;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "registrada_por", nullable = false)
    private UUID registradaPor;

    @Version
    @Column(name = "version_fila", nullable = false, insertable = false, updatable = false)
    private byte[] versionFila;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    public LecturaEntity() {}

    // TODO equipo: getters/setters para todos los campos
    public UUID getLecturaId() { return lecturaId; }
    public void setLecturaId(UUID v) { this.lecturaId = v; }
    public UUID getInquilinoId() { return inquilinoId; }
    public void setInquilinoId(UUID v) { this.inquilinoId = v; }
    public UUID getMedidorId() { return medidorId; }
    public void setMedidorId(UUID v) { this.medidorId = v; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal v) { this.valor = v; }
    public String getUnidad() { return unidad; }
    public void setUnidad(String v) { this.unidad = v; }
    public LocalDate getFechaLectura() { return fechaLectura; }
    public void setFechaLectura(LocalDate v) { this.fechaLectura = v; }
    public Instant getPeriodoInicio() { return periodoInicio; }
    public void setPeriodoInicio(Instant v) { this.periodoInicio = v; }
    public Instant getPeriodoFin() { return periodoFin; }
    public void setPeriodoFin(Instant v) { this.periodoFin = v; }
    public String getOrigen() { return origen; }
    public void setOrigen(String v) { this.origen = v; }
    public String getEstado() { return estado; }
    public void setEstado(String v) { this.estado = v; }
    public UUID getRegistradaPor() { return registradaPor; }
    public void setRegistradaPor(UUID v) { this.registradaPor = v; }
    public byte[] getVersionFila() { return versionFila; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant v) { this.creadoEn = v; }
}

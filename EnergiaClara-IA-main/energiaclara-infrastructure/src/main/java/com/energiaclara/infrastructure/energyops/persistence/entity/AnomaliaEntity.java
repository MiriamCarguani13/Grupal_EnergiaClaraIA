package com.energiaclara.infrastructure.energyops.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * SKELETON. Mapea {@code energiaops.anomalia}.
 * TODO equipo EnergyOps: completar mapeo bidireccional con {@code EnergyAnomaly} domain.
 */
@Entity
@Table(name = "anomalia", schema = "energiaops")
public class AnomaliaEntity {

    @Id
    @Column(name = "anomalia_id", nullable = false, updatable = false)
    private UUID anomaliaId;

    @Column(name = "inquilino_id", nullable = false)
    private UUID inquilinoId;

    @Column(name = "medidor_id", nullable = false)
    private UUID medidorId;

    @Column(name = "lectura_id")
    private UUID lecturaId;

    @Column(name = "tipo_anomalia", nullable = false, length = 50)
    private String tipoAnomalia;

    @Column(name = "severidad", nullable = false, length = 20)
    private String severidad;

    @Column(name = "puntaje_score", precision = 5, scale = 2)
    private BigDecimal puntajeScore;

    @Column(name = "porcentaje_desviacion", precision = 8, scale = 4)
    private BigDecimal porcentajeDesviacion;

    @Column(name = "explicacion", length = 1000)
    private String explicacion;

    @Column(name = "ia_utilizada", nullable = false)
    private boolean iaUtilizada;

    @Column(name = "version_modelo_ia", length = 50)
    private String versionModeloIa;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "detectada_el", nullable = false)
    private Instant detectadaEl;

    @Column(name = "resuelta_el")
    private Instant resueltaEl;

    @Column(name = "resuelta_por")
    private UUID resueltaPor;

    // Columnas extras DBA (database/seeds.sql) — recomendación IA + cálculo impacto + labels
    @Column(name = "recomendacion", length = 700)
    private String recomendacion;

    @Column(name = "costo_estimado", precision = 12, scale = 2)
    private BigDecimal costoEstimado;

    @Column(name = "co2_estimado", precision = 12, scale = 2)
    private BigDecimal co2Estimado;

    @Column(name = "facility_label", length = 80)
    private String facilityLabel;

    @Column(name = "meter_label", length = 80)
    private String meterLabel;

    @Version
    @Column(name = "version_fila", nullable = false, insertable = false, updatable = false)
    private byte[] versionFila;

    public AnomaliaEntity() {}

    // TODO equipo: getters/setters
    public UUID getAnomaliaId() { return anomaliaId; }
    public void setAnomaliaId(UUID v) { this.anomaliaId = v; }
    public UUID getInquilinoId() { return inquilinoId; }
    public void setInquilinoId(UUID v) { this.inquilinoId = v; }
    public UUID getMedidorId() { return medidorId; }
    public void setMedidorId(UUID v) { this.medidorId = v; }
    public UUID getLecturaId() { return lecturaId; }
    public void setLecturaId(UUID v) { this.lecturaId = v; }
    public String getTipoAnomalia() { return tipoAnomalia; }
    public void setTipoAnomalia(String v) { this.tipoAnomalia = v; }
    public String getSeveridad() { return severidad; }
    public void setSeveridad(String v) { this.severidad = v; }
    public BigDecimal getPuntajeScore() { return puntajeScore; }
    public void setPuntajeScore(BigDecimal v) { this.puntajeScore = v; }
    public BigDecimal getPorcentajeDesviacion() { return porcentajeDesviacion; }
    public void setPorcentajeDesviacion(BigDecimal v) { this.porcentajeDesviacion = v; }
    public String getExplicacion() { return explicacion; }
    public void setExplicacion(String v) { this.explicacion = v; }
    public boolean isIaUtilizada() { return iaUtilizada; }
    public void setIaUtilizada(boolean v) { this.iaUtilizada = v; }
    public String getVersionModeloIa() { return versionModeloIa; }
    public void setVersionModeloIa(String v) { this.versionModeloIa = v; }
    public String getEstado() { return estado; }
    public void setEstado(String v) { this.estado = v; }
    public Instant getDetectadaEl() { return detectadaEl; }
    public void setDetectadaEl(Instant v) { this.detectadaEl = v; }
    public Instant getResueltaEl() { return resueltaEl; }
    public void setResueltaEl(Instant v) { this.resueltaEl = v; }
    public UUID getResueltaPor() { return resueltaPor; }
    public void setResueltaPor(UUID v) { this.resueltaPor = v; }
    public String getRecomendacion() { return recomendacion; }
    public void setRecomendacion(String v) { this.recomendacion = v; }
    public BigDecimal getCostoEstimado() { return costoEstimado; }
    public void setCostoEstimado(BigDecimal v) { this.costoEstimado = v; }
    public BigDecimal getCo2Estimado() { return co2Estimado; }
    public void setCo2Estimado(BigDecimal v) { this.co2Estimado = v; }
    public String getFacilityLabel() { return facilityLabel; }
    public void setFacilityLabel(String v) { this.facilityLabel = v; }
    public String getMeterLabel() { return meterLabel; }
    public void setMeterLabel(String v) { this.meterLabel = v; }
    public byte[] getVersionFila() { return versionFila; }
}

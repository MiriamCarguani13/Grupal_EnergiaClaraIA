package com.energiaclara.infrastructure.energyops.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * SKELETON. Mapea {@code energiaops.snapshot_linea_base}.
 * Incluye columnas extras agregadas por DBA: tolerancia_porcentaje, activo, facility_label, meter_label.
 * TODO equipo EnergyOps: mapper bidireccional con {@code EnergyBaseline} domain.
 */
@Entity
@Table(name = "snapshot_linea_base", schema = "energiaops")
public class SnapshotLineaBaseEntity {

    @Id
    @Column(name = "linea_base_id", nullable = false, updatable = false)
    private UUID lineaBaseId;

    @Column(name = "inquilino_id", nullable = false)
    private UUID inquilinoId;

    @Column(name = "medidor_id", nullable = false)
    private UUID medidorId;

    @Column(name = "tipo_periodo", nullable = false, length = 10)
    private String tipoPeriodo;

    @Column(name = "referencia_inicio", nullable = false)
    private LocalDate referenciaInicio;

    @Column(name = "referencia_fin", nullable = false)
    private LocalDate referenciaFin;

    @Column(name = "valor_promedio", nullable = false, precision = 18, scale = 4)
    private BigDecimal valorPromedio;

    @Column(name = "valor_p95", nullable = false, precision = 18, scale = 4)
    private BigDecimal valorP95;

    @Column(name = "desviacion_estandar", precision = 18, scale = 4)
    private BigDecimal desviacionEstandar;

    @Column(name = "conteo_muestras", nullable = false)
    private int conteoMuestras;

    @Column(name = "calculado_el", nullable = false)
    private Instant calculadoEl;

    // Columnas extras DBA — críticas para EnergyBaselineProviderAdapter
    @Column(name = "tolerancia_porcentaje", precision = 7, scale = 3)
    private BigDecimal toleranciaPorcentaje;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @Column(name = "facility_label", length = 80)
    private String facilityLabel;

    @Column(name = "meter_label", length = 80)
    private String meterLabel;

    public SnapshotLineaBaseEntity() {}

    public UUID getLineaBaseId() { return lineaBaseId; }
    public void setLineaBaseId(UUID v) { this.lineaBaseId = v; }
    public UUID getInquilinoId() { return inquilinoId; }
    public void setInquilinoId(UUID v) { this.inquilinoId = v; }
    public UUID getMedidorId() { return medidorId; }
    public void setMedidorId(UUID v) { this.medidorId = v; }
    public String getTipoPeriodo() { return tipoPeriodo; }
    public void setTipoPeriodo(String v) { this.tipoPeriodo = v; }
    public LocalDate getReferenciaInicio() { return referenciaInicio; }
    public void setReferenciaInicio(LocalDate v) { this.referenciaInicio = v; }
    public LocalDate getReferenciaFin() { return referenciaFin; }
    public void setReferenciaFin(LocalDate v) { this.referenciaFin = v; }
    public BigDecimal getValorPromedio() { return valorPromedio; }
    public void setValorPromedio(BigDecimal v) { this.valorPromedio = v; }
    public BigDecimal getValorP95() { return valorP95; }
    public void setValorP95(BigDecimal v) { this.valorP95 = v; }
    public BigDecimal getDesviacionEstandar() { return desviacionEstandar; }
    public void setDesviacionEstandar(BigDecimal v) { this.desviacionEstandar = v; }
    public int getConteoMuestras() { return conteoMuestras; }
    public void setConteoMuestras(int v) { this.conteoMuestras = v; }
    public Instant getCalculadoEl() { return calculadoEl; }
    public void setCalculadoEl(Instant v) { this.calculadoEl = v; }
    public BigDecimal getToleranciaPorcentaje() { return toleranciaPorcentaje; }
    public void setToleranciaPorcentaje(BigDecimal v) { this.toleranciaPorcentaje = v; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean v) { this.activo = v; }
    public String getFacilityLabel() { return facilityLabel; }
    public void setFacilityLabel(String v) { this.facilityLabel = v; }
    public String getMeterLabel() { return meterLabel; }
    public void setMeterLabel(String v) { this.meterLabel = v; }
}

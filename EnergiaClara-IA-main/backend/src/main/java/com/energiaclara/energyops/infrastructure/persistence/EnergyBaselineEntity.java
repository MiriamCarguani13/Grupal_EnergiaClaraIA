package com.energiaclara.energyops.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "snapshot_linea_base", schema = "energiaops")
@Getter
@Setter
@NoArgsConstructor
public class EnergyBaselineEntity {

    @Id
    @Column(name = "linea_base_id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(name = "inquilino_id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID tenantId;

    @Column(name = "medidor_id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID medidorId;

    @Column(name = "tipo_periodo", nullable = false, length = 10)
    private String tipoPeriodo;

    @Column(name = "referencia_inicio", nullable = false)
    private LocalDate referenciaInicio;

    @Column(name = "referencia_fin", nullable = false)
    private LocalDate referenciaFin;

    @Column(name = "valor_promedio", nullable = false, precision = 18, scale = 4)
    private BigDecimal expectedKwh;

    @Column(name = "valor_p95", nullable = false, precision = 18, scale = 4)
    private BigDecimal valorP95;

    @Column(name = "desviacion_estandar", precision = 18, scale = 4)
    private BigDecimal desviacionEstandar;

    @Column(name = "conteo_muestras", nullable = false)
    private int conteoMuestras;

    @Column(name = "calculado_el", nullable = false)
    private Instant calculadoEl;

    @Column(name = "tolerancia_porcentaje", precision = 7, scale = 3)
    private BigDecimal tolerancePercent;

    @Column(name = "activo", nullable = false)
    private boolean active;

    @Column(name = "facility_label", length = 80)
    private String facilityId;

    @Column(name = "meter_label", length = 80)
    private String meterId;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        if (calculadoEl == null) {
            calculadoEl = now;
        }
        LocalDate today = now.atZone(ZoneOffset.UTC).toLocalDate();
        if (referenciaInicio == null) {
            referenciaInicio = today.minusDays(30);
        }
        if (referenciaFin == null) {
            referenciaFin = today;
        }
        if (tipoPeriodo == null) {
            tipoPeriodo = "DIARIO";
        }
        if (valorP95 == null) {
            valorP95 = expectedKwh;
        }
    }
}

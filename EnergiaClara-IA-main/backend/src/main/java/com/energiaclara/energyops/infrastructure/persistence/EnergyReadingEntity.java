package com.energiaclara.energyops.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "lectura", schema = "consumo")
@Getter
@Setter
@NoArgsConstructor
public class EnergyReadingEntity {

    @Id
    @Column(name = "lectura_id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(name = "inquilino_id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID tenantId;

    @Column(name = "medidor_id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID medidorId;

    @Column(name = "valor", nullable = false, precision = 18, scale = 4)
    private BigDecimal kwh;

    @Column(name = "unidad", nullable = false, length = 10)
    private String unidad;

    @Column(name = "fecha_lectura", nullable = false)
    private LocalDate fechaLectura;

    @Column(name = "periodo_inicio", nullable = false)
    private Instant periodoInicio;

    @Column(name = "periodo_fin", nullable = false)
    private Instant measuredAt;

    @Column(name = "origen", nullable = false, length = 20)
    private String origen;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "registrada_por", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID registradaPor;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "facility_label", length = 80)
    private String facilityId;

    @Column(name = "meter_label", length = 80)
    private String meterId;

    @Column(name = "voltaje", precision = 10, scale = 3)
    private BigDecimal voltage;

    @Column(name = "factor_potencia", precision = 5, scale = 3)
    private BigDecimal powerFactor;

    @Generated(event = { EventType.INSERT, EventType.UPDATE })
    @Column(name = "version_fila", insertable = false, updatable = false)
    private byte[] versionFila;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (measuredAt == null) {
            measuredAt = now;
        }
        if (periodoInicio == null) {
            periodoInicio = measuredAt;
        }
        if (fechaLectura == null) {
            fechaLectura = measuredAt.atZone(ZoneOffset.UTC).toLocalDate();
        }
        if (unidad == null) {
            unidad = "kWh";
        }
        if (origen == null) {
            origen = "API";
        }
        if (estado == null) {
            estado = "REGISTRADA";
        }
    }
}

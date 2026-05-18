package com.energiaclara.energyops.infrastructure.persistence;

import com.energiaclara.energyops.domain.AnomalySeverity;
import com.energiaclara.energyops.domain.AnomalyType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import java.util.UUID;

@Entity
@Table(name = "anomalia", schema = "energiaops")
@Getter
@Setter
@NoArgsConstructor
public class EnergyAnomalyEntity {

    @Id
    @Column(name = "anomalia_id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(name = "inquilino_id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID tenantId;

    @Column(name = "medidor_id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID medidorId;

    @Column(name = "lectura_id", columnDefinition = "uniqueidentifier")
    private UUID readingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_anomalia", nullable = false, length = 30)
    private AnomalyType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "severidad", nullable = false, length = 10)
    private AnomalySeverity severity;

    @Column(name = "puntaje_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal puntajeScore;

    @Column(name = "porcentaje_desviacion", precision = 10, scale = 4)
    private BigDecimal deviationPercent;

    @Column(name = "explicacion", nullable = false, columnDefinition = "nvarchar(max)")
    private String explanation;

    @Column(name = "ia_utilizada", nullable = false)
    private boolean iaUtilizada;

    @Column(name = "version_modelo_ia", length = 50)
    private String versionModeloIa;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "detectada_el", nullable = false)
    private Instant measuredAt;

    @Column(name = "facility_label", length = 80)
    private String facilityId;

    @Column(name = "meter_label", length = 80)
    private String meterId;

    @Column(name = "recomendacion", length = 700)
    private String recommendation;

    @Column(name = "costo_estimado", precision = 12, scale = 2)
    private BigDecimal estimatedCostImpact;

    @Column(name = "co2_estimado", precision = 12, scale = 2)
    private BigDecimal estimatedCo2Impact;

    @Generated(event = { EventType.INSERT, EventType.UPDATE })
    @Column(name = "version_fila", insertable = false, updatable = false)
    private byte[] versionFila;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (measuredAt == null) {
            measuredAt = Instant.now();
        }
        if (estado == null) {
            estado = "ABIERTA";
        }
        if (puntajeScore == null) {
            puntajeScore = BigDecimal.ZERO;
        }
    }
}

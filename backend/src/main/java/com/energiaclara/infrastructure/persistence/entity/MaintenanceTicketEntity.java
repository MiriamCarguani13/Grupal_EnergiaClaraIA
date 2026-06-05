package com.energiaclara.infrastructure.persistence.entity;

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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ticket", schema = "mantenimiento")
@Getter
@Setter
@NoArgsConstructor
public class MaintenanceTicketEntity {

    @Id
    @Column(name = "ticket_id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(name = "inquilino_id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID tenantId;

    @Column(name = "edificio_id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID buildingId;

    @Column(name = "anomalia_id", columnDefinition = "uniqueidentifier")
    private UUID anomalyId;

    @Column(name = "politica_sla_id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID slaPolicyId;

    @Column(name = "titulo", nullable = false, length = 300)
    private String title;

    @Column(name = "descripcion", columnDefinition = "nvarchar(max)")
    private String description;

    @Column(name = "prioridad", nullable = false, length = 10)
    private String priority;

    @Column(name = "estado", nullable = false, length = 20)
    private String status;

    @Column(name = "asignado_a", columnDefinition = "uniqueidentifier")
    private UUID assignedTo;

    @Column(name = "creado_por", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID createdBy;

    @Column(name = "vencimiento_sla", nullable = false)
    private Instant slaDueAt;

    @Column(name = "sla_incumplido", nullable = false)
    private boolean slaBreached;

    @Column(name = "cerrado_el")
    private Instant closedAt;

    @Column(name = "cerrado_por", columnDefinition = "uniqueidentifier")
    private UUID closedBy;

    @Column(name = "conteo_reapertura", nullable = false)
    private int reopenCount;

    @Column(name = "creado_en", nullable = false)
    private Instant createdAt;

    @Column(name = "actualizado_en", nullable = false)
    private Instant updatedAt;

    @Column(name = "diagnostico_tecnico", columnDefinition = "nvarchar(max)")
    private String technicalDiagnosis;

    @Column(name = "solucion_aplicada", columnDefinition = "nvarchar(max)")
    private String appliedSolution;

    @Column(name = "materiales_utilizados", columnDefinition = "nvarchar(max)")
    private String usedMaterials;

    @Column(name = "observaciones_tecnicas", columnDefinition = "nvarchar(max)")
    private String technicalNotes;

    @Column(name = "estado_tecnico", length = 30)
    private String technicalStatus;

    @Column(name = "atendido_el")
    private Instant attendedAt;

    @Column(name = "tecnico_responsable_id", columnDefinition = "uniqueidentifier")
    private UUID responsibleTechnicianId;

    @Column(name = "evidencia_imagen_url", length = 1000)
    private String evidenceImageUrl;

    @Generated(event = { EventType.INSERT, EventType.UPDATE })
    @Column(name = "version_fila", insertable = false, updatable = false)
    private byte[] versionFila;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (slaDueAt == null) {
            slaDueAt = now.plusSeconds(24 * 60 * 60L);
        }
        if (status == null) {
            status = "PENDIENTE";
        }
        if (priority == null) {
            priority = "MEDIA";
        }
    }
}

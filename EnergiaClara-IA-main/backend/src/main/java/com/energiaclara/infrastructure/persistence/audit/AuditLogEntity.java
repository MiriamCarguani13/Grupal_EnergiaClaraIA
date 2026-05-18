package com.energiaclara.infrastructure.persistence.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "evento_auditoria", schema = "audit")
@Getter
@Setter
@NoArgsConstructor
public class AuditLogEntity {

    @Id
    @Column(name = "evento_id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(name = "inquilino_id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID tenantId;

    @Column(name = "actor_id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID userId;

    @Column(name = "accion", nullable = false, length = 80)
    private String action;

    @Column(name = "tipo_recurso", nullable = false, length = 50)
    private String entityName;

    @Column(name = "recurso_id", nullable = false, length = 50)
    private String entityId;

    @Column(name = "hash_anterior", length = 64)
    private String hashAnterior;

    @Column(name = "hash_posterior", length = 64)
    private String hashPosterior;

    @Column(name = "direccion_ip", length = 45)
    private String ipAddress;

    @Column(name = "id_correlacion", columnDefinition = "uniqueidentifier")
    private UUID correlationId;

    @Column(name = "severidad", nullable = false, length = 10)
    private String severidad;

    @Column(name = "ocurrido_el", nullable = false)
    private Instant occurredAt;

    // ── Columnas extendidas (ALTER en seeds.sql) ──
    @Column(name = "metodo_http", length = 10)
    private String httpMethod;

    @Column(name = "endpoint", length = 500)
    private String endpoint;

    @Column(name = "user_email", length = 255)
    private String userEmail;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "estado", length = 20)
    private String status;

    @Column(name = "mensaje_error", columnDefinition = "nvarchar(max)")
    private String errorMessage;

    @Column(name = "duracion_ms")
    private Long durationMs;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
        if (severidad == null) {
            severidad = "INFO";
        }
        if (entityId == null || entityId.isBlank()) {
            entityId = "N/A";
        }
        if (entityName == null || entityName.isBlank()) {
            entityName = "N/A";
        }
    }
}

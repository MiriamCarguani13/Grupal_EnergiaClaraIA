package com.energiaclara.infrastructure.persistence.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "log_cambios", schema = "audit")
@Getter
@Setter
@NoArgsConstructor
public class AuditChangeEntity {

    @Id
    @Column(name = "cambio_id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(name = "evento_id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID eventId;

    @Column(name = "inquilino_id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID tenantId;

    @Column(name = "nombre_campo", nullable = false, length = 100)
    private String fieldName;

    @Column(name = "valor_anterior", columnDefinition = "nvarchar(max)")
    private String previousValue;

    @Column(name = "valor_nuevo", columnDefinition = "nvarchar(max)")
    private String newValue;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}

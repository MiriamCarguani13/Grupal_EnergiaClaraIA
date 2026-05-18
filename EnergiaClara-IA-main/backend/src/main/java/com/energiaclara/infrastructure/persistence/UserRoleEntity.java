package com.energiaclara.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usuario_rol", schema = "iam",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_usuario_rol_alcance",
                columnNames = {"usuario_id", "rol_id", "inquilino_id", "edificio_id"}))
@Getter
@Setter
@NoArgsConstructor
public class UserRoleEntity {

    @Id
    @Column(name = "usuario_rol_id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(name = "usuario_id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID userId;

    @Column(name = "rol_id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID roleId;

    @Column(name = "inquilino_id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID tenantId;

    @Column(name = "edificio_id", columnDefinition = "uniqueidentifier")
    private UUID buildingId;

    @Column(name = "asignado_el", nullable = false)
    private Instant assignedAt;

    @Column(name = "asignado_por", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID assignedBy;
}

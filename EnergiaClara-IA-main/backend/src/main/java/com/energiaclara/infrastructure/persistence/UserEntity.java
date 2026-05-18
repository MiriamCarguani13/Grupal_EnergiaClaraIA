package com.energiaclara.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usuario", schema = "iam")
@Getter
@Setter
@NoArgsConstructor
public class UserEntity {

    @Id
    @Column(name = "usuario_id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(name = "inquilino_id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID tenantId;

    @Column(name = "correo", nullable = false, length = 200)
    private String email;

    @Column(name = "nombre_completo", nullable = false, length = 200)
    private String fullName;

    @Column(name = "contrasena_hash", nullable = false, length = 500)
    private String hashedPassword;

    @Column(name = "esta_activo", nullable = false)
    private boolean active;

    @Column(name = "ultimo_ingreso_el")
    private Instant lastLoginAt;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "actualizado_en", nullable = false)
    private Instant updatedAt;
}

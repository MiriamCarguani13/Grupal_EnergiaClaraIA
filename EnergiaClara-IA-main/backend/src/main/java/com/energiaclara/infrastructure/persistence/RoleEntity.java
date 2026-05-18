package com.energiaclara.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "rol", schema = "iam")
@Getter
@Setter
@NoArgsConstructor
public class RoleEntity {

    @Id
    @Column(name = "rol_id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "descripcion", length = 300)
    private String description;

    @Column(name = "nivel_alcance", nullable = false, length = 30)
    private String scope;
}

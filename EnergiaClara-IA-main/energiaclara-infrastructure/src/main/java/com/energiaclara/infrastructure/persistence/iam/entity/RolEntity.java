package com.energiaclara.infrastructure.persistence.iam.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "rol", schema = "iam")
public class RolEntity {

    @Id
    @Column(name = "rol_id", nullable = false, updatable = false)
    private UUID rolId;

    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    @Column(name = "descripcion", length = 300)
    private String descripcion;

    @Column(name = "nivel_alcance", nullable = false, length = 30)
    private String nivelAlcance;

    public RolEntity() {}

    public UUID getRolId() { return rolId; }
    public void setRolId(UUID v) { this.rolId = v; }
    public String getNombre() { return nombre; }
    public void setNombre(String v) { this.nombre = v; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String v) { this.descripcion = v; }
    public String getNivelAlcance() { return nivelAlcance; }
    public void setNivelAlcance(String v) { this.nivelAlcance = v; }
}

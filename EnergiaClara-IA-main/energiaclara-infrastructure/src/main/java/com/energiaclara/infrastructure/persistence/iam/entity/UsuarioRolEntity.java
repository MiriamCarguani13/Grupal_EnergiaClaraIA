package com.energiaclara.infrastructure.persistence.iam.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usuario_rol", schema = "iam")
public class UsuarioRolEntity {

    @Id
    @Column(name = "usuario_rol_id", nullable = false, updatable = false)
    private UUID usuarioRolId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "rol_id", nullable = false)
    private UUID rolId;

    @Column(name = "inquilino_id", nullable = false)
    private UUID inquilinoId;

    @Column(name = "edificio_id")
    private UUID edificioId;

    @Column(name = "asignado_el", nullable = false)
    private Instant asignadoEl;

    @Column(name = "asignado_por", nullable = false)
    private UUID asignadoPor;

    public UsuarioRolEntity() {}

    public UUID getUsuarioRolId() { return usuarioRolId; }
    public void setUsuarioRolId(UUID v) { this.usuarioRolId = v; }
    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID v) { this.usuarioId = v; }
    public UUID getRolId() { return rolId; }
    public void setRolId(UUID v) { this.rolId = v; }
    public UUID getInquilinoId() { return inquilinoId; }
    public void setInquilinoId(UUID v) { this.inquilinoId = v; }
    public UUID getEdificioId() { return edificioId; }
    public void setEdificioId(UUID v) { this.edificioId = v; }
    public Instant getAsignadoEl() { return asignadoEl; }
    public void setAsignadoEl(Instant v) { this.asignadoEl = v; }
    public UUID getAsignadoPor() { return asignadoPor; }
    public void setAsignadoPor(UUID v) { this.asignadoPor = v; }
}

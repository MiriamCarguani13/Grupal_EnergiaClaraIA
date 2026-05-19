package com.energiaclara.infrastructure.persistence.iam.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usuario", schema = "iam")
public class UsuarioEntity {

    @Id
    @Column(name = "usuario_id", nullable = false, updatable = false)
    private UUID usuarioId;

    @Column(name = "inquilino_id", nullable = false)
    private UUID inquilinoId;

    @Column(name = "correo", nullable = false, length = 200)
    private String correo;

    @Column(name = "nombre_completo", nullable = false, length = 200)
    private String nombreCompleto;

    @Column(name = "contrasena_hash", nullable = false, length = 500)
    private String contrasenaHash;

    @Column(name = "esta_activo", nullable = false)
    private boolean estaActivo;

    @Column(name = "ultimo_ingreso_el")
    private Instant ultimoIngresoEl;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    public UsuarioEntity() {}

    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID v) { this.usuarioId = v; }
    public UUID getInquilinoId() { return inquilinoId; }
    public void setInquilinoId(UUID v) { this.inquilinoId = v; }
    public String getCorreo() { return correo; }
    public void setCorreo(String v) { this.correo = v; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String v) { this.nombreCompleto = v; }
    public String getContrasenaHash() { return contrasenaHash; }
    public void setContrasenaHash(String v) { this.contrasenaHash = v; }
    public boolean isEstaActivo() { return estaActivo; }
    public void setEstaActivo(boolean v) { this.estaActivo = v; }
    public Instant getUltimoIngresoEl() { return ultimoIngresoEl; }
    public void setUltimoIngresoEl(Instant v) { this.ultimoIngresoEl = v; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant v) { this.creadoEn = v; }
    public Instant getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(Instant v) { this.actualizadoEn = v; }
}

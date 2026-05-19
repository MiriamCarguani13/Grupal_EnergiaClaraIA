package com.energiaclara.iam.domain;

import com.energiaclara.core.domain.shared.Email;
import com.energiaclara.core.domain.shared.DomainException;
import com.energiaclara.core.domain.shared.TenantId;
import com.energiaclara.core.domain.shared.UserId;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public class IamUser {
    private final UserId id;
    private final TenantId tenantId;
    private final Email email;
    private final String fullName;
    private final String hashedPassword;
    private final Set<Role> roles;
    private boolean active;
    private final Instant createdAt;

    private IamUser(UserId id, TenantId tenantId, Email email, String fullName, String hashedPassword, Set<Role> roles, boolean active, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "UserId no puede ser nulo");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId no puede ser nulo");
        this.email = Objects.requireNonNull(email, "Email no puede ser nulo");
        this.fullName = validateFullName(fullName);
        this.hashedPassword = validateHashedPassword(hashedPassword);
        this.roles = Set.copyOf(Objects.requireNonNull(roles, "Roles no puede ser nulo"));
        this.active = active;
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt no puede ser nulo");
        if (this.roles.isEmpty()) {
            throw new DomainException("El usuario IAM debe tener al menos un rol");
        }
    }

    public static IamUser register(TenantId tenantId, Email email, String fullName, String hashedPassword, Set<Role> roles) {
        return new IamUser(UserId.generate(), tenantId, email, fullName, hashedPassword, roles, true, Instant.now());
    }

    public static IamUser hydrate(UserId id, TenantId tenantId, Email email, String fullName, String hashedPassword, Set<Role> roles, boolean active, Instant createdAt) {
        return new IamUser(id, tenantId, email, fullName, hashedPassword, roles, active, createdAt);
    }

    public AuthenticatedPrincipal authenticate() {
        if (!active) {
            throw new DomainException("Usuario IAM inactivo: " + id.value());
        }
        return new AuthenticatedPrincipal(id, tenantId, email, roles);
    }

    public void deactivate() {
        if (!active) {
            throw new DomainException("Usuario IAM ya está inactivo: " + id.value());
        }
        this.active = false;
    }

    private static String validateFullName(String fullName) {
        Objects.requireNonNull(fullName, "El nombre completo no puede ser nulo");
        String trimmed = fullName.trim();
        if (trimmed.length() < 3) {
            throw new DomainException("El nombre completo debe tener al menos 3 caracteres");
        }
        return trimmed;
    }

    private static String validateHashedPassword(String hashedPassword) {
        Objects.requireNonNull(hashedPassword, "La contraseña hash no puede ser nula");
        if (hashedPassword.isBlank()) {
            throw new DomainException("La contraseña hash no puede estar vacía");
        }
        return hashedPassword;
    }

    public UserId getId() { return id; }
    public TenantId getTenantId() { return tenantId; }
    public Email getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getHashedPassword() { return hashedPassword; }
    public Set<Role> getRoles() { return roles; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
}

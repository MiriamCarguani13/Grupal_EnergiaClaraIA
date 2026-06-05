package com.energiaclara.iam.domain.model;

import com.energiaclara.core.model.vo.TenantId;
import com.energiaclara.core.model.vo.UserId;
import com.energiaclara.iam.domain.model.vo.Email;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class User {

    private final UserId id;
    private final TenantId tenantId;
    private final Email email;
    private final String fullName;
    private final String hashedPassword;
    private final Set<Role> roles;
    private boolean active;
    private final Instant createdAt;

    public User(UserId id, TenantId tenantId, Email email, String fullName,
                String hashedPassword, Set<Role> roles, boolean active, Instant createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.email = email;
        this.fullName = fullName;
        this.hashedPassword = hashedPassword;
        this.roles = new HashSet<>(roles);
        this.active = active;
        this.createdAt = createdAt;
    }

    public static User create(TenantId tenantId, Email email, String fullName,
                              String hashedPassword, Set<Role> roles) {
        return new User(UserId.generate(), tenantId, email, fullName, hashedPassword, roles, true, Instant.now());
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    public UserId getId() { return id; }
    public TenantId getTenantId() { return tenantId; }
    public Email getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getHashedPassword() { return hashedPassword; }
    public Set<Role> getRoles() { return Set.copyOf(roles); }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
}

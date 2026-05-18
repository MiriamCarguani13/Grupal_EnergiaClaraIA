package com.energiaclara.iam.domain;

import com.energiaclara.core.domain.shared.Email;
import com.energiaclara.core.domain.shared.TenantId;
import com.energiaclara.core.domain.shared.UserId;

import java.util.Set;

public record AuthenticatedPrincipal(UserId userId, TenantId tenantId, Email email, Set<Role> roles) {
    public AuthenticatedPrincipal {
        roles = Set.copyOf(roles);
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }
}

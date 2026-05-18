package com.energiaclara.iam.application.dto;

import com.energiaclara.core.domain.shared.TenantId;
import com.energiaclara.core.domain.shared.UserId;
import com.energiaclara.iam.domain.Role;

import java.util.Set;

public record LoginResult(String token, UserId userId, TenantId tenantId, Set<Role> roles) {
}

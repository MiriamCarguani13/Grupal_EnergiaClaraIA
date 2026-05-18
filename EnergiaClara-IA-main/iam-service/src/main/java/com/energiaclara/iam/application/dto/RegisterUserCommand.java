package com.energiaclara.iam.application.dto;

import com.energiaclara.core.domain.shared.Email;
import com.energiaclara.core.domain.shared.TenantId;
import com.energiaclara.core.domain.shared.UserId;
import com.energiaclara.iam.domain.Role;

import java.util.Set;

public record RegisterUserCommand(TenantId tenantId, Email email, String fullName, String rawPassword, Set<Role> roles, UserId assignedBy) {
}

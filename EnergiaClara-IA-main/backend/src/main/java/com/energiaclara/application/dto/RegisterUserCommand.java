package com.energiaclara.application.dto;

import com.energiaclara.domain.model.Role;
import com.energiaclara.domain.model.vo.Email;
import com.energiaclara.domain.model.vo.TenantId;
import com.energiaclara.domain.model.vo.UserId;

import java.util.Set;

public record RegisterUserCommand(
        TenantId tenantId,
        Email email,
        String fullName,
        String rawPassword,
        Set<Role> roles,
        UserId assignedBy
) {}

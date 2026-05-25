package com.energiaclara.application.dto;

import java.util.Set;

public record RegisterUserCommand(
        String tenantId,
        String email,
        String fullName,
        String rawPassword,
        Set<String> roles,
        String assignedBy
) {}

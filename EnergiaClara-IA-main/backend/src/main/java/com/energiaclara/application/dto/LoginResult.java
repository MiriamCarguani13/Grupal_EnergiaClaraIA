package com.energiaclara.application.dto;

import com.energiaclara.domain.model.Role;

import java.util.Set;

public record LoginResult(String token, String userId, String tenantId, Set<Role> roles) {}

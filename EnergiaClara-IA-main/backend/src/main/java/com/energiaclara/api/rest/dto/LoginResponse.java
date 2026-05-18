package com.energiaclara.api.rest.dto;

import com.energiaclara.iam.domain.Role;

import java.util.Set;

public record LoginResponse(String token, String userId, String tenantId, Set<Role> roles) {}

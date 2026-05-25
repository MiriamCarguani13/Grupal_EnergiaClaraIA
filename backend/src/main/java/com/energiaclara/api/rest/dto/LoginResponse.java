package com.energiaclara.api.rest.dto;

import java.util.Set;

public record LoginResponse(String token, String userId, String tenantId, Set<String> roles) {}

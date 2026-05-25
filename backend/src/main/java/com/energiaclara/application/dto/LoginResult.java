package com.energiaclara.application.dto;

import java.util.Set;

public record LoginResult(String token, String userId, String tenantId, Set<String> roles) {}

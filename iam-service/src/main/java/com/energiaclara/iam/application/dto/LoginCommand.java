package com.energiaclara.iam.application.dto;

public record LoginCommand(String email, String password, String tenantId) {}

package com.energiaclara.infrastructure.api.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LoginRequestDto(
        @NotNull UUID tenantId,
        @NotBlank @Email String email,
        @NotBlank String password
) {
}

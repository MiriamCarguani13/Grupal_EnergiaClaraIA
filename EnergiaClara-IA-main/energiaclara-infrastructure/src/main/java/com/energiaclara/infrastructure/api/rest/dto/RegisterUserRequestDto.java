package com.energiaclara.infrastructure.api.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record RegisterUserRequestDto(
        @NotNull UUID tenantId,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 3, max = 200) String fullName,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotEmpty List<String> roles
) {
}

package com.energiaclara.api.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record RegisterRequest(
        @NotBlank String tenantId,
        @Email @NotBlank String email,
        @NotBlank @Size(min = 3, max = 200) String fullName,
        @NotBlank @Size(min = 8) String password,
        @NotEmpty Set<String> roles
) {}

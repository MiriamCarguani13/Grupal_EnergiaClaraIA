package com.energiaclara.core.domain.tenant;

import com.energiaclara.core.domain.shared.DomainException;

import java.util.Objects;

public record TenantName(String value) {
    public TenantName {
        Objects.requireNonNull(value, "El nombre del tenant no puede ser nulo");
        value = value.trim();
        if (value.length() < 3) {
            throw new DomainException("El nombre del tenant debe tener al menos 3 caracteres");
        }
        if (value.length() > 120) {
            throw new DomainException("El nombre del tenant no puede superar 120 caracteres");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}

package com.energiaclara.core.domain.energy;

import com.energiaclara.core.domain.shared.DomainException;

public class InvalidKwhValueException extends DomainException {
    public InvalidKwhValueException(String message) {
        super(message);
    }
}

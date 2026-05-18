package com.energiaclara.core.domain.energy;

import com.energiaclara.core.domain.shared.DomainException;

public class AnomalyAlreadyAcknowledgedException extends DomainException {
    public AnomalyAlreadyAcknowledgedException(AnomalyId anomalyId) {
        super("La anomalía ya fue reconocida: " + anomalyId.value());
    }
}

package com.energiaclara.core.domain.shared;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredAt();
}

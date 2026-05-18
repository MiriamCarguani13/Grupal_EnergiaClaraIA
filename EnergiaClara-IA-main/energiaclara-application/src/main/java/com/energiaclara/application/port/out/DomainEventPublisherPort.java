package com.energiaclara.application.port.out;

import com.energiaclara.core.domain.shared.DomainEvent;

import java.util.List;

public interface DomainEventPublisherPort {
    void publish(List<DomainEvent> events);
}

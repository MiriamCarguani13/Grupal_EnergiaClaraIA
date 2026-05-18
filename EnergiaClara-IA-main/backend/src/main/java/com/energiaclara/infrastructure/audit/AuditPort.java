package com.energiaclara.infrastructure.audit;

public interface AuditPort {
    void record(AuditEvent event);
}

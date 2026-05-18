package com.energiaclara.application.port.out;

import com.energiaclara.domain.model.audit.AuditEvent;

public interface AuditPort {
    void record(AuditEvent event);
}

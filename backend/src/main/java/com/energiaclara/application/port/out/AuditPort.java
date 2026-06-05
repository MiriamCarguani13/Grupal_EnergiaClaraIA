package com.energiaclara.application.port.out;

import com.energiaclara.core.audit.AuditEvent;

public interface AuditPort {
    void record(AuditEvent event);
}

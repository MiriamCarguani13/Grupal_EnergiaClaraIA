package com.energiaclara.domain.model.audit;

import com.energiaclara.domain.model.vo.TenantId;
import com.energiaclara.domain.model.vo.UserId;

import java.time.Instant;

public record AuditEvent(
        TenantId tenantId,
        UserId userId,
        String userEmail,
        String action,
        String entityName,
        String entityId,
        String httpMethod,
        String endpoint,
        String previousStateJson,
        String newStateJson,
        String ipAddress,
        String userAgent,
        AuditStatus status,
        String errorMessage,
        long durationMs,
        Instant occurredAt
) {
    public enum AuditStatus { SUCCESS, FAILURE }
}

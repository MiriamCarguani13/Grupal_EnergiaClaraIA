package com.energiaclara.infrastructure.persistence.audit;

import com.energiaclara.application.port.out.AuditPort;
import com.energiaclara.domain.model.audit.AuditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuditAdapter implements AuditPort {

    private static final Logger log = LoggerFactory.getLogger(AuditAdapter.class);
    private static final UUID SYSTEM_UUID = new UUID(0L, 0L);

    private final AuditLogJpaRepository eventRepo;
    private final AuditChangeJpaRepository changeRepo;
    private final UUID fallbackTenantId;

    public AuditAdapter(AuditLogJpaRepository eventRepo,
                        AuditChangeJpaRepository changeRepo,
                        @Value("${app.energyops.demo-tenant-id:11111111-1111-1111-1111-111111111111}") UUID fallbackTenantId) {
        this.eventRepo = eventRepo;
        this.changeRepo = changeRepo;
        this.fallbackTenantId = fallbackTenantId;
    }

    @Override
    @Async("auditExecutor")
    public void record(AuditEvent event) {
        try {
            AuditLogEntity entity = new AuditLogEntity();
            UUID tenant = event.tenantId() != null ? event.tenantId().value() : fallbackTenantId;
            UUID actor  = event.userId()   != null ? event.userId().value()   : SYSTEM_UUID;

            entity.setTenantId(tenant);
            entity.setUserId(actor);
            entity.setAction(event.action());
            entity.setEntityName(blankToFallback(event.entityName(), "N/A"));
            entity.setEntityId(blankToFallback(event.entityId(), "N/A"));
            entity.setIpAddress(event.ipAddress());
            entity.setSeveridad(event.status() == AuditEvent.AuditStatus.SUCCESS ? "INFO" : "ERROR");
            entity.setOccurredAt(event.occurredAt());

            entity.setHttpMethod(event.httpMethod());
            entity.setEndpoint(event.endpoint());
            entity.setUserEmail(event.userEmail());
            entity.setUserAgent(event.userAgent());
            entity.setStatus(event.status().name());
            entity.setErrorMessage(event.errorMessage());
            entity.setDurationMs(event.durationMs());

            AuditLogEntity saved = eventRepo.save(entity);

            persistChange(saved, "new_state", null, event.newStateJson());
            persistChange(saved, "previous_state", null, event.previousStateJson());
        } catch (Exception ex) {
            log.error("Fallo persistiendo evento_auditoria action={} entity={} endpoint={}",
                    event.action(), event.entityName(), event.endpoint(), ex);
        }
    }

    private void persistChange(AuditLogEntity event, String fieldName, String previous, String next) {
        if (previous == null && next == null) return;
        try {
            AuditChangeEntity change = new AuditChangeEntity();
            change.setEventId(event.getId());
            change.setTenantId(event.getTenantId());
            change.setFieldName(fieldName);
            change.setPreviousValue(previous);
            change.setNewValue(next);
            changeRepo.save(change);
        } catch (Exception ex) {
            log.error("Fallo persistiendo log_cambios eventId={} field={}", event.getId(), fieldName, ex);
        }
    }

    private static String blankToFallback(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}

package com.energiaclara.infrastructure.audit;

import com.energiaclara.infrastructure.security.context.CorrelationIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Service
public class AuditTrailService {

    private static final Logger log = LoggerFactory.getLogger(AuditTrailService.class);

    // Columnas core canónicas. Columnas extras (metodo_http, endpoint, user_email,
    // user_agent, estado, mensaje_error, duracion_ms) agregadas por DBA seeds.sql
    // quedan NULL — uso reservado para audit AOP estilo HTTP (futuro módulo).
    private static final String INSERT_SQL = """
            INSERT INTO audit.evento_auditoria
                (evento_id, inquilino_id, actor_id, accion, tipo_recurso, recurso_id,
                 hash_anterior, hash_posterior, direccion_ip, id_correlacion, severidad, ocurrido_el)
            VALUES (?, ?, ?, ?, ?, ?, NULL, NULL, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbc;

    public AuditTrailService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void record(String accion, String tipoRecurso, String recursoId, UUID tenantId, UUID actorId, String severidad, String ipAddress) {
        try {
            UUID eventoId = UUID.randomUUID();
            UUID idCorrelacion = parseCorrelationId();
            jdbc.update(INSERT_SQL,
                    eventoId,
                    tenantId,
                    actorId,
                    accion,
                    tipoRecurso,
                    recursoId,
                    ipAddress,
                    idCorrelacion,
                    severidad,
                    Timestamp.from(Instant.now())
            );
        } catch (Exception ex) {
            log.warn("No se pudo registrar auditoría accion={} recursoId={}: {}", accion, recursoId, ex.getMessage());
        }
    }

    public void record(String accion, String tipoRecurso, String recursoId, UUID tenantId, UUID actorId) {
        record(accion, tipoRecurso, recursoId, tenantId, actorId, "MEDIA", null);
    }

    private UUID parseCorrelationId() {
        String cid = CorrelationIdContext.get();
        if (cid == null) return null;
        try {
            return UUID.fromString(cid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

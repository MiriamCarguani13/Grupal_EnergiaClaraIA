package com.energiaclara.infrastructure.api.rest;

import com.energiaclara.core.domain.shared.DomainException;
import com.energiaclara.iam.domain.AuthenticatedPrincipal;
import com.energiaclara.infrastructure.api.rest.dto.AnomalyItemDto;
import com.energiaclara.infrastructure.api.rest.dto.DashboardResponseDto;
import com.energiaclara.infrastructure.api.rest.dto.KpiItemDto;
import com.energiaclara.infrastructure.config.security.SecurityConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "KPIs, dashboard ejecutivo, anomalías")
@SecurityRequirement(name = "bearer-jwt")
public class AnalyticsController {

    private final JdbcTemplate jdbc;

    public AnalyticsController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard ejecutivo del tenant")
    public ResponseEntity<DashboardResponseDto> dashboard() {
        UUID tenantId = currentTenantId();

        Long totalReadings = jdbc.queryForObject(
                "SELECT COUNT(*) FROM consumo.lectura WHERE inquilino_id = ?",
                Long.class, tenantId);
        Long totalAnomalies = jdbc.queryForObject(
                "SELECT COUNT(*) FROM energiaops.anomalia WHERE inquilino_id = ?",
                Long.class, tenantId);
        Long ticketsOpen = jdbc.queryForObject(
                "SELECT COUNT(*) FROM mantenimiento.ticket WHERE inquilino_id = ? AND estado IN ('ABIERTO','ASIGNADO','EN_PROCESO')",
                Long.class, tenantId);

        List<KpiItemDto> kpis = jdbc.query(
                "SELECT TOP 20 lectura_id, periodo_fin, valor FROM consumo.lectura " +
                        "WHERE inquilino_id = ? ORDER BY periodo_fin DESC",
                (rs, i) -> new KpiItemDto(
                        rs.getObject("lectura_id", UUID.class),
                        rs.getTimestamp("periodo_fin") != null ? rs.getTimestamp("periodo_fin").toInstant() : null,
                        rs.getBigDecimal("valor"),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                ),
                tenantId);

        List<AnomalyItemDto> anomalies = jdbc.query(
                "SELECT TOP 10 a.anomalia_id, a.medidor_id, a.severidad, a.estado, " +
                        "       a.porcentaje_desviacion, a.detectada_el, m.edificio_id " +
                        "FROM energiaops.anomalia a " +
                        "LEFT JOIN core.medidor m ON m.medidor_id = a.medidor_id " +
                        "WHERE a.inquilino_id = ? ORDER BY a.detectada_el DESC",
                (rs, i) -> {
                    Timestamp ts = rs.getTimestamp("detectada_el");
                    Object edificio = rs.getObject("edificio_id");
                    Object medidor = rs.getObject("medidor_id");
                    return new AnomalyItemDto(
                            rs.getObject("anomalia_id", UUID.class),
                            edificio != null ? edificio.toString() : null,
                            medidor != null ? medidor.toString() : null,
                            mapSeverity(rs.getString("severidad")),
                            rs.getString("estado"),
                            rs.getBigDecimal("porcentaje_desviacion"),
                            ts != null ? ts.toInstant() : null
                    );
                },
                tenantId);

        return ResponseEntity.ok(new DashboardResponseDto(
                totalReadings == null ? 0 : totalReadings,
                totalAnomalies == null ? 0 : totalAnomalies,
                ticketsOpen == null ? 0 : ticketsOpen,
                kpis == null ? new ArrayList<>() : kpis,
                anomalies == null ? new ArrayList<>() : anomalies
        ));
    }

    @GetMapping("/kpis")
    @Operation(summary = "Lista de KPIs por lectura (sin agregación)")
    public ResponseEntity<List<KpiItemDto>> kpis() {
        UUID tenantId = currentTenantId();
        List<KpiItemDto> rows = jdbc.query(
                "SELECT TOP 50 lectura_id, periodo_fin, valor FROM consumo.lectura " +
                        "WHERE inquilino_id = ? ORDER BY periodo_fin DESC",
                (rs, i) -> new KpiItemDto(
                        rs.getObject("lectura_id", UUID.class),
                        rs.getTimestamp("periodo_fin") != null ? rs.getTimestamp("periodo_fin").toInstant() : null,
                        rs.getBigDecimal("valor"),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                ),
                tenantId);
        return ResponseEntity.ok(rows);
    }

    @GetMapping("/anomalies")
    @Operation(summary = "Lista de anomalías recientes")
    public ResponseEntity<List<AnomalyItemDto>> anomalies() {
        UUID tenantId = currentTenantId();
        List<AnomalyItemDto> rows = jdbc.query(
                "SELECT TOP 50 a.anomalia_id, a.medidor_id, a.severidad, a.estado, " +
                        "       a.porcentaje_desviacion, a.detectada_el, m.edificio_id " +
                        "FROM energiaops.anomalia a " +
                        "LEFT JOIN core.medidor m ON m.medidor_id = a.medidor_id " +
                        "WHERE a.inquilino_id = ? ORDER BY a.detectada_el DESC",
                (rs, i) -> {
                    Timestamp ts = rs.getTimestamp("detectada_el");
                    Object edificio = rs.getObject("edificio_id");
                    Object medidor = rs.getObject("medidor_id");
                    return new AnomalyItemDto(
                            rs.getObject("anomalia_id", UUID.class),
                            edificio != null ? edificio.toString() : null,
                            medidor != null ? medidor.toString() : null,
                            mapSeverity(rs.getString("severidad")),
                            rs.getString("estado"),
                            rs.getBigDecimal("porcentaje_desviacion"),
                            ts != null ? ts.toInstant() : null
                    );
                },
                tenantId);
        return ResponseEntity.ok(rows);
    }

    private UUID currentTenantId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof SecurityConfig.PrincipalAuthentication pa) {
            AuthenticatedPrincipal p = pa.getAuthenticatedPrincipal();
            return p.tenantId().value();
        }
        throw new DomainException("No hay principal autenticado en el contexto");
    }

    private static String mapSeverity(String dbValue) {
        if (dbValue == null) return null;
        return switch (dbValue.toUpperCase()) {
            case "CRITICA", "CRITICAL" -> "CRITICAL";
            case "ALTA", "HIGH" -> "HIGH";
            case "MEDIA", "MEDIUM" -> "MEDIUM";
            case "BAJA", "LOW" -> "LOW";
            default -> dbValue;
        };
    }
}

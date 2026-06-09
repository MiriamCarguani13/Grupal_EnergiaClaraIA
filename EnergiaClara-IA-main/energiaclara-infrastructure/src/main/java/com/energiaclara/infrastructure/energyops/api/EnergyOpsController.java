package com.energiaclara.infrastructure.energyops.api;

import com.energiaclara.ai.application.EnergyAiAnalysisCommand;
import com.energiaclara.ai.application.EnergyAiAnalysisResponse;
import com.energiaclara.ai.application.port.in.AnalyzeEnergyWithAiUseCase;
import com.energiaclara.core.domain.shared.DomainException;
import com.energiaclara.iam.domain.AuthenticatedPrincipal;
import com.energiaclara.infrastructure.config.security.SecurityConfig;
import com.energiaclara.infrastructure.energyops.api.dto.AnalyzeReadingRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Bounded context EnergyOps (core domain).
 * {@code analyze-reading} ya cablea el motor AI híbrido (baseline dinámico + score estadístico + recomendación).
 * El resto de endpoints siguen como skeleton del arquitecto.
 */
@RestController
@RequestMapping("/api/energyops")
@Tag(name = "EnergyOps", description = "Detección de anomalías y baselines")
@SecurityRequirement(name = "bearer-jwt")
public class EnergyOpsController {

    private final AnalyzeEnergyWithAiUseCase analyzeEnergyWithAi;

    public EnergyOpsController(AnalyzeEnergyWithAiUseCase analyzeEnergyWithAi) {
        this.analyzeEnergyWithAi = analyzeEnergyWithAi;
    }

    @PostMapping("/analyze-reading")
    @Operation(summary = "Analizar lectura con motor AI híbrido (baseline dinámico + anomalía + recomendación)")
    public ResponseEntity<EnergyAiAnalysisResponse> analyzeReading(@Valid @RequestBody AnalyzeReadingRequestDto req) {
        UUID tenantId = currentTenantId();
        EnergyAiAnalysisCommand command = new EnergyAiAnalysisCommand(
                tenantId,
                req.meterId(),
                null,                               // facilityLabel — opcional
                null,                               // meterLabel — opcional
                req.timestamp(),
                BigDecimal.valueOf(req.kwhValue()),
                null,                               // staticBaselineKwh — el motor cae a historial/lectura
                null,                               // tolerancePercent — default 15%
                null,                               // costPerKwh
                null,                               // co2KgPerKwh
                null,                               // voltage
                null                                // powerFactor
        );
        return ResponseEntity.ok(analyzeEnergyWithAi.analyze(command));
    }

    @GetMapping("/anomalies")
    @Operation(summary = "Listar anomalías recientes (TODO)")
    public ResponseEntity<Map<String, String>> listAnomalies() {
        // TODO equipo EnergyOps: nuevo puerto query AnomaliaQueryPort + filtros tenant-scoped
        return notImplemented();
    }

    @PostMapping("/anomalies/{id}/acknowledge")
    @Operation(summary = "Reconocer anomalía (TODO)")
    public ResponseEntity<Map<String, String>> acknowledge(@PathVariable UUID id) {
        // TODO: cargar EnergyAnomaly desde repo, llamar aggregate.acknowledge(actor), guardar
        return notImplemented();
    }

    @PostMapping("/anomalies/{id}/resolve")
    @Operation(summary = "Resolver anomalía (TODO)")
    public ResponseEntity<Map<String, String>> resolve(@PathVariable UUID id) {
        // TODO: aggregate.resolve(actor, evidence) + auditar
        return notImplemented();
    }

    @GetMapping("/baseline")
    @Operation(summary = "Obtener baseline activa por meter (TODO)")
    public ResponseEntity<Map<String, String>> baseline() {
        // TODO: EnergyBaselineProviderPort.currentBaselineFor(tenantId)
        return notImplemented();
    }

    private UUID currentTenantId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof SecurityConfig.PrincipalAuthentication pa) {
            AuthenticatedPrincipal p = pa.getAuthenticatedPrincipal();
            return p.tenantId().value();
        }
        throw new DomainException("No hay principal autenticado en el contexto");
    }

    private static ResponseEntity<Map<String, String>> notImplemented() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of(
                "status", "not_implemented",
                "message", "Endpoint pendiente. Skeleton arquitecto."
        ));
    }
}

package com.energiaclara.infrastructure.energyops.api;

import com.energiaclara.infrastructure.energyops.api.dto.AnalyzeReadingRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * <b>SKELETON.</b> Bounded context EnergyOps (core domain).
 * TODO equipo EnergyOps: orquestar use cases register + detect + (createTicket si severity alta).
 */
@RestController
@RequestMapping("/api/energyops")
@Tag(name = "EnergyOps", description = "Detección de anomalías y baselines (SKELETON)")
@SecurityRequirement(name = "bearer-jwt")
public class EnergyOpsController {

    @PostMapping("/analyze-reading")
    @Operation(summary = "Registrar lectura + detectar anomalía + crear ticket (TODO)")
    public ResponseEntity<Map<String, String>> analyzeReading(@Valid @RequestBody AnalyzeReadingRequestDto req) {
        // TODO equipo EnergyOps:
        // 1. RegisterEnergyReadingUseCase.register(...)
        // 2. DetectAnomalyUseCase.detect(...)
        // 3. Si severity >= MEDIUM, CreateMaintenanceTicketUseCase.create(...)
        // 4. Auditar ANOMALY_DETECTED + READING_REGISTERED
        return notImplemented();
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

    private static ResponseEntity<Map<String, String>> notImplemented() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of(
                "status", "not_implemented",
                "message", "Endpoint pendiente. Skeleton arquitecto."
        ));
    }
}

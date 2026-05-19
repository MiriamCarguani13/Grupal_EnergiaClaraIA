package com.energiaclara.infrastructure.maintenance.api;

import com.energiaclara.infrastructure.maintenance.api.dto.CreateTicketRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * <b>SKELETON.</b> Bounded context Maintenance.
 * TODO equipo Maintenance: wire CreateMaintenanceTicketUseCase + state transitions.
 */
@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Maintenance", description = "Tickets de mantenimiento (SKELETON)")
@SecurityRequirement(name = "bearer-jwt")
public class TicketController {

    @PostMapping
    @PreAuthorize("hasAnyRole('DIRECTOR','TECNICO')")
    @Operation(summary = "Crear ticket (TODO)")
    public ResponseEntity<Map<String, String>> create(@Valid @RequestBody CreateTicketRequestDto req) {
        return notImplemented();
    }

    @GetMapping
    @Operation(summary = "Listar tickets del tenant (TODO)")
    public ResponseEntity<Map<String, String>> list() { return notImplemented(); }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener ticket por id (TODO)")
    public ResponseEntity<Map<String, String>> getById(@PathVariable UUID id) { return notImplemented(); }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('DIRECTOR')")
    @Operation(summary = "Asignar técnico (TODO)")
    public ResponseEntity<Map<String, String>> assign(@PathVariable UUID id) { return notImplemented(); }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasRole('TECNICO')")
    @Operation(summary = "Resolver con evidencia (TODO)")
    public ResponseEntity<Map<String, String>> resolve(@PathVariable UUID id) { return notImplemented(); }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('DIRECTOR','TECNICO')")
    @Operation(summary = "Cerrar ticket (TODO)")
    public ResponseEntity<Map<String, String>> close(@PathVariable UUID id) { return notImplemented(); }

    private static ResponseEntity<Map<String, String>> notImplemented() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of(
                "status", "not_implemented",
                "message", "Endpoint pendiente. Skeleton arquitecto."
        ));
    }
}

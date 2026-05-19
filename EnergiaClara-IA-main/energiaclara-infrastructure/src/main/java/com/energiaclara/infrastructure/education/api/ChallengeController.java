package com.energiaclara.infrastructure.education.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * <b>SKELETON.</b> Bounded context Education.
 * TODO equipo Education: retos + rankings + medallas.
 */
@RestController
@RequestMapping("/api/challenges")
@Tag(name = "Education", description = "Retos energéticos y gamificación (SKELETON)")
@SecurityRequirement(name = "bearer-jwt")
public class ChallengeController {

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCENTE','DIRECTOR')")
    @Operation(summary = "Crear reto (TODO)")
    public ResponseEntity<Map<String, String>> create() { return notImplemented(); }

    @GetMapping
    @Operation(summary = "Listar retos (TODO)")
    public ResponseEntity<Map<String, String>> list() { return notImplemented(); }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('DOCENTE','DIRECTOR')")
    @Operation(summary = "Publicar reto (TODO)")
    public ResponseEntity<Map<String, String>> publish(@PathVariable UUID id) { return notImplemented(); }

    @PostMapping("/{id}/evaluate")
    @Operation(summary = "Evaluar reto vs lecturas (TODO)")
    public ResponseEntity<Map<String, String>> evaluate(@PathVariable UUID id) { return notImplemented(); }

    @GetMapping("/{id}/ranking")
    @Operation(summary = "Ranking del reto (TODO)")
    public ResponseEntity<Map<String, String>> ranking(@PathVariable UUID id) { return notImplemented(); }

    private static ResponseEntity<Map<String, String>> notImplemented() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of(
                "status", "not_implemented",
                "message", "Endpoint pendiente. Skeleton arquitecto."
        ));
    }
}

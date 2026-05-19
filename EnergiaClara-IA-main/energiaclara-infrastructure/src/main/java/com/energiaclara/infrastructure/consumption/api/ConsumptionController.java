package com.energiaclara.infrastructure.consumption.api;

import com.energiaclara.infrastructure.consumption.api.dto.RegisterReadingRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * <b>SKELETON.</b> Bounded context Consumption.
 * TODO equipo Consumption: implementar endpoints registrando lecturas vía {@code RegisterEnergyReadingUseCase}.
 */
@RestController
@RequestMapping("/api/energyops/readings")
@Tag(name = "Consumption", description = "Lecturas energéticas (SKELETON)")
@SecurityRequirement(name = "bearer-jwt")
public class ConsumptionController {

    @PostMapping
    @Operation(summary = "Registrar lectura (TODO implementar)")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterReadingRequestDto request) {
        // TODO equipo Consumption: invocar RegisterEnergyReadingUseCase + mapear command + auditar READING_REGISTERED
        return ResponseEntity
                .status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of(
                        "status", "not_implemented",
                        "message", "Endpoint pendiente. Skeleton arquitecto. Implementar en consumption/api + adapter."
                ));
    }
}

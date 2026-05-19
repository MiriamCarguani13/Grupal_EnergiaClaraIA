# ai-api

## Propósito
REST controllers AI + DTOs HTTP + anotaciones OpenAPI.

## Estructura sugerida
```
com.energiaclara.ai.api
├── rest
│   ├── AnomalyDetectionController       POST /api/ai/detect-anomaly
│   ├── ExplanationController            POST /api/ai/explain
│   └── BaselineController               POST /api/ai/recompute-baseline
├── rest.dto
│   ├── DetectAnomalyRequest             record (facilityId, meterId, kwhValue, timestamp)
│   ├── AnomalyDetectedResponse
│   └── ExplanationResponse
└── exception
    └── AiExceptionHandler               @RestControllerAdvice si necesario
```

## Reglas
- ✅ Depende de `ai-application` (in ports + commands)
- ❌ NO depende de `ai-infrastructure` directamente (no llama adapter; usa use case)
- ✅ Spring web, validation, OpenAPI

## Convenciones
- Path prefix: `/api/ai/**`
- Auth: requiere JWT (gestionado por SecurityConfig del bootstrap)
- DTOs HTTP separados de Commands de application (mapear en controller)
- Bean validation con `@Valid`, `@NotNull`, etc.

## TODO equipo
- [ ] `AnomalyDetectionController` con `@RestController`
- [ ] DTOs HTTP records con validación
- [ ] OpenAPI tags: `@Tag(name = "AI", description = "Detección y explicación de anomalías")`
- [ ] `@SecurityRequirement(name = "bearer-jwt")` en cada endpoint
- [ ] Mapear `DetectAnomalyRequest → DetectAnomalyCommand`

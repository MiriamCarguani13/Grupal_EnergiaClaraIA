# ai-service

Bounded context **AI / Detección de Anomalías**. Espejo del pattern Clean Architecture de Solveria (ProyectoAI).

## Estado

**Esqueleto.** Sub-módulos creados, código pendiente. Esta carpeta es responsabilidad del integrante de equipo que cubra la parte IA del proyecto (motor de detección, recomendaciones, integración con `consumo.lectura` / `energiaops.anomalia` / `energiaops.snapshot_linea_base`).

## Estructura

```
ai-service/
├── ai-domain          Aggregates + value objects + invariantes del dominio AI
├── ai-application     Use cases + ports (in/out), DTOs commands
├── ai-infrastructure  Adapters (Spring AI, embeddings, persistence específica AI)
├── ai-api             REST controllers + DTOs HTTP + OpenAPI
└── ai-bootstrap       @SpringBootApplication + wiring + application.yml (si corre como microservicio standalone)
```

## Reglas arquitectónicas

Sigue [ADR-002 Hexagonal pragmática](../docs/adr/ADR-002-hexagonal-ddd-tactico.md) y [ADR-008 ArchUnit](../docs/adr/ADR-008-archunit-boundaries.md).

| Módulo | Puede depender de | NO puede depender de |
|---|---|---|
| `ai-domain` | nada (solo java estándar) | Spring, JPA, Jackson, servlet |
| `ai-application` | `ai-domain`, `core-platform` | Spring (excepto `@Transactional` si justificado), JPA |
| `ai-infrastructure` | `ai-application`, `ai-domain`, `core-platform`, Spring, JPA, Spring AI | `ai-api`, `ai-bootstrap` |
| `ai-api` | `ai-application` (use cases / ports in), `core-platform` (value objects) | `ai-infrastructure` directo |
| `ai-bootstrap` | todos los anteriores | nadie depende de bootstrap |

## Qué va en cada módulo

### `ai-domain`
- Aggregates: `AnomalyDetection`, `PredictionResult`, `BaselineCalculation`
- Value objects: `AnomalyScore`, `ConfidenceLevel`, `ModelVersion`
- Domain events: `AnomalyPredicted`, `BaselineRecomputed`
- Domain services: `AnomalyDetectionService.detectarAnomalia(consumo, baseline)`
- Sin frameworks

### `ai-application`
- Use cases (in ports): `DetectAnomalyUseCase`, `RecomputeBaselineUseCase`, `ExplainAnomalyUseCase`
- Commands: `DetectAnomalyCommand(facilityId, meterId, kwhValue, timestamp)`
- Out ports: `LlmPort`, `VectorStorePort`, `BaselineRepositoryPort`, `AnomalyResultRepositoryPort`
- Application service que orquesta los anteriores

### `ai-infrastructure`
- `SpringAiLlmAdapter` (implementa `LlmPort`) — llama OpenAI / Anthropic / local model
- `PgVectorAdapter` (implementa `VectorStorePort`) — embeddings + similarity search
- `BaselineJpaAdapter` (implementa `BaselineRepositoryPort`)
- Configs Spring AI

### `ai-api`
- `AnomalyController` con `POST /api/ai/detect-anomaly`, `POST /api/ai/explain`
- DTOs HTTP: `DetectAnomalyRequest`, `AnomalyExplanationResponse`
- OpenAPI annotations

### `ai-bootstrap`
- `@SpringBootApplication` si corre como microservicio standalone (port 8091)
- `application.yml` con configs Spring AI + DB
- Wiring beans manuales si `ai-application` permanece Spring-free

**Alternativa:** si AI corre embebido en EnergíaClara (no microservicio separado), `ai-bootstrap` no se necesita — la app principal `energiaclara-infrastructure` declara dep de `ai-application` y `ai-infrastructure`, y wirea desde su propio `@Configuration`.

## Setup para empezar a codear

1. Confirmar con el equipo si AI corre embebido o standalone (decide si `ai-bootstrap` se completa o se ignora).
2. Añadir `pom.xml` mínimo en cada sub-módulo (template en cada README local).
3. Definir aggregates en `ai-domain` antes que cualquier otra cosa (Fase 2 obliga).
4. ArchUnit tests recomendados (ver [ADR-008](../docs/adr/ADR-008-archunit-boundaries.md)).
5. Tests unitarios del dominio sin Spring (milisegundos).
6. Integración con `core-platform` (TenantId, FacilityId, KwhValue).
7. Conectar con `energiaops.anomalia` via puerto out → adapter JPA.

## Integración con otros módulos

- Consume `core-platform` para tipos compartidos (`TenantId`, `KwhValue`, `FacilityId`)
- Publica eventos hacia `audit.evento_auditoria` (Fase 4 §9) — usar `AuditTrailService` o evento de dominio
- Recibe `EnergyReading` desde `energiaclara-application` (caso de uso `AnalyzeReading` compuesto: register reading → AI detect → create ticket)
- En multi-tenant, **todo input debe incluir `TenantId`** (Fase 4 §7)

## Referencias
- Docs Fase 1 §1.14 (Rol de la IA)
- Docs Fase 3 §7 (Integración con AI Service)
- Docs Fase 4 §8 (IA en contexto multi-tenant)
- BACKEND_SPEC.md §2.3 (puerto `EnergyBaselineProviderPort`)

# ADR-002: Hexagonal pragmática + DDD táctico ligero

## Estado
Aceptado · 2026-05-18

## Contexto

Fase 3 docs piden Arquitectura Hexagonal (Ports & Adapters) con Dependency Rule estricta. Fase 2 docs piden DDD táctico (aggregates, value objects, domain events, invariantes). Existen interpretaciones puristas y pragmáticas.

Purismo extremo (Clean Architecture Robert Martin):
- domain/application/infrastructure/api/bootstrap como módulos físicos separados
- DTOs en cada borde (domain↔application↔api)
- Mappers explícitos en cada cruce
- Sin Lombok, sin getters/setters genéricos

Costo: ~2-3x archivos para misma funcionalidad. Beneficio: máximo desacople.

## Decisión

**Hexagonal pragmática.** Aplicamos:

- ✅ Domain no depende de framework (verificado ArchUnit, ADR-008)
- ✅ Puertos en application (`IamUserRepositoryPort`, `EnergyAnomalyRepositoryPort`)
- ✅ Adapters en infrastructure (`IamUserRepositoryAdapter`, `JwtTokenIssuerAdapter`)
- ✅ Aggregates con invariantes (`IamUser.register()`, `EnergyAnomaly.acknowledge()`)
- ✅ Value objects (`TenantId`, `Email`, `KwhValue`)
- ✅ Domain events (`AnomalyDetected`, `TicketCreated`)

**Concesiones pragmáticas:**
- Application y domain en mismo módulo Maven para bounded contexts pequeños (`iam-service` agrupa ambos)
- DTOs HTTP separados de Commands de application (`LoginRequestDto` ≠ `LoginCommand`), pero Commands reusan value objects de domain directamente (no se duplica `Email`)
- `MaintenanceTicket` domain expone getters (no Lombok, no patrón rich behavior puro)
- Algunos use cases tocan repos directos sin domain service intermedio cuando lógica es trivial (CRUD)

## Alternativas consideradas

- **Clean Architecture purista (5 capas físicas)**: rechazado. Demasiado boilerplate para scope académico.
- **DDD anémico (entidades = JPA + getters/setters)**: rechazado explícitamente por Fase 2 §11 anti-patrones.
- **Service Layer tradicional sin aggregates**: rechazado. No cumple invariantes Fase 2 §2.4.

## Consecuencias

**Beneficios:**
- Dominio testable sin Spring (tests en ms, no segundos)
- Cumple Dependency Rule (ArchUnit verifica)
- Invariantes de negocio centralizadas en aggregates
- Menos archivos que purismo extremo
- Defendible en docs (alineado Fase 2/3)

**Cedemos:**
- Application y domain comparten módulo en algunos casos (no físicamente separados)
- Domain expone getters (algo de fugacidad de estado)
- Algunos cruces sin mapper explícito

**Riesgos:**
- Si crece código, pragmatismo puede degradar a anémico. Mitigar: ArchUnit + code review.

## Referencias
- Fase 2 §2 (Aggregates)
- Fase 2 §11 (Anti-patrones a evitar)
- Fase 3 §1 (Dependency Rule)
- ADR-008 (ArchUnit)

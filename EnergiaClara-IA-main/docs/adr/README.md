# Architecture Decision Records (ADR)

Decisiones arquitectónicas significativas del proyecto **EnergíaClara AI**. Cada ADR captura una decisión, su contexto, alternativas consideradas, y consecuencias.

## Formato

Usamos formato breve estilo Michael Nygard:

```
# ADR-NNN: <Título corto>

## Estado
Propuesto | Aceptado | Reemplazado por ADR-XXX | Deprecado

## Contexto
¿Qué fuerzas técnicas / de negocio motivaron esta decisión?

## Decisión
¿Qué decidimos hacer?

## Alternativas consideradas
Listar opciones rechazadas + razón.

## Consecuencias
Trade-offs: qué ganamos, qué cedemos, qué riesgos.
```

## Índice

| ADR | Título | Estado |
|---|---|---|
| [ADR-001](ADR-001-monorepo-multi-module.md) | Monorepo multi-módulo Maven (vs multi-repo microservicios) | Aceptado |
| [ADR-002](ADR-002-hexagonal-ddd-tactico.md) | Hexagonal pragmática + DDD táctico ligero | Aceptado |
| [ADR-003](ADR-003-iam-spring-free.md) | iam-service Spring-free, wiring en infrastructure | Aceptado |
| [ADR-004](ADR-004-role-enum-aligned-db.md) | Role enum alineado con nombres de DB | Aceptado |
| [ADR-005](ADR-005-flyway-disabled.md) | Flyway deshabilitado, schema DBA-owned | Aceptado |
| [ADR-006](ADR-006-audit-jdbctemplate.md) | AuditTrailService vía JdbcTemplate (sin event listener) | Aceptado |
| [ADR-007](ADR-007-multitenant-discriminator.md) | Multi-tenant discriminador `inquilino_id` por fila | Aceptado |
| [ADR-008](ADR-008-archunit-boundaries.md) | ArchUnit para enforcing dependency rule | Aceptado |
| [ADR-009](ADR-009-bounded-context-subpackages.md) | Sub-paquetes por bounded context en infrastructure | Aceptado |

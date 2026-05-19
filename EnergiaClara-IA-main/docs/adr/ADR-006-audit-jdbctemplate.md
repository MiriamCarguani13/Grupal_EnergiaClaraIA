# ADR-006: AuditTrailService vía JdbcTemplate (sin event listener)

## Estado
Aceptado · 2026-05-18 · Provisional para Hito 1

## Contexto

Docs piden auditoría obligatoria (Fase 4 §9). Existen dos patrones:

1. **Imperativo**: services/controllers llaman `auditTrailService.record(...)` después de operación exitosa. Acoplamiento explícito al audit.
2. **Event-driven**: aggregates emiten `DomainEvent`s, un `@EventListener<AuditEvent>` los persiste. Desacople total.

El patrón 2 es más elegante pero requiere:
- `DomainEventPublisherPort` implementado
- `SpringDomainEventPublisher` reenviando a `ApplicationEventPublisher`
- `AuditEventListener` registrado
- Aggregates emitiendo eventos consistentemente

## Decisión

**Hito 1: solo patrón imperativo.** `AuditTrailService` usa `JdbcTemplate` directo, persiste `audit.evento_auditoria` con `accion, tipo_recurso, recurso_id, inquilino_id, actor_id, severidad, id_correlacion, ocurrido_el`. Invocado desde `AuthController` después de login/register.

```java
auditTrail.record("AUTH_LOGIN_SUCCESS", "Usuario", userId, tenantId, actorId, "MEDIA", ipAddress);
```

Fallos del audit (DB caída, hash inconsistente) se loguean como WARN pero no rompen la operación de negocio.

## Alternativas consideradas

- **Event-driven con `@EventListener` directo**: deferido a Hito 2/3. Requiere wiring de `DomainEventPublisher` + redefinir aggregates.
- **AOP con `@Auditable` annotation**: rechazado. Mágico, difícil debug, oculta dónde ocurre.
- **CDC (Change Data Capture) en SQL Server**: rechazado. Solo Enterprise Edition, overkill.

## Consecuencias

**Beneficios:**
- Implementación 1 archivo, 60 líneas
- Visible y debuggeable: `grep "audit.record"` muestra todos los puntos
- Resiliente: audit fail no rompe login

**Cedemos:**
- Si dev olvida llamar `record(...)`, no se audita (no enforcement)
- Acoplamiento de controllers a `AuditTrailService`
- Cambios de schema audit requieren modificar el servicio en lugar de evento

**Plan Hito 3:** introducir `AuditEventListener` + emitir `AuditEvent` desde aggregates. `AuditTrailService` quedará como fallback para casos imperativos (ej. login fallido sin aggregate involucrado).

## Referencias
- Fase 4 §9 (Auditoría enterprise)
- BACKEND_SPEC.md §6.2 (Cómo persiste audit)

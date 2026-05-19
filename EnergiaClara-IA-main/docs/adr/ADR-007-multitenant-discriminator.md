# ADR-007: Multi-tenant discriminador `inquilino_id` por fila

## Estado
Aceptado · 2026-05-18

## Contexto

Fase 4 §2 obliga elegir estrategia de aislamiento multi-tenant:

1. **Base de datos por tenant**: máximo aislamiento, máximo costo. SQL Server licencias por instancia.
2. **Schema por tenant**: aislamiento medio, scripts DDL N veces, joins cross-tenant imposibles.
3. **Discriminador (columna `inquilino_id` por fila)**: aislamiento lógico, una sola DB, simple.

Proyecto académico, volumen proyectado bajo (< 50 tenants v1), DBA solo.

## Decisión

**Discriminador por tenant.** Cada tabla con datos de negocio tiene `inquilino_id UNIQUEIDENTIFIER NOT NULL`. Toda query filtra explícito por tenant del JWT.

```sql
SELECT * FROM consumo.lectura WHERE inquilino_id = ? AND ...
```

Tablas con `inquilino_id`: `iam.usuario`, `iam.usuario_rol`, `core.inquilino` (PK), `core.edificio`, `core.medidor`, `consumo.lectura`, `energiaops.anomalia`, `energiaops.snapshot_linea_base`, `mantenimiento.ticket`, `educacion.reto`, `audit.evento_auditoria`, `analitica.kpi_diario`, etc.

Tablas globales sin `inquilino_id`: `iam.rol`, `iam.permiso`, `iam.rol_permiso` (catálogos compartidos).

## Estrategia aplicación

- JWT carga `tenant_id` claim (firmado HS384, inmutable)
- `AuthenticatedPrincipal.tenantId()` derivado del claim
- Controllers extraen tenantId del SecurityContext (NO del request body)
- `TenantContext` ThreadLocal opcional para servicios deep en stack
- Cada repository method recibe `TenantId` parámetro o filtra en query

## Alternativas consideradas

- **Base por tenant**: rechazado por costo licencias SQL Server + N backups.
- **Schema por tenant**: rechazado. Reportes cross-tenant para super-admin imposibles sin unión manual de N schemas.
- **Row-Level Security (SQL Server feature nativo)**: deferido. Útil v2 como defensa en profundidad, no como única protección.

## Consecuencias

**Beneficios:**
- Una sola DB, un solo backup, un solo schema
- Joins simples
- Reportes cross-tenant (auditor global) triviales
- Migración de tenants a base separada posible si crece (mover filas filtradas)

**Cedemos:**
- Una query sin filtro tenant = fuga catastrófica. Riesgo si dev olvida.
- Sin aislamiento físico (un tenant pesado afecta performance de todos)

**Mitigación:**
- ArchUnit test futuro: queries en `*Adapter` deben recibir `tenantId` (heurística vía `findAllBy*AndInquilinoId*` naming convention)
- Code review obligatorio para repos nuevos
- v2: añadir Row-Level Security SQL Server como red

## Anti-patrones a evitar (Fase 4 §12)
- Hardcodear `tenantId` en queries — usar `AuthenticatedPrincipal.tenantId()`
- Aceptar `tenantId` desde request body en endpoints autenticados — fuente de verdad = JWT
- Mezclar `tenantId` de dos usuarios en una transacción

## Referencias
- Fase 4 §2 (Estrategias de aislamiento)
- Fase 4 §3 (Tenant Context)
- Fase 4 §12 (Anti-patrones)

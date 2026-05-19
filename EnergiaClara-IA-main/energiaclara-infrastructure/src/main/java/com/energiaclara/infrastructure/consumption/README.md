# Consumption (skeleton)

Bounded context: **medición + registro de lecturas energéticas**.

## Estado
**Skeleton.** Stubs creados. Implementación pendiente por equipo Consumption.

## Aggregates (Fase 2)
- `Meter` — punto físico de medición (no implementado v1; usar `core.medidor` directamente)
- `Reading` (`EnergyReading` en domain) — lectura individual con invariantes

## Endpoints a implementar
| Método | Ruta | Use case | Auth |
|---|---|---|---|
| POST | `/api/energyops/readings` | `RegisterEnergyReadingUseCase` | autenticado |
| GET | `/api/energyops/readings?from&to&meterId` | TBD nuevo puerto | autenticado |

## Archivos stub
- `api.ConsumptionController` — wire al use case `RegisterEnergyReadingUseCase`
- `persistence.entity.LecturaEntity` — mapea `consumo.lectura`
- `persistence.repository.LecturaRepository` — JpaRepository
- `persistence.adapter.EnergyReadingRepositoryAdapter` — implementa `EnergyReadingRepositoryPort`
- `persistence.mapper.EnergyReadingJpaMapper` — `LecturaEntity` ↔ `EnergyReading`

## TODO equipo
- [ ] Implementar `EnergyReadingRepositoryAdapter.save(...)` + `findById(...)`
- [ ] Mapper bidireccional `LecturaEntity` ↔ `EnergyReading` (cuidado: `KwhValue` usa double, columna `valor` es decimal(18,4))
- [ ] Controller: mapear `RegisterReadingRequestDto` → `RegisterEnergyReadingCommand`
- [ ] Validar tenantId del JWT == tenantId de la lectura
- [ ] Auditoría: `READING_REGISTERED` vía `AuditTrailService`
- [ ] Wiring beans application service en `EnergyOpsWiringConfig` (compartido con energyops)
- [ ] **Columnas extras DBA** (ya en `LecturaEntity`): `facility_label`, `meter_label`, `voltaje`, `factor_potencia`. Poblar si llegan en payload. Útiles para dashboard sin extra join

## Reglas
- Tabla `consumo.lectura` tiene `version_fila timestamp` → `@Version byte[]` en entity
- Filtrar siempre por `inquilino_id` (ADR-007)
- `KwhValue` debe ser >= 0 (invariante dominio)
- Solo una lectura por medidor + periodo (Fase 2 §2.2.2)

## Integración
- **Consumer:** EnergyOps (al detectar anomalía consume Reading)
- **Producer:** Analytics (agrega KPIs de lecturas)
- **Producer:** AI Service (entrada para detección de patrones)

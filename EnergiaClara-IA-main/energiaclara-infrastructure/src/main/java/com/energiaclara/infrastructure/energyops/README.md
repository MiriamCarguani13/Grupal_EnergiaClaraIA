# EnergyOps (skeleton)

Bounded context **core domain**: detección de anomalías + gestión de baselines.

## Estado
**Skeleton.** Implementación pendiente equipo EnergyOps.

## Aggregate
- `Anomaly` (`EnergyAnomaly` en domain) — desviación detectada con score, severidad, tipo

## Endpoints a implementar
| Método | Ruta | Use case | Auth |
|---|---|---|---|
| POST | `/api/energyops/analyze-reading` | Compuesto: `RegisterEnergyReadingUseCase` + `DetectAnomalyUseCase` (+ `CreateMaintenanceTicketUseCase` si severity ≥ MEDIUM) | autenticado |
| GET | `/api/energyops/anomalies` | nuevo puerto query (filtros from, to, severity, status) | autenticado |
| POST | `/api/energyops/anomalies/{id}/acknowledge` | aggregate `EnergyAnomaly.acknowledge()` | TECNICO\|DIRECTOR |
| POST | `/api/energyops/anomalies/{id}/resolve` | aggregate `EnergyAnomaly.resolve()` | TECNICO\|DIRECTOR |
| GET | `/api/energyops/baseline?meterId` | lectura baseline activa | autenticado |

## Archivos stub
- `api.EnergyOpsController`
- `persistence.entity.AnomaliaEntity` — mapea `energiaops.anomalia`
- `persistence.entity.SnapshotLineaBaseEntity` — mapea `energiaops.snapshot_linea_base`
- `persistence.repository.AnomaliaRepository`, `SnapshotLineaBaseRepository`
- `persistence.adapter.EnergyAnomalyRepositoryAdapter` (implementa `EnergyAnomalyRepositoryPort`)
- `persistence.adapter.EnergyBaselineProviderAdapter` (implementa `EnergyBaselineProviderPort`)

## TODO equipo
- [ ] `EnergyAnomalyRepositoryAdapter.save()` con mapper bidireccional
- [ ] `EnergyBaselineProviderAdapter.currentBaselineFor(tenantId)` lee TODAS las baselines activas del tenant + arma Map
- [ ] Mapeo severidad DB (`CRITICA/ALTA/MEDIA/BAJA`) ↔ enum domain (`CRITICAL/HIGH/MEDIUM/LOW`)
- [ ] Endpoint compuesto `analyze-reading` orquesta 2-3 use cases
- [ ] Auditoría: `ANOMALY_DETECTED`, `ANOMALY_ACKNOWLEDGED`, `ANOMALY_RESOLVED`
- [ ] Cache baseline activa (v2) — caro recargar todas en cada llamada
- [ ] **Columnas extras DBA** (ya en entities):
  - `snapshot_linea_base.activo` — **CRÍTICO**, filtrar `WHERE activo=true` en baseline provider
  - `snapshot_linea_base.tolerancia_porcentaje` — usar para cálculo umbral anomalía
  - `anomalia.recomendacion` — texto IA output
  - `anomalia.costo_estimado`, `co2_estimado` — calcular impacto antes de persistir
  - `*.facility_label`, `meter_label` — denormalización para queries dashboard

## Reglas (Fase 2 §2.2.3)
- Una anomalía no puede cambiar de tipo una vez confirmada (invariante)
- Severidad calculada en momento de detección
- Solo se resuelve mediante acción correctiva o descarte
- Estados: `DETECTED → NOTIFIED → IN_ACTION → RESOLVED|IGNORED`

## Integración
- **Consumer:** Maintenance (anomalía severity ≥ MEDIUM → crea ticket)
- **Consumer:** Analytics (cuenta anomalías por tenant/severidad)
- **Producer:** AI Service (alimenta `AnomalyDetectionService` con score)

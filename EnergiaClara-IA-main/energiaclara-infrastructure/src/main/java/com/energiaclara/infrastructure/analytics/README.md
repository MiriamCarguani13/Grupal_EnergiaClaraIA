# Analytics

Bounded context: **KPIs, dashboard ejecutivo, métricas agregadas**.

## Estado actual
**Stub funcional implementado** vía `JdbcTemplate` en `api/rest/AnalyticsController.java` (flat, no en este paquete).

## Endpoints activos
| Método | Ruta | Estado |
|---|---|---|
| GET | `/api/analytics/dashboard` | ✅ funcional (cuenta lecturas, anomalías, tickets abiertos) |
| GET | `/api/analytics/kpis` | ✅ funcional (últimas 50 lecturas) |
| GET | `/api/analytics/anomalies` | ✅ funcional (últimas 50 anomalías) |

## TODO equipo Analytics (refactor)
- [ ] Mover `AnalyticsController` desde `api/rest/` a `analytics/api/`
- [ ] Crear puerto `AnalyticsQueryPort` en `energiaclara-application/port/out/`
- [ ] Implementar `AnalyticsQueryAdapter` aquí usando `JdbcTemplate` o JPA criteria
- [ ] Stub actual queda como fallback hasta que se complete migración
- [ ] Endpoints adicionales:
  - `GET /api/analytics/kpis/monthly` — agrega por mes vía `analitica.kpi_mensual`
  - `GET /api/analytics/impact-summary` (DIRECTOR\|AUDITOR) — `analitica.resumen_impacto`
  - `GET /api/analytics/sla-compliance` — cumplimiento SLA tickets

## Reglas
- Toda query filtra por `inquilino_id` (ADR-007)
- Cache 5 min recomendado para dashboard (recálculos costosos)
- Endpoints DIRECTOR-only para datos sensibles (impacto, comparativas)

## Integración
- **Consumer:** todos los demás contextos producen datos que analytics agrega
- Sin producers (terminal del flujo)

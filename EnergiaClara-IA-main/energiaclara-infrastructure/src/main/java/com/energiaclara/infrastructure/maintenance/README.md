# Maintenance (skeleton)

Bounded context: **tickets de mantenimiento + evidencias + SLA**.

## Aggregate (Fase 2 §2.2.4)
- `Ticket` (`MaintenanceTicket` en domain)

## Endpoints a implementar
| Método | Ruta | Use case | Auth |
|---|---|---|---|
| POST | `/api/tickets` | `CreateMaintenanceTicketUseCase` | DIRECTOR\|TECNICO |
| GET | `/api/tickets` | nuevo puerto query (filtros tenant + estado + asignado_a) | autenticado |
| GET | `/api/tickets/{id}` | repo findById | autenticado |
| POST | `/api/tickets/{id}/assign` | aggregate `MaintenanceTicket.assign()` | DIRECTOR |
| POST | `/api/tickets/{id}/resolve` | aggregate `MaintenanceTicket.resolve()` | TECNICO |
| POST | `/api/tickets/{id}/close` | aggregate `MaintenanceTicket.close()` | DIRECTOR\|TECNICO |

## Archivos stub
- `api.TicketController`
- `persistence.entity.TicketEntity` — `mantenimiento.ticket`
- `persistence.repository.TicketRepository`
- `persistence.adapter.MaintenanceTicketRepositoryAdapter` (implementa `MaintenanceTicketRepositoryPort`)

## TODO equipo
- [ ] Implementar `MaintenanceTicketRepositoryAdapter.save()` + mapper
- [ ] Endpoint `POST /tickets/{id}/resolve` exige evidencia (invariante Fase 2 §2.4)
- [ ] Mapeo `TicketStatus` enum domain ↔ DB string (`BORRADOR`, `ABIERTO`, `ASIGNADO`, `EN_PROCESO`, `CERRADO`, `REABIERTO`)
- [ ] Auditoría: `TICKET_CREATED`, `TICKET_ASSIGNED`, `TICKET_RESOLVED`, `TICKET_CLOSED`, `TICKET_REOPENED`
- [ ] SLA check: cron job que marca `sla_incumplido` cuando `vencimiento_sla < NOW()` y estado abierto

## Reglas (Fase 2 §2.4)
- Ticket cerrado no puede modificarse
- No se cierra sin evidencia
- SLA marca `sla_incumplido = 1` automático cuando vence
- Optimistic locking `@Version` para concurrencia (Fase 2 §2.9)

## Integración
- **Consumer:** EnergyOps (anomalía severity ≥ MEDIUM → crea ticket vía `CreateMaintenanceTicketUseCase`)
- **Producer:** Analytics (tickets cerrados, cumplimiento SLA)

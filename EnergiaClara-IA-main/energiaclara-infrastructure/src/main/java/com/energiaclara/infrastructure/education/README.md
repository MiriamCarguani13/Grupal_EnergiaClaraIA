# Education (skeleton)

Bounded context: **retos energéticos + rankings + gamificación**.

## Aggregate (Fase 2 §2.2.5)
- `Challenge` (`EnergyChallenge` en domain)

## Endpoints a implementar
| Método | Ruta | Use case | Auth |
|---|---|---|---|
| POST | `/api/challenges` | TBD: factory de Challenge | DOCENTE\|DIRECTOR |
| GET | `/api/challenges` | nuevo puerto query | autenticado |
| POST | `/api/challenges/{id}/publish` | aggregate `EnergyChallenge.publish()` | DOCENTE\|DIRECTOR |
| POST | `/api/challenges/{id}/evaluate` | `EvaluateChallengeUseCase` | sistema/scheduler\|DOCENTE |
| GET | `/api/challenges/{id}/ranking` | nuevo puerto query | autenticado |

## Archivos stub
- `api.ChallengeController`
- `persistence.entity.RetoEntity`
- `persistence.repository.RetoRepository`
- `persistence.adapter.EnergyChallengeRepositoryAdapter` (implementa `EnergyChallengeRepositoryPort`)

## TODO equipo
- [ ] `EnergyChallengeRepositoryAdapter.save()` + mapper
- [ ] Endpoint `evaluate` lee progreso real desde lecturas → calcula avance
- [ ] Auditoría: `CHALLENGE_PUBLISHED`, `CHALLENGE_EVALUATED`, `CHALLENGE_COMPLETED`
- [ ] Cache Redis del ranking (Fase 1 §1.12 menciona)
- [ ] Sistema medallas: trigger automático cuando reto completed

## Reglas (Fase 2 §2.2.5, §2.4)
- Reto debe tener meta de ahorro (`KwhTarget`)
- Reto debe tener periodo definido
- Reto publicado no se modifica sin versionado
- Ranking recalcula automático tras cada progreso

## Integración
- **Consumer:** Consumption (lecturas alimentan progreso del reto)
- **Producer:** Analytics (KPIs de adopción cultural)

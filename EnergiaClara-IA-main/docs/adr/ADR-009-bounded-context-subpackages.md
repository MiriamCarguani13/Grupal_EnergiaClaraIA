# ADR-009: Sub-paquetes por bounded context en infrastructure

## Estado
Aceptado · 2026-05-19

## Contexto

Hito 2 trae 5 bounded contexts más: Consumption, EnergyOps, Maintenance, Education, Analytics. Cada uno con controller + entidades JPA + adapters de puertos out.

Dos formas de organizar:

**Opción A**: Sub-paquetes Java dentro de `energiaclara-infrastructure` por contexto:
```
infrastructure/
├── consumption/      (api, persistence, ...)
├── energyops/
├── maintenance/
├── education/
└── analytics/
```

**Opción B**: Módulos Maven separados por contexto (estilo Solveria ai-service):
```
energiaclara-platform/
├── consumption-service/{domain,application,infrastructure,api,bootstrap}
├── energyops-service/...
├── maintenance-service/...
├── education-service/...
└── analytics-service/...
```

Opción B = 25 sub-módulos Maven adicionales. Cada uno con pom.xml + 5 sub-dirs.

## Decisión

**Opción A.** Sub-paquetes por contexto dentro de `energiaclara-infrastructure`.

```
com.energiaclara.infrastructure.<context>
├── api               REST controllers + DTOs HTTP del contexto
└── persistence       JPA entities + repositories + adapters del contexto
    ├── entity
    ├── repository
    ├── mapper
    └── adapter
```

Paquetes transversales se mantienen flat:
- `api.exception` — error handling cross-context
- `audit` — auditoría compartida
- `config.*` — cors, security, openapi
- `security` — JWT, filters, context
- `persistence.iam` — IAM (existente, sigue patrón actual)

`ai-service` queda como excepción con módulos Maven (ADR-001) porque puede migrar a microservicio standalone.

## Alternativas consideradas

**Opción B (módulos Maven completos)**:
- ❌ 25 sub-módulos extra → ~75 archivos pom.xml + READMEs
- ❌ Tiempo build ~3x
- ❌ Sobre-engineering para scope académico
- ❌ Solo justifica si bounded context migra a microservicio (no es el caso de los 5)

**Opción C (flat con prefijos `Consumption*`, `EnergyOps*`)**:
- ❌ Mezcla tipos en mismo paquete → 60+ archivos sueltos
- ❌ Imposible aplicar ArchUnit "ninguna clase de consumption importa de energyops"

## Consecuencias

**Beneficios:**
- Onboarding equipo X: abre `infrastructure/<context>/` → ve toda su responsabilidad
- ArchUnit puede enforcing "no cross-context imports" (ver ADR-008 extensión)
- Cada contexto puede migrar a Maven module propio LUEGO si crece
- Build sigue siendo 5 módulos Maven, no 30
- README por contexto = doc viva por equipo

**Cedemos:**
- Sub-paquetes no son módulos Maven → no se puede declarar `<dependency>consumption</dependency>` aislada
- Posible acceso indebido cross-context si dev importa clase de otro paquete (mitigado con ArchUnit)

**Plan migración futura:**
Cuando algún contexto crezca > 50 archivos o se decida convertir en microservicio:
1. Crear módulo Maven nuevo `<context>-service/{domain,application,infrastructure}`
2. Mover paquete `infrastructure.<context>.*` → `<context>-service/infrastructure`
3. Mover use cases de `energiaclara-application/.../<context>` al nuevo módulo
4. Actualizar imports
5. Nuevo ADR documentando promoción

## Convención de naming

| Tipo | Patrón | Ejemplo |
|---|---|---|
| Controller | `<Context>Controller` | `TicketController` |
| Entity | `<TableSpanish>Entity` | `LecturaEntity`, `AnomaliaEntity`, `RetoEntity` |
| Repository | `<TableSpanish>Repository` | `TicketRepository` |
| Adapter | `<DomainAggregate>RepositoryAdapter` | `EnergyAnomalyRepositoryAdapter` |
| DTO request | `<Action><Subject>RequestDto` | `CreateTicketRequestDto` |
| DTO response | `<Subject>ResponseDto` | `AnomalyItemDto` |

## Referencias
- ADR-001 (monorepo multi-módulo)
- ADR-008 (ArchUnit boundaries)
- BACKEND_SPEC.md §2.4 (estructura paquetes infrastructure)

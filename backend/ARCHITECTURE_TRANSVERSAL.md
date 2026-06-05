# Arquitectura Transversal / Hexagonal - EnergiaClara AI

## Que significa arquitectura transversal en este proyecto

EnergiaClara AI esta organizado como un monolito modular con capas transversales. Esto significa que el primer nivel de organizacion no es un arbol vertical por modulo, sino las capas arquitectonicas:

```text
api -> application -> domain <- application <- infrastructure
bootstrap
```

Los bounded contexts existen dentro de esas capas. Por ejemplo, `energyops` tiene controller en API, casos de uso en Application, reglas puras en Domain y persistencia en Infrastructure. La separacion importante no es crear carpetas por simetria, sino mantener dependencias correctas:

- API traduce HTTP a comandos/resultados de Application.
- Application orquesta casos de uso mediante puertos de entrada/salida.
- Domain contiene reglas puras, value objects, enums y conceptos de negocio.
- Infrastructure contiene detalles tecnicos: JPA, SQL Server, JWT, Spring Security, audit adapter y configuracion.
- Bootstrap arranca Spring Boot y configura el escaneo.

## Capas

```text
com.energiaclara
├── api
│   └── rest
│       ├── auth
│       ├── consumption
│       ├── energyops
│       ├── analytics
│       ├── maintenance
│       ├── education
│       └── audit
├── application
│   ├── auth
│   ├── consumption
│   ├── energyops
│   ├── analytics
│   ├── maintenance
│   ├── education
│   ├── audit
│   ├── port
│   │   ├── in
│   │   └── out
│   └── service
├── domain
│   ├── auth
│   ├── consumption
│   ├── energyops
│   ├── analytics
│   ├── maintenance
│   ├── education
│   ├── audit
│   └── model
├── infrastructure
│   ├── audit
│   ├── config
│   ├── persistence
│   │   ├── entity
│   │   ├── repository
│   │   ├── adapter
│   │   ├── auth
│   │   ├── consumption
│   │   ├── energyops
│   │   ├── analytics
│   │   ├── maintenance
│   │   ├── education
│   │   └── audit
│   └── security
└── bootstrap
```

Los paquetes de contexto sin casos de uso backend activos se mantienen como marcadores `package-info.java`. No contienen logica falsa.

## Reglas de dependencia

```text
api.rest.* -> application.port.in
application.service -> application.port.out + domain
domain -> Java puro
infrastructure.persistence.adapter -> application.port.out + JPA repositories/entities
infrastructure.security -> application.port.out + Spring Security/JWT
bootstrap -> Spring Boot startup
```

Validaciones actuales:

- Los controllers no llaman repositories.
- Application no importa API ni Infrastructure.
- Domain no importa Spring, JPA, API ni Infrastructure.
- Infrastructure implementa puertos de salida de Application.
- Entities JPA estan en `infrastructure.persistence.entity`.
- Repositories Spring Data estan en `infrastructure.persistence.repository`.
- Adapters estan en `infrastructure.persistence.adapter`.

## Bounded contexts

| Contexto | Estado | Representacion actual |
|---|---|---|
| `auth/iam` | Implementado parcialmente | `api.rest.auth.AuthController`, casos de uso `LoginUseCase`/`RegisterUserUseCase`, `AuthApplicationService`, dominio historico en `domain.model`, persistencia IAM en entities/repositories/adapters, JWT/BCrypt en `infrastructure.security`. |
| `consumption` | MVP/conceptual | Package markers en API/Application/Domain/Infrastructure. La lectura de consumo existe tecnicamente en SQL `[consumo].[lectura]` y se persiste hoy desde el flujo `energyops`. |
| `energyops` | Implementado | Controller de lectura/anomalia, caso de uso `AnalyzeEnergyReadingUseCase`, servicio `EnergyAnalysisService`, reglas puras en `domain.energyops`, adapters de lectura/baseline/anomalia. |
| `analytics` | Implementado para MVP | Controller de dashboard/KPIs/anomalias, casos de uso de consulta, `AnalyticsQueryService`, adapter de metricas y lectura derivada desde EnergyOps. |
| `maintenance` | MVP/conceptual | Package markers y enums minimos de dominio (`TicketStatus`, `TicketPriority`). La DB y el frontend mock ya existen; no hay backend activo todavia. |
| `education` | MVP/conceptual | Package markers y enum minimo `ChallengeStatus`. La DB y el frontend mock ya existen; no hay backend activo todavia. |
| `audit` | Transversal implementado parcialmente | `@Audited`, `AuditAspect`, `AuditPort`, `AuditEvent`, `AuditAdapter` y entities/repositories de audit. Se aplica sobre mutaciones HTTP actuales. |

## Flujo lectura -> anomalia -> KPI

```text
POST /api/energyops/analyze-reading
  -> api.rest.energyops.EnergyOpsController
  -> application.port.in.AnalyzeEnergyReadingUseCase
  -> application.energyops.service.EnergyAnalysisService
  -> domain.energyops.EnergyAnalysisPolicy
       - calcula desviacion contra baseline
       - evalua tolerancia
       - clasifica severidad
       - calcula exceso, score e impactos
  -> application.port.out.SaveEnergyReadingPort
  -> infrastructure.persistence.adapter.EnergyReadingPersistenceAdapter
  -> infrastructure.persistence.repository.EnergyReadingRepository
  -> SQL Server [consumo].[lectura]

  si hay anomalia:
  -> application.port.out.SaveEnergyAnomalyPort
  -> infrastructure.persistence.adapter.EnergyAnomalyPersistenceAdapter
  -> infrastructure.persistence.repository.EnergyAnomalyRepository
  -> SQL Server [energiaops].[anomalia]
```

Luego el dashboard:

```text
GET /api/analytics/dashboard
  -> api.rest.analytics.AnalyticsController
  -> application.port.in.GetAnalyticsDashboardUseCase
  -> application.analytics.service.AnalyticsQueryService
  -> application.port.out.LoadAnalyticsDashboardPort
  -> infrastructure.persistence.adapter.AnalyticsDashboardPersistenceAdapter
  -> Spring Data repositories
  -> SQL Server
```

Los KPIs del MVP se calculan on-the-fly desde lecturas, anomalias y baseline. No se cambio la base de datos ni los contratos HTTP.

## Contextos implementados

- `auth/iam`: login, registro admin, roles, JWT y hashing.
- `energyops`: analisis de lectura, baseline, deteccion de anomalia e impacto.
- `analytics`: dashboard, KPIs y anomalias recientes.
- `audit`: auditoria transversal de mutaciones HTTP y persistencia en tablas audit.

## Contextos MVP/conceptuales

- `consumption`: existe como schema SQL y entidad tecnica de lectura; falta caso de uso propio para registrar/consultar consumo fuera del flujo EnergyOps.
- `maintenance`: existe en schema SQL y frontend mock; falta backend de tickets, SLA, evidencias y asignaciones.
- `education`: existe en schema SQL y frontend mock; falta backend de retos, ranking, progreso y medallas.

Estos contextos tienen representacion minima para mostrar el limite modular sin introducir clases sin comportamiento.

## Fase 2

- Separar `consumption` de `energyops` con casos de uso propios de lectura y consulta.
- Migrar `domain.model.User`, `Role` y value objects hacia `domain.auth` cuando se aborde auth de extremo a extremo.
- Evaluar si `@Audited` y los adapters de auditoria deben permanecer en backend o evolucionar a un modulo transversal adicional.
- Crear modelos de lectura propios para Analytics y dejar de reutilizar records internos de EnergyOps.
- Implementar `maintenance` con tickets, SLA, asignaciones, checklist y evidencias.
- Implementar `education` con retos, ranking, medallas y progreso.
- Sustituir endpoints demo `permitAll` por JWT/RBAC.
- Agregar tests ArchUnit para bloquear dependencias indebidas entre capas.

## Core Platform migrado

Se agrego un modulo Maven real en `core-plataform/core-platform` y un `pom.xml` raiz agregador.

La direccion de dependencia actual es:

```text
backend -> core-platform
```

Core Platform contiene solo responsabilidades transversales sin Spring, JPA ni infraestructura concreta:

- `com.energiaclara.core.model.vo.TenantId`
- `com.energiaclara.core.model.vo.UserId`
- `com.energiaclara.core.security.AuthenticatedUser`
- `com.energiaclara.core.security.TenantContextHolder`
- `com.energiaclara.core.audit.AuditEvent`

Estas clases fueron extraidas desde el backend porque son usadas por mas de un contexto:

- IAM usa `TenantId`, `UserId` y `AuthenticatedUser`.
- Seguridad usa `TenantContextHolder`.
- Auditoria usa `AuditEvent`, `TenantId`, `UserId` y `AuthenticatedUser`.
- Los flujos operativos siguen consumiendo tenant y usuario sin conocer detalles de persistencia.

No se movieron:

- Controllers.
- Servicios de aplicacion.
- JWT/AuthController.
- Entidades JPA.
- Repositorios.
- EnergyOps.
- Analytics.

No se cambiaron:

- Endpoints `/api/...`.
- JSON de request/response.
- Frontend.
- Base de datos.

## Como defenderlo ante el docente

1. Es monolito modular, no microservicios: todos los contextos conviven en un solo deploy, pero con limites internos claros.
2. La arquitectura es transversal porque las capas (`api`, `application`, `domain`, `infrastructure`, `bootstrap`) son el eje principal; los contexts se representan dentro de cada capa.
3. La direccion de dependencias respeta hexagonal: API entra por puertos de entrada; Infrastructure sale por puertos de salida; Domain no conoce frameworks.
4. No se crearon clases falsas para aparentar completitud. Los contexts sin backend activo estan marcados y documentados como MVP/conceptuales.
5. El flujo critico de negocio ya demuestra la arquitectura: una lectura HTTP pasa por controller, caso de uso, regla pura de dominio, puerto de salida, adapter JPA y SQL Server.
6. La base de datos canonica se respeta: no se cambiaron tablas, columnas ni scripts.
7. Los contratos publicos se conservan: las rutas y JSON actuales siguen iguales.

## IA hibrida explicable integrada

Se agrego una integracion controlada entre EnergyOps y `ai-service`, manteniendo la aplicacion como monolito modular.

Direccion de dependencia:

```text
backend -> ai-application -> ai-domain
backend -> ai-infrastructure -> ai-application -> ai-domain
```

Piezas nuevas en backend:

- `EnergyAiAnalysisPort`: puerto de salida de la aplicacion EnergyOps.
- `EnergyAiAnalysisRequest`: DTO interno para enviar lectura, baseline y contexto a IA.
- `EnergyAiAnalysisOutcome`: DTO interno para devolver resultado compatible con EnergyOps.
- `HybridEnergyAiAnalysisAdapter`: adapter de infraestructura que usa historial real de lecturas y llama al caso de uso de IA.

Flujo:

```text
EnergyAnalysisService
  -> EnergyAiAnalysisPort
      -> HybridEnergyAiAnalysisAdapter
          -> EnergyReadingRepository
          -> AnalyzeEnergyWithAiUseCase
              -> HybridEnergyAiEngine
```

Reglas de seguridad arquitectonica:

- `ai-domain` no importa Spring, JPA ni backend.
- `ai-application` trabaja por puertos y no accede a SQL Server.
- El adapter Spring/JPA vive en backend infrastructure, porque ahi estan los repositories reales.
- EnergyOps conserva sus calculos anteriores como fallback.

Contratos preservados:

- No cambio `POST /api/energyops/analyze-reading`.
- No cambio el request JSON.
- No cambio el response JSON.
- No cambio frontend.
- No cambio base de datos.
- No cambio login/JWT.
- `GET /api/analytics/anomalies` sigue funcionando y puede mostrar explicaciones dinamicas cuando la anomalia fue generada con IA.

Decision sobre metadatos IA:

No se agregaron `aiUsed`, `confidence` ni `modelVersion` al response publico de EnergyOps para no romper consumidores actuales. Esos datos quedan como informacion interna de integracion y se podrian exponer en una version futura del contrato.

## IAM migrado parcialmente a iam-service

Se agrego `iam-service` como modulo Maven interno real para iniciar la separacion enterprise de identidad y acceso.

Direccion de dependencia:

```text
backend -> iam-service -> core-platform
backend -> core-platform
```

Responsabilidades movidas:

- Dominio IAM: usuario, rol y email.
- Application IAM: login, registro, comandos/resultados internos y puertos.

Responsabilidades que permanecen temporalmente en backend:

- `AuthController`, porque conserva rutas y JSON publicos.
- DTOs REST de auth, porque son contrato HTTP.
- `SecurityConfig`, porque gobierna seguridad web de toda la aplicacion.
- `JwtAuthFilter`, porque interpreta Bearer para todos los contextos.
- `JwtTokenAdapter`, porque el filtro global todavia lo usa.
- `BCryptPasswordHasherAdapter`, porque depende de Spring Security.
- Entidades y repositories JPA de IAM, porque el escaneo JPA actual vive en backend.

Contratos preservados:

- No cambio `POST /api/auth/login`.
- No cambio `POST /api/auth/register`.
- No cambio request/response JSON.
- No cambio frontend.
- No cambio SQL Server.
- No cambio IA.
- No cambio EnergyOps.
- No cambio Analytics.

Validacion:

- `mvn clean test`: exitoso.
- `mvn install -DskipTests`: exitoso.
- Backend arranco con SQL Server.
- Login HTTP `200`.
- EnergyOps con Bearer HTTP `201`.
- Analytics con Bearer HTTP `200`.

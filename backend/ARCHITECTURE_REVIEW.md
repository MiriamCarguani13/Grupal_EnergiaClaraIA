# Architecture Review

## Alcance

Revision del backend EnergiaClara AI contra la arquitectura transversal/hexagonal pedida:

- Capas base: `com.energiaclara.api`, `application`, `domain`, `infrastructure`, `bootstrap`.
- Bounded contexts objetivo: `auth`, `consumption`, `energyops`, `analytics`, `maintenance`, `education`, `audit`.
- Sin cambios de contratos HTTP, frontend ni base de datos.

## Estructura Base Creada

Se preparo una estructura transversal minima con marcadores `package-info.java` para representar los bounded contexts sin mover logica ni registrar beans nuevos.

```text
com.energiaclara
├── api
│   └── rest
│       ├── analytics
│       ├── audit
│       ├── auth
│       ├── consumption
│       ├── education
│       ├── energyops
│       └── maintenance
├── application
│   ├── analytics
│   ├── audit
│   ├── auth
│   ├── consumption
│   ├── education
│   ├── energyops
│   └── maintenance
├── domain
│   ├── analytics
│   ├── audit
│   ├── auth
│   ├── consumption
│   ├── education
│   ├── energyops
│   └── maintenance
├── infrastructure
│   ├── audit
│   ├── config
│   ├── persistence
│   │   ├── adapter
│   │   ├── entity
│   │   ├── repository
│   │   ├── auth
│   │   ├── consumption
│   │   ├── education
│   │   └── maintenance
│   └── security
└── bootstrap
```

La clase main sigue en `com.energiaclara.bootstrap.EnergiaclaraApplication`.

## Bounded Contexts Representados

- `auth`: estructura preparada para autenticacion, usuarios, roles y permisos. La logica actual sigue en `AuthController`, `AuthApplicationService`, `domain.model` e `infrastructure.persistence`.
- `consumption`: estructura preparada para lecturas de consumo. La logica actual de lectura sigue integrada al flujo de `energyops`.
- `energyops`: ya contiene controller, casos de uso, puertos, dominio y adapters activos.
- `analytics`: ya contiene controller, casos de uso, puertos y adapters de consulta activos.
- `maintenance`: estructura preparada para tickets, SLA, asignaciones y evidencias. Hoy existe en DB/frontend mock, sin backend activo.
- `education`: estructura preparada para retos, rankings y medallas. Hoy existe en DB/frontend mock, sin backend activo.
- `audit`: ya tiene anotacion REST, aspecto, puerto, modelo y persistencia; tambien queda representado como contexto por capa.

## Estado Final de Bounded Contexts

| Contexto | API | Application | Domain | Infrastructure | Estado |
|---|---|---|---|---|---|
| `auth/iam` | `api.rest.auth.AuthController` | `LoginUseCase`, `RegisterUserUseCase`, `AuthApplicationService` | `domain.model.User`, `Role`, VOs | IAM entities/repositories/adapters, JWT, BCrypt | Implementado parcial/MVP |
| `consumption` | marcador `api.rest.consumption` | marcador `application.consumption` | `MeterStatus` y marcador | marcador `infrastructure.persistence.consumption`; lectura persistida tecnicamente como entity | Conceptual/MVP |
| `energyops` | `EnergyOpsController` | `AnalyzeEnergyReadingUseCase`, `EnergyAnalysisService` | `AnomalyType`, `AnomalySeverity`, `EnergyBaseline`, `EnergyAnalysisPolicy` | adapters/repositories/entities de lectura, baseline y anomalia | Implementado |
| `analytics` | `AnalyticsController` | casos de uso de dashboard/KPIs/anomalias, `AnalyticsQueryService` | marcador `domain.analytics` | `AnalyticsDashboardPersistenceAdapter` y lecturas derivadas via repositories | Implementado MVP |
| `maintenance` | marcador `api.rest.maintenance` | marcador `application.maintenance` | `TicketStatus`, `TicketPriority` | marcador `infrastructure.persistence.maintenance` | Conceptual/MVP |
| `education` | marcador `api.rest.education` | marcador `application.education` | `ChallengeStatus` | marcador `infrastructure.persistence.education` | Conceptual/MVP |
| `audit` | `api.rest.audit.Audited` | `AuditPort` y marcador `application.audit` | `AuditEvent` historico y marcador `domain.audit` | `AuditAspect`, `AuditAdapter`, audit entities/repositories | Transversal implementado parcial |

No se agrego logica falsa para completar carpetas. Los contextos sin backend activo se representan como limites modulares y quedan documentados para fase 2.

## Controllers Migrados o Validados

- `com.energiaclara.api.rest.auth.AuthController`: migrado a la capa API transversal del contexto `auth`. Conserva las rutas `/api/auth/login` y `/api/auth/register`, los mismos DTOs REST y las mismas llamadas a `LoginUseCase` y `RegisterUserUseCase`.
- `com.energiaclara.api.rest.energyops.EnergyOpsController`: validado. Ya estaba en `api.rest.energyops`, conserva `/api/energyops/analyze-reading` y llama solo a `AnalyzeEnergyReadingUseCase`.
- `com.energiaclara.api.rest.analytics.AnalyticsController`: validado. Ya estaba en `api.rest.analytics`, conserva `/api/analytics/dashboard`, `/api/analytics/kpis` y `/api/analytics/anomalies`, y llama solo a puertos de entrada de application.
- `com.energiaclara.api.rest.GlobalExceptionHandler`: se mantiene como advice global de API REST. No pertenece a un bounded context funcional ni expone endpoints propios.

## Endpoints Conservados

- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/energyops/analyze-reading`
- `GET /api/analytics/dashboard`
- `GET /api/analytics/kpis`
- `GET /api/analytics/anomalies`

## Application Migrada o Validada

- Puertos de entrada principales centralizados en `com.energiaclara.application.port.in`:
  - `LoginUseCase`
  - `RegisterUserUseCase`
  - `AnalyzeEnergyReadingUseCase`
  - `GetAnalyticsDashboardUseCase`
  - `GetKpiSnapshotsUseCase`
  - `GetAnomaliesUseCase`
- Puertos de salida principales centralizados en `com.energiaclara.application.port.out`:
  - `UserRepositoryPort`
  - `PasswordHasherPort`
  - `TokenPort`
  - `AuditPort`
  - `SaveEnergyReadingPort`
  - `FindEnergyBaselinePort`
  - `SaveEnergyAnomalyPort`
  - `SaveEnergyKpiSnapshotPort`
  - `LoadAnalyticsDashboardPort`
  - `LoadKpiSnapshotsPort`
  - `LoadAnomaliesPort`
- `AuthApplicationService`, `EnergyAnalysisService` y `AnalyticsQueryService` implementan puertos de entrada de application y orquestan mediante puertos de salida.
- Los controllers activos dependen de `application.port.in`, no de servicios concretos ni repositories.
- Los adapters de infrastructure implementan `application.port.out`; no se movieron entities JPA ni repositories.
- Se valido que `application` no importa paquetes `api` ni `infrastructure`.

## Domain Migrado o Validado

- Se valido que `domain` no importa `org.springframework`, `jakarta.persistence`, `javax.persistence`, controllers, DTOs de API, repositories JPA ni adapters de infrastructure.
- `domain.energyops` concentra enums y reglas puras de analisis energetico:
  - `AnomalySeverity`
  - `AnomalyType`
  - `EnergyBaseline`
  - `EnergyAnalysisPolicy`
- `EnergyAnalysisPolicy` contiene calculos puros compartidos por application:
  - desviacion porcentual contra baseline
  - deteccion por tolerancia
  - exceso de kWh
  - severidad de anomalia
  - score de anomalia
  - redondeo de porcentajes e impactos
- `EnergyAnalysisService` y `AnalyticsQueryService` delegan esos calculos en domain, manteniendo la orquestacion y persistencia por puertos en application.
- Se agregaron enums conceptuales minimos para contextos aun no implementados:
  - `domain.consumption.MeterStatus`
  - `domain.maintenance.TicketStatus`
  - `domain.maintenance.TicketPriority`
  - `domain.education.ChallengeStatus`

## Infrastructure Migrada o Validada

- Entities JPA activas ubicadas en `com.energiaclara.infrastructure.persistence.entity`:
  - `UserEntity`, `RoleEntity`, `UserRoleEntity`
  - `EnergyReadingEntity`, `EnergyAnomalyEntity`, `EnergyBaselineEntity`
  - `AuditLogEntity`, `AuditChangeEntity`
- Repositories Spring Data ubicados en `com.energiaclara.infrastructure.persistence.repository`:
  - `UserJpaRepository`, `RoleJpaRepository`, `UserRoleJpaRepository`
  - `EnergyReadingRepository`, `EnergyAnomalyRepository`, `EnergyBaselineRepository`
  - `AuditLogJpaRepository`, `AuditChangeJpaRepository`
- Persistence adapters ubicados en `com.energiaclara.infrastructure.persistence.adapter`:
  - `UserRepositoryAdapter`
  - `EnergyReadingPersistenceAdapter`
  - `EnergyAnomalyPersistenceAdapter`
  - `EnergyBaselinePersistenceAdapter`
  - `EnergyKpiSnapshotPersistenceAdapter`
  - `AnalyticsDashboardPersistenceAdapter`
  - `AuditAdapter`
- Los adapters implementan puertos de salida definidos en `application.port.out`.
- `infrastructure.security` mantiene detalles tecnicos de Spring Security, JWT, BCrypt y tenant context.
- `infrastructure.audit` mantiene el aspecto de auditoria y configuracion async.
- `bootstrap.EnergiaclaraApplication` sigue escaneando JPA bajo `com.energiaclara.infrastructure.persistence`, por lo que cubre `entity` y `repository` sin cambiar runtime.

## Que Cumple

- La aplicacion ya esta organizada por capas transversales bajo `com.energiaclara`.
- `api.rest.energyops.EnergyOpsController` y `api.rest.analytics.AnalyticsController` dependen de puertos de entrada en `application.port.in` (`AnalyzeEnergyReadingUseCase`, `GetAnalyticsDashboardUseCase`, `GetAnomaliesUseCase`, `GetKpiSnapshotsUseCase`) y no llaman repositories JPA.
- `application.energyops.service.EnergyAnalysisService` usa puertos de salida (`SaveEnergyReadingPort`, `FindEnergyBaselinePort`, `SaveEnergyAnomalyPort`) para persistencia.
- `application.analytics.service.AnalyticsQueryService` usa puertos de salida (`LoadAnalyticsDashboardPort`, `LoadKpiSnapshotsPort`, `LoadAnomaliesPort`, `FindEnergyBaselinePort`) para consultas.
- `domain` contiene modelos, value objects y enums sin dependencias de Spring, JPA, Hibernate ni infraestructura.
- `domain.energyops.EnergyAnalysisPolicy` contiene reglas de negocio puras que antes estaban duplicadas o embebidas en servicios de application.
- `infrastructure.persistence.entity`, `infrastructure.persistence.repository` e `infrastructure.persistence.adapter` contienen respectivamente entities JPA, repositories Spring Data y adapters que implementan puertos.
- `infrastructure.security` contiene la configuracion Spring Security, filtro JWT y adapter de tokens.
- `infrastructure.audit` y `infrastructure.persistence.adapter.AuditAdapter` concentran el aspecto de auditoria y la persistencia del log.
- `bootstrap.EnergiaclaraApplication` queda como punto unico de arranque.
- Los paquetes base de los bounded contexts objetivo ya existen como estructura minima sin alterar el comportamiento runtime.
- Los controllers activos estan ubicados bajo `com.energiaclara.api.rest.<contexto>` cuando pertenecen a un contexto funcional.
- `ARCHITECTURE_TRANSVERSAL.md` documenta el monolito modular transversal/hexagonal, el flujo lectura-anomalia-KPI y la defensa tecnica ante el docente.

## Que No Cumple o Esta Parcial

- Los bounded contexts `consumption`, `maintenance` y `education` aun no tienen casos de uso backend activos. Hoy existen como estructura de paquete, schema/base de datos y pantallas/mock en frontend.
- `auth` ya tiene controller en `api.rest.auth`, pero application/domain/infrastructure siguen parcialmente modelados en paquetes transversales historicos (`application.service`, `application.port`, `domain.model`, `infrastructure.persistence/security`) en lugar de tener su logica dentro de `*.auth`.
- `application` aun usa anotaciones Spring (`@Service`, `@Transactional`, `@Value`) para wiring y transacciones. Es aceptable para MVP Spring Boot, pero una hexagonal estricta moveria ese wiring a configuracion/adapters.
- `api.rest.audit.Audited` esta declarado en API y consumido por `infrastructure.audit.AuditAspect`. Funciona como cross-cutting concern, pero una version mas estricta podria mover la anotacion a un paquete transversal compartido.
- `AnalyticsQueryService` consume records del contexto `energyops` para construir KPIs. Es pragmatico para MVP, pero a futuro conviene introducir modelos/puertos de lectura propios de analytics.
- `application.analytics` todavia reutiliza DTOs internos de `application.energyops` para lecturas/anomalias recientes. Queda pendiente separar modelos de lectura de analytics sin tocar comportamiento.
- `domain.analytics`, `domain.consumption`, `domain.maintenance` y `domain.education` aun son conceptuales/minimos; los agregados reales deben introducirse cuando existan casos de uso backend.
- `domain.model` mantiene modelos historicos de auth (`User`, `Role`, value objects) en lugar de estar bajo `domain.auth`; moverlos requeriria refactor de imports y adapters.
- `domain.model.audit.AuditEvent` sigue en ruta historica. Puede migrarse a `domain.audit` cuando se aborde audit como bounded context completo.
- Los paquetes `application.analytics.port.*` y `application.energyops.port.*` quedaron vacios como resultado de centralizar puertos; pueden eliminarse en una limpieza posterior si no se usan como convencion de contexto.
- Los paquetes marcadores `infrastructure.persistence.auth`, `consumption`, `education` y `maintenance` existen como reserva de contexto, pero las clases activas de persistencia estan organizadas por tipo tecnico (`entity`, `repository`, `adapter`) para cumplir la regla actual de Infrastructure.
- `SecurityConfig` mantiene `permitAll` temporal en `/api/energyops/analyze-reading` y `/api/analytics/**` para la demo local.

## Que Se Corrigio

- Se movio el puerto de repositorio de usuarios desde `domain.port.out.UserRepositoryPort` hacia `application.port.out.UserRepositoryPort`, alineando los puertos de salida con la capa de aplicacion.
- `AuthApplicationService` dejo de depender directamente de `PasswordEncoder` de Spring Security.
- Se creo `application.port.out.PasswordHasherPort` como puerto de salida para hashing/verificacion de contrasenas.
- Se creo `infrastructure.security.BCryptPasswordHasherAdapter` como adapter que implementa el puerto usando `PasswordEncoder`.
- `UserRepositoryAdapter` ahora implementa el puerto desde `application.port.out`.
- `bootstrap.EnergiaclaraApplication` restringe `@EnableJpaRepositories` y `@EntityScan` a `com.energiaclara.infrastructure.persistence`, evitando que JPA escanee toda la aplicacion.
- Se agregaron paquetes base para `auth`, `consumption`, `maintenance`, `education` y los contexts faltantes por capa mediante `package-info.java`, sin mover controllers, services, entities ni repositories.
- Se migro `AuthController` desde `api.rest` hacia `api.rest.auth`, sin cambiar rutas, payloads, respuestas ni casos de uso invocados.
- Se centralizaron los puertos activos de EnergyOps y Analytics desde subpaquetes contextuales hacia `application.port.in` y `application.port.out`.
- Se actualizaron controllers, services, adapters y tests para depender de los puertos centralizados, sin cambiar endpoints, JSON ni persistencia.
- Se movieron calculos puros de EnergyOps/Analytics hacia `domain.energyops.EnergyAnalysisPolicy`.
- Se creo `domain.energyops.EnergyBaseline` como value object minimo para baseline esperado y tolerancia.
- Se agregaron enums minimos de dominio para consumption, maintenance y education sin conectarlos aun a persistence ni API.
- Se movieron entities JPA activas a `infrastructure.persistence.entity`.
- Se movieron repositories Spring Data activos a `infrastructure.persistence.repository`.
- Se movieron adapters de persistencia activos a `infrastructure.persistence.adapter`.
- Se corrigieron imports internos de Infrastructure para que los adapters sigan implementando puertos de salida de `application.port.out`.
- Se completaron marcadores de Infrastructure para `analytics`, `energyops` y `audit` como contexts de persistencia, sin mover logica ni crear beans.
- Se actualizo `ARCHITECTURE_TRANSVERSAL.md` con el estado final de arquitectura, contextos implementados, contextos MVP/conceptuales y fase 2.

## Proximos Pasos

- Migrar gradualmente la logica de autenticacion hacia `auth` manteniendo las rutas `/api/auth/**`.
- Separar `consumption` de `energyops` cuando se creen casos de uso propios para registrar y consultar lecturas.
- Introducir puertos de lectura propios para analytics y evitar depender de DTOs/records internos de energyops.
- Crear agregados reales de `consumption`, `analytics`, `maintenance` y `education` cuando existan casos de uso backend, evitando duplicar el schema SQL como clases de dominio sin comportamiento.
- Migrar `domain.model.User`, `Role` y VOs hacia `domain.auth` solo cuando se aborde auth de extremo a extremo.
- Migrar `domain.model.audit.AuditEvent` hacia `domain.audit` solo cuando se formalice audit como bounded context completo.
- Mover gradualmente services, commands/results, puertos, modelos de dominio y adapters de auth a paquetes `*.auth`, con pruebas de contrato antes de tocar comportamiento.
- Mantener controllers sin acceso directo a repositories y sin calculos de negocio; cualquier regla nueva debe entrar por application/domain.
- Evaluar si se mantendra la convencion de puertos centralizados (`application.port.in/out`) o si se agregaran subcarpetas por contexto debajo de esos paquetes sin romper imports publicos.
- Extraer configuracion de valores demo (`demoTenantId`, `demoMedidorId`, costos, tolerancias) a una clase de propiedades de infraestructura/configuracion si se busca una aplicacion mas pura.
- Extraer propiedades tecnicas de SQL/JWT/demo hacia clases `@ConfigurationProperties` en `infrastructure.config` cuando se haga una limpieza de configuracion.
- Implementar `maintenance` desde los contratos de tickets/SLA/evidencias cuando se reemplacen los mocks del frontend.
- Implementar `education` desde los contratos de retos/ranking/medallas cuando se reemplacen los mocks del frontend.
- Proteger endpoints demo con JWT/RBAC antes de produccion.
- Agregar tests de arquitectura con ArchUnit para bloquear dependencias indebidas entre capas.

# Architecture Gap Analysis - EnergiaClara AI

## 1. Estructura actual

El proyecto esta dividido fisicamente en:

```text
EnergiaClara-IA/
├── backend/      # Spring Boot 3.2, Java 21, Maven
├── frontend/     # React + Vite
├── database/     # SQL Server schema y seeds
└── Pantallas/    # mockups HTML estaticos
```

### Backend

La estructura Java activa ya esta parcialmente migrada a capas transversales bajo `com.energiaclara`:

```text
com.energiaclara
├── api
│   └── rest
│       ├── analytics
│       ├── audit
│       ├── dto
│       └── energyops
├── application
│   ├── analytics
│   ├── dto
│   ├── energyops
│   ├── port
│   ├── security
│   └── service
├── bootstrap
├── domain
│   ├── energyops
│   ├── model
│   └── port
└── infrastructure
    ├── audit
    ├── persistence
    │   ├── analytics
    │   ├── audit
    │   └── energyops
    └── security
```

Controllers actuales:

- `AuthController`: expone `/api/auth/login` y `/api/auth/register`.
- `EnergyOpsController`: expone `/api/energyops/analyze-reading`.
- `AnalyticsController`: expone `/api/analytics/dashboard`, `/api/analytics/kpis`, `/api/analytics/anomalies`.
- `GlobalExceptionHandler`: manejo REST de errores.

Services actuales:

- `AuthApplicationService`: login y registro.
- `EnergyAnalysisService`: analisis de lectura, baseline, anomalia y calculos demo.
- `AnalyticsQueryService`: dashboard, KPIs y anomalias recientes.

Entities y repositories actuales:

- IAM/auth en `infrastructure.persistence`: `UserEntity`, `RoleEntity`, `UserRoleEntity` y repositories JPA.
- EnergyOps/consumo en `infrastructure.persistence.energyops`: entities de lectura, anomalia y baseline con repositories y adapters.
- Audit en `infrastructure.persistence.audit`: entities y repositories para evento/log de auditoria.
- Analytics no tiene tabla/adaptador persistente propio completo; usa adapter de dashboard y datos derivados de lecturas/anomalias.

Security actual:

- `SecurityConfig`, `JwtAuthFilter`, `JwtTokenAdapter`, `BCryptPasswordHasherAdapter`, `TenantContextHolder`.
- JWT y BCrypt estan encapsulados en infraestructura, con puertos de aplicacion para token/hash.
- `/api/energyops/analyze-reading` y `/api/analytics/**` estan abiertos por configuracion demo temporal.

Configuracion actual:

- `bootstrap.EnergiaclaraApplication` arranca Spring Boot y limita `@EntityScan`/`@EnableJpaRepositories` a `infrastructure.persistence`.
- `application.yml` contiene datasource SQL Server, JWT, parametros demo de energyops, costos y puerto.
- Hibernate esta con `ddl-auto: none`; la DB es fuente canonica.

Frontend actual:

- React + Vite.
- Estructura por tipo tecnico: `components`, `context`, `pages`, `services`, `styles`.
- Servicios reales para `auth`, `energyops` y `analytics`.
- `mockService.js` mantiene datos mock para tickets/mantenimiento y retos/educacion.
- Rutas de UI cubren dashboard, lecturas, anomalias, creacion de ticket, retos y vistas mobile.

Database actual:

- `database/script.sql` define schemas: `iam`, `core`, `consumo`, `energiaops`, `audit`, `analitica`, `educacion`, `mantenimiento`.
- `database/seeds.sql` agrega seeds demo y columnas auxiliares usadas por backend.
- La base de datos ya expresa mas bounded contexts que el backend activo.

## 2. Estructura esperada

La arquitectura objetivo transversal/hexagonal deberia mantener las capas de primer nivel:

```text
com.energiaclara
├── api
├── application
├── domain
├── infrastructure
└── bootstrap
```

Dentro de cada capa, los bounded contexts deberian ser explicitos y consistentes:

```text
api.rest
├── iam
├── consumption
├── energyops
├── analytics
├── maintenance
├── education
└── audit

application
├── iam
├── consumption
├── energyops
├── analytics
├── maintenance
├── education
└── audit

domain
├── iam
├── consumption
├── energyops
├── analytics
├── maintenance
├── education
└── audit

infrastructure
├── persistence
│   ├── iam
│   ├── consumption
│   ├── energyops
│   ├── analytics
│   ├── maintenance
│   ├── education
│   └── audit
├── security
├── audit
└── config
```

Reglas esperadas:

- `api` solo conoce contratos REST y puertos de entrada de `application`.
- `application` contiene casos de uso, puertos `in/out`, transacciones y orquestacion; no conoce JPA, controllers ni detalles HTTP.
- `domain` contiene entidades/VOs/reglas puras; no depende de Spring, JPA, Web, Security ni SQL.
- `infrastructure` implementa puertos de salida, seguridad, persistencia, clientes externos y configuracion tecnica.
- `bootstrap` queda como arranque y wiring general.
- Cada bounded context debe tener lenguaje, modelos y puertos propios. Las integraciones entre contextos deben ocurrir por puertos/eventos/DTOs de aplicacion, no por reutilizacion directa de modelos internos.

## 3. Que ya cumple

- Las capas base `api`, `application`, `domain`, `infrastructure` y `bootstrap` ya existen.
- Controllers de `energyops` y `analytics` dependen de puertos de entrada, no de repositories JPA.
- `AuthController` depende de `LoginUseCase` y `RegisterUserUseCase`.
- Entities JPA y Spring Data repositories estan concentrados en `infrastructure.persistence`.
- El dominio activo no muestra dependencias directas hacia `api` o `infrastructure`.
- Puertos de salida existen para usuarios, token, hashing, auditoria, energyops y analytics.
- `bootstrap` ya restringe scan de JPA a infraestructura.
- Seguridad tecnica vive en `infrastructure.security`.
- La DB ya separa schemas funcionales alineables con bounded contexts.
- El frontend ya consume backend mediante servicios por area (`authService`, `energyOpsService`, `analyticsService`), lo que facilita una migracion gradual de contratos.
- Hay un test smoke de application que verifica wiring de casos de uso principales sin levantar infraestructura real.

## 4. Que no cumple

- `iam` no esta expresado como bounded context explicito. Auth vive en paquetes genericos: `api.rest`, `application.service`, `application.dto`, `application.port`, `domain.model` e `infrastructure.persistence`.
- `consumption` no existe como bounded context backend, aunque la tabla central de lecturas vive en schema SQL `consumo`. Hoy la escritura de lectura esta acoplada al flujo `energyops`.
- `maintenance` no tiene backend activo; existe en schema SQL y pantallas/frontend mock.
- `education` no tiene backend activo; existe en schema SQL y pantallas/frontend mock.
- `analytics` depende de records internos de `application.energyops` para construir KPIs y anomalias. Esto mezcla modelos de lectura de un contexto con otro.
- `audit` esta repartido entre `api.rest.audit`, `infrastructure.audit`, `application.port.out.AuditPort` y `domain.model.audit`; funciona, pero el bounded context no esta uniformemente nombrado por capa.
- `application` todavia contiene dependencias Spring (`@Service`, `@Transactional`, `@Value`). Es pragmatico para Spring Boot, pero no es hexagonal estricta.
- `EnergyAnalysisService` contiene parametros demo e IDs fijos inyectados por `@Value`; eso mezcla configuracion de demo con caso de uso.
- `SecurityConfig` mantiene endpoints de analytics/energyops en `permitAll`, lo cual no cumple un modelo IAM/RBAC real.
- `application.security.AuthenticatedUser` representa datos de seguridad en application, pero es usado como principal Spring en API/security. Conviene definir si es modelo de aplicacion o detalle de infraestructura.
- `domain/port/out` existe como carpeta vacia, lo que sugiere una migracion incompleta o una convencion abandonada.
- Frontend esta organizado por tipo tecnico y paginas, no por bounded contexts. Eso no impide la migracion backend, pero puede hacer crecer dependencias cruzadas en UI.
- Database y backend no estan al mismo nivel funcional: SQL ya tiene schemas para todos los contextos, backend solo implementa auth/iam parcial, energyops, analytics parcial y audit parcial.
- No hay tests de arquitectura tipo ArchUnit para impedir regresiones entre capas.

## 5. Riesgos

- Romper contratos REST existentes al mover paquetes sin congelar primero los endpoints y DTOs publicos.
- Duplicar modelos entre `consumption`, `energyops` y `analytics` sin definir limites: lectura, baseline, anomalia y KPI pueden terminar acoplados en tres direcciones.
- Migrar `iam` tarde puede bloquear seguridad/RBAC en los demas contextos.
- Mover entities/repositories antes de estabilizar adapters puede romper mapeos SQL Server y seeds demo.
- Endpoints abiertos temporalmente pueden ocultar errores de autorizacion que apareceran al activar JWT/RBAC.
- La DB canonica esta fuera de Hibernate; cualquier cambio de persistencia debe respetar nombres, schemas y columnas existentes.
- Frontend usa mocks para maintenance/education; crear backend sin contrato incremental puede obligar a rehacer pantallas.
- `AnalyticsQueryService` calcula snapshots on-the-fly; si se introduce persistencia real en `analitica`, hay riesgo de inconsistencias entre calculo historico y calculo derivado.
- Auditoria por aspecto sobre controllers es util, pero puede registrar datos incompletos si se mueven endpoints o se introducen casos de uso no HTTP.

## 6. Orden recomendado de migracion

1. Congelar contratos actuales.
   - Mantener `API_CONTRACTS_CURRENT.md` como fuente de rutas, payloads y respuestas.
   - Agregar tests de contrato para auth, energyops y analytics antes de mover mas clases.

2. Ordenar IAM/auth como bounded context explicito.
   - Mover gradualmente a `api.rest.iam`, `application.iam`, `domain.iam`, `infrastructure.persistence.iam`.
   - Mantener rutas `/api/auth/**` para no romper frontend.
   - Consolidar roles, permisos, usuario, tenant y principal autenticado.

3. Separar Consumption de EnergyOps.
   - Introducir `consumption` para registrar/consultar lecturas.
   - Dejar que `energyops` consuma lecturas mediante puertos o comandos, no mediante ownership directo de persistence de `consumo`.
   - Renombrar con cuidado: el schema SQL es `consumo`, pero el bounded context objetivo es `consumption`.

4. Fortalecer EnergyOps.
   - Mantener deteccion de anomalias, baseline y recomendaciones en `energyops`.
   - Sacar IDs demo y costos a propiedades/configuracion de infraestructura.
   - Definir modelos de dominio mas ricos si la logica de anomalias crece.

5. Desacoplar Analytics.
   - Crear puertos/modelos de lectura propios de analytics.
   - Evitar dependencia directa de `application.energyops.dto`.
   - Decidir si `analitica.kpi_*` sera fuente persistida o si analytics seguira como read model derivado.

6. Formalizar Audit.
   - Decidir si la anotacion `@Audited` queda en API, en un paquete transversal compartido o en `application.audit`.
   - Preparar auditoria para eventos de aplicacion, no solo mutaciones HTTP.

7. Implementar Maintenance.
   - Usar las pantallas y mocks actuales como contrato inicial.
   - Crear casos de uso para tickets, asignaciones, SLA, evidencias y cierre.
   - Integrar con anomalias de energyops por puerto/evento.

8. Implementar Education.
   - Crear casos de uso para retos, progreso, ranking y medallas.
   - Definir como consume KPIs/ahorro desde analytics sin depender de tablas internas.

9. Reorganizar frontend por dominios.
   - Mantener rutas visibles.
   - Evolucionar `pages/services` hacia modulos por contexto solo cuando existan contratos backend reales.

10. Agregar guardrails.
    - Tests ArchUnit para dependencias entre capas.
    - Tests de application con puertos fake.
    - Tests de adapters contra SQL Server o Testcontainers si el entorno lo permite.

## 7. Que NO se debe tocar todavia

- No cambiar rutas REST publicas existentes: `/api/auth/**`, `/api/energyops/**`, `/api/analytics/**`.
- No renombrar schemas, tablas ni columnas del SQL canonico sin coordinacion con DBA.
- No mover frontend a estructura por bounded contexts antes de estabilizar contratos backend para maintenance/education.
- No eliminar `mockService.js` hasta que existan endpoints reales equivalentes para tickets y retos.
- No activar cambios fuertes de RBAC en analytics/energyops sin preparar frontend, datos de roles y pruebas de autorizacion.
- No convertir `analytics` a persistencia real de KPIs sin decidir la fuente de verdad entre calculo on-the-fly y tablas `analitica`.
- No introducir carpetas vacias para todos los contexts solo por simetria; crear cada paquete cuando exista un caso de uso real.
- No refactorizar entities JPA masivamente antes de cubrir adapters con tests.
- No cambiar `ddl-auto: none`; la base canonica debe seguir controlada por scripts SQL.
- No mezclar la migracion arquitectonica con cambios funcionales grandes de IA, prediccion o UX.

# Plan de migracion hacia arquitectura tipo docente

Este documento analiza la estructura actual de EnergiaClara AI contra la arquitectura ejemplo:

```text
ProyectoAI/
├── ai-service/
│   └── modules/
│       ├── ai-api
│       ├── ai-application
│       ├── ai-bootstrap
│       ├── ai-domain
│       └── ai-infrastructure
├── core-plataform/
│   └── core-platform
├── iam-service/
└── scripts/
```

Restricciones del plan:

- No hacer reescritura completa.
- No romper el frontend.
- No cambiar endpoints actuales.
- No cambiar base de datos salvo que sea indispensable.
- Migrar por etapas, manteniendo comportamiento observable.

## 1. Estructura actual detectada

```text
EnergiaClara-IA/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/energiaclara/
│       │   │   ├── api/
│       │   │   │   └── rest/
│       │   │   │       ├── analytics/
│       │   │   │       ├── audit/
│       │   │   │       ├── auth/
│       │   │   │       ├── consumption/
│       │   │   │       ├── education/
│       │   │   │       ├── energyops/
│       │   │   │       ├── maintenance/
│       │   │   │       └── GlobalExceptionHandler.java
│       │   │   ├── application/
│       │   │   │   ├── analytics/
│       │   │   │   ├── audit/
│       │   │   │   ├── auth/
│       │   │   │   ├── consumption/
│       │   │   │   ├── dto/
│       │   │   │   ├── education/
│       │   │   │   ├── energyops/
│       │   │   │   ├── maintenance/
│       │   │   │   ├── port/in/
│       │   │   │   ├── port/out/
│       │   │   │   ├── security/
│       │   │   │   └── service/
│       │   │   ├── bootstrap/
│       │   │   │   └── EnergiaclaraApplication.java
│       │   │   ├── domain/
│       │   │   │   ├── analytics/
│       │   │   │   ├── audit/
│       │   │   │   ├── auth/
│       │   │   │   ├── consumption/
│       │   │   │   ├── education/
│       │   │   │   ├── energyops/
│       │   │   │   ├── maintenance/
│       │   │   │   └── model/
│       │   │   └── infrastructure/
│       │   │       ├── audit/
│       │   │       ├── config/
│       │   │       ├── persistence/
│       │   │       │   ├── adapter/
│       │   │       │   ├── entity/
│       │   │       │   └── repository/
│       │   │       └── security/
│       │   └── resources/
│       │       └── application.yml
│       └── test/
│           └── java/com/energiaclara/application/
├── frontend/
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── components/
│       ├── context/
│       ├── pages/
│       ├── services/
│       └── styles/
├── database/
│   ├── script.sql
│   └── seeds.sql
├── Pantallas/
└── documentacion de arquitectura existente
```

Observaciones:

- El backend ya sigue una separacion por capas tipo hexagonal dentro de un solo modulo Maven.
- Hay un solo artefacto Spring Boot: `backend/pom.xml` con `artifactId` `energiaclara-ai`.
- El frontend consume la API mediante `baseURL: '/api'`.
- Endpoints actuales detectados:
  - `POST /api/auth/login`
  - `POST /api/auth/register`
  - `POST /api/energyops/analyze-reading`
  - `GET /api/analytics/dashboard`
  - `GET /api/analytics/kpis`
  - `GET /api/analytics/anomalies`
- La base de datos ya esta separada por esquemas funcionales: `core`, `iam`, `consumo`, `energiaops`, `analitica`, `audit`, `educacion`, `mantenimiento`.

## 2. Estructura objetivo basada en el ejemplo

Propuesta objetivo conservadora, manteniendo frontend y contratos actuales:

```text
EnergiaClara-IA/
├── ai-service/
│   ├── pom.xml
│   └── modules/
│       ├── ai-api/
│       ├── ai-application/
│       ├── ai-bootstrap/
│       ├── ai-domain/
│       └── ai-infrastructure/
├── core-plataform/
│   └── core-platform/
│       └── pom.xml
├── iam-service/
│   └── pom.xml
├── frontend/
├── scripts/
│   ├── database/
│   │   ├── script.sql
│   │   └── seeds.sql
│   └── runbooks/
├── Pantallas/
└── pom.xml
```

Notas sobre nombres:

- El ejemplo usa `core-plataform`, pero el modulo interno se llama `core-platform`. Se puede respetar el nombre del docente para carpeta externa si es requisito academico.
- `ai-service` deberia contener la aplicacion Spring Boot principal al inicio, para evitar partir despliegue y endpoints demasiado pronto.
- `iam-service` puede comenzar como modulo Maven dentro del mismo proceso Spring Boot, antes de convertirse en microservicio real.
- `frontend` debe permanecer sin cambios de rutas: debe seguir llamando a `/api/...`.

## 3. Diferencias principales

| Tema | Estado actual | Objetivo docente | Diferencia |
| --- | --- | --- | --- |
| Organizacion backend | Un solo modulo Maven `backend` | Servicio `ai-service` con submodulos | Falta separacion fisica por modulos Maven |
| Capas | Ya existen paquetes `api`, `application`, `domain`, `infrastructure`, `bootstrap` | Cada capa como modulo | La separacion existe conceptualmente, pero no en build |
| IAM | Mezclado en `backend` dentro de `auth`, `security`, entidades `iam` | `iam-service` separado | Falta frontera propia para autenticacion, usuarios, roles y JWT |
| Core platform | Parcialmente representado por VO y tenant, pero sin modulo propio | `core-platform` | Faltan clases Java para entidades core como tenant, edificio, medidor |
| Scripts | Carpeta `database/` | `scripts/` | Scripts no estan bajo carpeta `scripts` del ejemplo |
| Frontend | Separado en `frontend` | No aparece en ejemplo | Debe conservarse fuera de la migracion backend |
| BD | SQL Server con esquemas separados | No especificado | La BD ya ayuda a separar responsabilidades |
| API publica | `/api/...` estable | `ai-api` | Debe preservarse sin cambios para no romper frontend |

## 4. Modulos que faltan

Faltan como modulos fisicos de build:

- `ai-service/modules/ai-api`
- `ai-service/modules/ai-application`
- `ai-service/modules/ai-bootstrap`
- `ai-service/modules/ai-domain`
- `ai-service/modules/ai-infrastructure`
- `core-plataform/core-platform`
- `iam-service`
- `scripts`
- Un `pom.xml` raiz agregador, si se migra a multi-modulo Maven.

Tambien faltan fronteras explicitas entre dominios:

- Contratos publicos entre `ai-service` e `iam-service`.
- Contratos publicos entre `ai-service` y `core-platform`.
- Paquetes Java dedicados a `core` para tenant, edificios, medidores y equipos.
- Pruebas de contrato HTTP que congelen los endpoints actuales.

## 5. Clases o responsabilidades candidatas para core-platform

Responsabilidades candidatas:

- Identidad de tenant/inquilino.
- Entidades maestras de plataforma: institucion, edificio, medidor, equipo.
- Value objects compartidos: identificadores, email si se usa fuera de IAM, tenant id, correlation id.
- Contexto multi-tenant transversal, siempre que no dependa de Spring Security directamente.
- Constantes o tipos comunes que no pertenezcan a IA, analitica, mantenimiento ni IAM.

Clases actuales candidatas:

- `domain/model/vo/TenantId.java`
- `domain/model/vo/UserId.java`, si se decide que el ID de usuario es transversal; si no, dejarlo en IAM.
- `domain/model/vo/Email.java`, solo si se usa fuera de autenticacion/usuarios.
- `application/security/AuthenticatedUser.java`, como contrato de usuario autenticado consumido por varios servicios.
- `infrastructure/security/TenantContextHolder.java`, aunque conviene moverlo con cuidado porque hoy esta en infraestructura de seguridad.

Responsabilidades aun no modeladas en Java, pero presentes en BD:

- `core.inquilino`
- `core.edificio`
- `core.medidor`
- `core.equipo`

Recomendacion:

- Crear `core-platform` primero con objetos y contratos pequenos.
- No mover entidades JPA de inmediato si eso obliga a reconfigurar `@EntityScan` o repositorios.
- Evitar que `core-platform` dependa de `ai-service` o `iam-service`.

## 6. Clases o responsabilidades candidatas para iam-service

Responsabilidades candidatas:

- Login y registro.
- Usuarios, roles y asignacion de roles.
- Hash de contrasenas.
- Emision y validacion de JWT.
- Filtro de autenticacion y configuracion de seguridad.
- Contrato de usuario autenticado para otros modulos.

Clases actuales candidatas:

- `api/rest/auth/AuthController.java`
- `api/rest/dto/LoginRequest.java`
- `api/rest/dto/LoginResponse.java`
- `api/rest/dto/RegisterRequest.java`
- `application/service/AuthApplicationService.java`
- `application/dto/LoginCommand.java`
- `application/dto/LoginResult.java`
- `application/dto/RegisterUserCommand.java`
- `application/port/in/LoginUseCase.java`
- `application/port/in/RegisterUserUseCase.java`
- `application/port/out/UserRepositoryPort.java`
- `application/port/out/PasswordHasherPort.java`
- `application/port/out/TokenPort.java`
- `domain/model/User.java`
- `domain/model/Role.java`
- `domain/model/vo/UserId.java`
- `domain/model/vo/Email.java`
- `infrastructure/persistence/entity/UserEntity.java`
- `infrastructure/persistence/entity/RoleEntity.java`
- `infrastructure/persistence/entity/UserRoleEntity.java`
- `infrastructure/persistence/repository/UserJpaRepository.java`
- `infrastructure/persistence/repository/RoleJpaRepository.java`
- `infrastructure/persistence/repository/UserRoleJpaRepository.java`
- `infrastructure/persistence/adapter/UserRepositoryAdapter.java`
- `infrastructure/security/BCryptPasswordHasherAdapter.java`
- `infrastructure/security/JwtTokenAdapter.java`
- `infrastructure/security/JwtAuthFilter.java`
- `infrastructure/security/SecurityConfig.java`

Tablas/esquemas relacionados:

- `iam.usuario`
- `iam.rol`
- `iam.usuario_rol`

Recomendacion:

- Mantener inicialmente los endpoints `/api/auth/login` y `/api/auth/register` igual.
- Si `iam-service` se separa fisicamente como microservicio, hacerlo despues de estabilizarlo como modulo Maven.
- No cambiar formato del JWT hasta tener pruebas de frontend y seguridad.

## 7. Clases o responsabilidades candidatas para ai-service

Responsabilidades candidatas:

- Analisis de lecturas energeticas.
- Deteccion de anomalias.
- Calculo de KPIs y snapshots.
- Dashboard de analitica.
- Casos de uso propios de energia, consumo, analitica, educacion y mantenimiento que no sean identidad ni core maestro.
- API publica actual bajo `/api/energyops` y `/api/analytics`.

Distribucion sugerida dentro de `ai-service/modules`:

### ai-api

- `api/rest/energyops/EnergyOpsController.java`
- `api/rest/energyops/dto/AnalyzeReadingRequest.java`
- `api/rest/energyops/dto/AnalyzeReadingResponse.java`
- `api/rest/analytics/AnalyticsController.java`
- `api/rest/analytics/dto/AnomalyDto.java`
- `api/rest/analytics/dto/DashboardResponse.java`
- `api/rest/analytics/dto/KpiSnapshotDto.java`
- `api/rest/GlobalExceptionHandler.java`, si solo aplica a este servicio.
- `api/rest/audit/Audited.java`, si la auditoria sigue anotando endpoints de AI.

### ai-application

- `application/energyops/service/EnergyAnalysisService.java`
- `application/analytics/service/AnalyticsQueryService.java`
- `application/energyops/dto/*`
- `application/analytics/dto/*`
- Puertos de entrada:
  - `AnalyzeEnergyReadingUseCase`
  - `GetAnalyticsDashboardUseCase`
  - `GetKpiSnapshotsUseCase`
  - `GetAnomaliesUseCase`
- Puertos de salida:
  - `FindEnergyBaselinePort`
  - `SaveEnergyReadingPort`
  - `SaveEnergyAnomalyPort`
  - `SaveEnergyKpiSnapshotPort`
  - `LoadAnalyticsDashboardPort`
  - `LoadKpiSnapshotsPort`
  - `LoadAnomaliesPort`
  - `AuditPort`, si auditoria queda como dependencia transversal.

### ai-domain

- `domain/energyops/AnomalySeverity.java`
- `domain/energyops/AnomalyType.java`
- `domain/energyops/EnergyAnalysisPolicy.java`
- `domain/energyops/EnergyBaseline.java`
- `domain/analytics/package-info.java`
- `domain/consumption/MeterStatus.java`
- `domain/education/ChallengeStatus.java`
- `domain/maintenance/TicketPriority.java`
- `domain/maintenance/TicketStatus.java`
- Tipos de dominio futuros para consumo, educacion y mantenimiento.

### ai-infrastructure

- `infrastructure/persistence/entity/EnergyReadingEntity.java`
- `infrastructure/persistence/entity/EnergyBaselineEntity.java`
- `infrastructure/persistence/entity/EnergyAnomalyEntity.java`
- Repositorios:
  - `EnergyReadingRepository`
  - `EnergyBaselineRepository`
  - `EnergyAnomalyRepository`
- Adaptadores:
  - `EnergyReadingPersistenceAdapter`
  - `EnergyBaselinePersistenceAdapter`
  - `EnergyAnomalyPersistenceAdapter`
  - `EnergyKpiSnapshotPersistenceAdapter`
  - `AnalyticsDashboardPersistenceAdapter`
- Auditoria de persistencia si se decide que `ai-service` la emite:
  - `AuditAdapter`
  - `AuditLogEntity`
  - `AuditChangeEntity`
  - `AuditLogJpaRepository`
  - `AuditChangeJpaRepository`

### ai-bootstrap

- `bootstrap/EnergiaclaraApplication.java`
- `application.yml`
- Configuracion de `@SpringBootApplication`, `@EntityScan`, `@EnableJpaRepositories`.
- Wiring entre modulos.

Tablas/esquemas relacionados:

- `consumo.lectura`
- `energiaops.snapshot_linea_base`
- `energiaops.anomalia`
- `analitica.kpi_diario`
- `audit.evento_auditoria`, si se mantiene auditoria local al servicio.

## 8. Riesgos de migracion

- Romper component scanning de Spring al mover paquetes o modulos.
- Romper `@EntityScan` y `@EnableJpaRepositories` al separar entidades y repositorios.
- Generar ciclos Maven entre `ai-application`, `ai-infrastructure`, `iam-service` y `core-platform`.
- Mover DTOs de API y cambiar JSON accidentalmente.
- Cambiar rutas `/api/...` y romper `frontend/src/services/*`.
- Cambiar reglas de `SecurityConfig` y bloquear endpoints que hoy son publicos para demo.
- Duplicar clases `User`, `Role`, `TenantId` entre IAM y Core.
- Separar `iam-service` como proceso independiente demasiado pronto y obligar a CORS, gateway o cambios de frontend.
- Desalinear entidades JPA con los esquemas SQL Server actuales.
- Cambiar seeds o script SQL y perder compatibilidad con datos demo.
- Romper auditoria AOP al mover la anotacion `Audited` o el aspecto `AuditAspect`.
- Mantener package names antiguos en algunos archivos y nuevos en otros, generando errores de compilacion dificiles de leer.

## 9. Orden recomendado de migracion

1. Congelar comportamiento actual.
   - Crear pruebas de contrato para endpoints actuales.
   - Documentar request/response de `/api/auth`, `/api/energyops` y `/api/analytics`.
   - Verificar que el frontend sigue usando `baseURL: '/api'`.

2. Crear estructura multi-modulo sin mover logica.
   - Agregar `pom.xml` raiz.
   - Crear carpetas objetivo vacias o con clases minimas.
   - Mantener `backend` funcionando durante esta etapa.

3. Migrar `ai-domain`.
   - Mover primero clases puras sin Spring ni JPA.
   - Validar compilacion.

4. Migrar `ai-application`.
   - Mover casos de uso, servicios de aplicacion, DTOs internos y puertos.
   - Mantener contratos HTTP intactos.
   - Evitar dependencia de `application` hacia `infrastructure`.

5. Migrar `ai-infrastructure`.
   - Mover entidades, repositorios y adaptadores de energia/analitica.
   - Ajustar `@EntityScan` y repositorios desde bootstrap.
   - Validar contra BD existente sin alterar tablas.

6. Migrar `ai-api`.
   - Mover controllers y DTOs REST.
   - Confirmar que las rutas sigan exactamente iguales.

7. Migrar `ai-bootstrap`.
   - Mover `EnergiaclaraApplication`.
   - Hacer que el arranque dependa de los modulos anteriores.
   - Confirmar que `server.port: 8080` y configuracion de datasource sigan iguales.

8. Introducir `core-platform`.
   - Empezar con value objects y contratos compartidos.
   - Despues modelar entidades core si hay casos de uso reales.
   - No modificar tablas `core.*` al inicio.

9. Introducir `iam-service` como modulo, no como microservicio externo.
   - Mover login, registro, usuarios, roles, JWT y seguridad.
   - Mantener `/api/auth/*`.
   - Mantener formato de token y permisos actuales.

10. Reubicar scripts.
    - Mover o copiar `database/script.sql` y `database/seeds.sql` a `scripts/database`.
    - Idealmente mantener compatibilidad temporal con la carpeta `database/`.

11. Retirar `backend` solo cuando la nueva estructura compile, arranque y pase pruebas.

## 10. Validaciones necesarias para no romper la aplicacion

Validaciones de build:

- `mvn clean test` desde el agregador raiz.
- Compilacion individual de cada modulo Maven.
- Verificar que no existan dependencias ciclicas.

Validaciones de backend:

- Arranque Spring Boot sin errores de component scan.
- Arranque con SQL Server usando `application.yml` actual.
- Validar que `ddl-auto` siga en `none`.
- Confirmar que no se generen cambios automaticos de schema.
- Verificar repositorios JPA para:
  - `iam.usuario`
  - `iam.rol`
  - `iam.usuario_rol`
  - `consumo.lectura`
  - `energiaops.snapshot_linea_base`
  - `energiaops.anomalia`
  - `audit.evento_auditoria`

Validaciones de API:

- `POST /api/auth/login` debe devolver el mismo JSON: `token`, `userId`, `tenantId`, `roles`.
- `POST /api/auth/register` debe conservar ruta, payload y respuesta.
- `POST /api/energyops/analyze-reading` debe conservar payload y respuesta.
- `GET /api/analytics/dashboard` debe conservar estructura de dashboard.
- `GET /api/analytics/kpis` debe conservar lista de KPIs.
- `GET /api/analytics/anomalies` debe conservar lista de anomalias.
- `SecurityConfig` debe mantener accesibles los endpoints actualmente permitidos.

Validaciones de frontend:

- `frontend/src/services/apiClient.js` debe seguir usando `/api`.
- Login debe seguir guardando y enviando JWT desde `localStorage`.
- Pantallas existentes deben cargar sin cambiar rutas:
  - Dashboard KPIs.
  - Registro de lectura.
  - Lista/detalle de anomalias.
  - Login.
- Build de frontend con `npm run build`.

Validaciones de base de datos:

- No cambiar nombres de schemas ni tablas.
- No cambiar columnas usadas por entidades JPA.
- Ejecutar seeds solo en ambiente controlado.
- Confirmar que extensiones actuales de `seeds.sql` sigan existiendo si se usan:
  - `facility_label`
  - `meter_label`
  - `voltaje`
  - `factor_potencia`
  - `tolerancia_porcentaje`
  - `recomendacion`
  - `costo_estimado`
  - `co2_estimado`
  - metadata HTTP de auditoria.

Validaciones de auditoria y seguridad:

- Confirmar que `AuditAspect` siga interceptando metodos anotados con `@Audited`.
- Confirmar que login invalido siga fallando igual.
- Confirmar que JWT valido permita endpoints protegidos.
- Confirmar que tenant actual se propague igual que antes.

## Recomendacion final

La migracion debe tratarse como reorganizacion fisica gradual, no como rediseño funcional. El proyecto ya tiene una buena separacion logica por capas; el primer objetivo es convertir esa separacion en modulos Maven sin cambiar rutas, payloads, tablas ni experiencia del frontend.

La secuencia mas segura es mover primero codigo puro de dominio y aplicacion, despues infraestructura, y dejar IAM/core como modulos internos antes de pensar en servicios desplegados por separado.

## 11. Estructura base creada

Se creo la forma fisica inicial de la arquitectura del docente sin mover logica, sin cambiar endpoints, sin tocar frontend y sin modificar base de datos.

```text
EnergiaClara-IA/
├── ai-service/
│   ├── README.md
│   └── modules/
│       ├── ai-api/
│       │   └── README.md
│       ├── ai-application/
│       │   └── README.md
│       ├── ai-bootstrap/
│       │   └── README.md
│       ├── ai-domain/
│       │   └── README.md
│       └── ai-infrastructure/
│           └── README.md
├── core-plataform/
│   ├── README.md
│   └── core-platform/
│       └── README.md
├── iam-service/
│   └── README.md
└── scripts/
    └── README.md
```

Decisiones tomadas en esta preparacion:

- No se agrego `pom.xml` raiz porque el proyecto actual solo tiene `backend/pom.xml`.
- No se conectaron los nuevos directorios al build Maven.
- No se movieron clases Java.
- No se copiaron ni movieron scripts SQL desde `database/`.
- No se modifico `frontend/`.
- No se modificaron rutas `/api/...`.

Pendiente para la siguiente etapa:

- Definir si se creara un `pom.xml` agregador raiz o si se mantendra `backend/` como build principal durante mas tiempo.
- Agregar `pom.xml` minimos por modulo solo cuando se decida integrar la compilacion multi-modulo.
- Congelar contratos HTTP con pruebas antes de migrar controladores.
- Migrar primero clases puras de dominio a `ai-service/modules/ai-domain`.
- Mantener IAM y Core Platform como estructura preparada hasta tener contratos claros.

## 12. Primera migracion real: core-platform transversal

Se realizo una primera migracion real y no cosmetica hacia `core-plataform/core-platform`.

Cambios estructurales:

- Se agrego `pom.xml` raiz como agregador Maven.
- Se conectaron estos modulos al reactor:
  - `core-plataform/core-platform`
  - `backend`
- `backend` ahora depende de `com.energiaclara:core-platform`.

Clases movidas a Core Platform:

- `com.energiaclara.core.model.vo.TenantId`
- `com.energiaclara.core.model.vo.UserId`
- `com.energiaclara.core.security.AuthenticatedUser`
- `com.energiaclara.core.security.TenantContextHolder`
- `com.energiaclara.core.audit.AuditEvent`

Por que son transversales:

- `TenantId` representa el inquilino usado por IAM, auditoria y flujos operativos.
- `UserId` identifica actores de seguridad y auditoria, sin depender de JPA ni Spring.
- `AuthenticatedUser` es el principal autenticado que consumen controllers y aspectos.
- `TenantContextHolder` conserva el tenant actual por request sin depender de un contexto de negocio concreto.
- `AuditEvent` es un contrato base de auditoria entre aplicacion e infraestructura.

Restricciones respetadas:

- No se movieron controllers.
- No se movieron servicios JWT ni AuthController.
- No se movieron entidades JPA.
- No se movieron EnergyOps ni Analytics.
- No se cambiaron endpoints.
- No se cambio JSON.
- No se cambio frontend.
- No se cambio base de datos.

Pendiente:

- `iam-service` sigue pendiente para login, usuarios, roles, JWT y seguridad.
- `ai-service` sigue pendiente para EnergyOps, Analytics, API, aplicacion, dominio e infraestructura.
- Auditoria podria evolucionar a un modulo transversal mas completo, pero por ahora solo se movio el contrato `AuditEvent`.

## 13. Primera implementacion real en ai-service: ai-domain

Se implemento la Fase 1 de la IA hibrida explicable como dominio puro en:

```text
ai-service/modules/ai-domain
```

El modulo `ai-domain` ya no esta vacio. Fue conectado al reactor Maven raiz sin integrarse todavia con EnergyOps.

Clases reales creadas:

- `com.energiaclara.ai.domain.EnergyAiInput`
- `com.energiaclara.ai.domain.EnergyAiHistoricalReading`
- `com.energiaclara.ai.domain.EnergyAiResult`
- `com.energiaclara.ai.domain.DynamicBaseline`
- `com.energiaclara.ai.domain.StatisticalAnomalyScore`
- `com.energiaclara.ai.domain.HybridSeverity`
- `com.energiaclara.ai.domain.ExplainableRecommendation`
- `com.energiaclara.ai.domain.HybridEnergyAiEngine`

Responsabilidades implementadas:

- baseline dinamico
- promedio movil
- desviacion estandar
- Z-Score
- desviacion porcentual
- score de anomalia
- severidad sugerida
- confianza
- explicacion dinamica
- recomendacion dinamica
- fallback deterministico
- version del modelo

Limites respetados:

- `ai-domain` no importa Spring.
- `ai-domain` no importa JPA.
- `ai-domain` no depende de backend.
- `ai-domain` no depende de infraestructura.
- No se modificaron endpoints.
- No se modifico frontend.
- No se modifico base de datos.
- No se modifico JWT/IAM.
- No se modifico Analytics.

Validacion realizada:

- `mvn clean test` desde la raiz del proyecto: exitoso.
- Tests unitarios de dominio: exitosos.

Pendiente:

- Crear `ai-application` para orquestar el motor de dominio.
- Crear puertos para historial de lecturas.
- Crear adapters para obtener historial desde persistencia actual.
- Integrar EnergyOps por puerto/adaptador sin cambiar JSON externo.

## 14. Segunda implementacion real en ai-service: ai-application

Se implemento la Fase 2 de la IA hibrida explicable como capa de aplicacion hexagonal en:

```text
ai-service/modules/ai-application
```

El modulo `ai-application` ya no esta vacio. Fue conectado al reactor Maven raiz y depende de `ai-domain`.

Clases reales creadas:

- `com.energiaclara.ai.application.EnergyAiAnalysisCommand`
- `com.energiaclara.ai.application.EnergyAiAnalysisResponse`
- `com.energiaclara.ai.application.AnalyzeEnergyWithAiService`
- `com.energiaclara.ai.application.port.in.AnalyzeEnergyWithAiUseCase`
- `com.energiaclara.ai.application.port.out.EnergyHistoryProviderPort`
- `com.energiaclara.ai.application.port.out.EnergyAiEnginePort`

Responsabilidades implementadas:

- caso de uso de aplicacion para analizar energia con IA
- puerto de entrada para futura integracion EnergyOps
- puerto de salida para historial de lecturas
- puerto de salida para motor de IA
- transformacion command -> input de dominio
- transformacion resultado de dominio -> response de aplicacion
- preservacion del fallback del dominio

Limites respetados:

- `ai-application` no importa Spring.
- `ai-application` no importa JPA.
- `ai-application` no depende de backend.
- `ai-application` no accede a SQL Server.
- No se modificaron endpoints.
- No se modifico frontend.
- No se modifico base de datos.
- No se modifico JWT/IAM.
- No se modifico Analytics.
- No se movio codigo del backend.

Validacion realizada:

- `mvn clean test` desde la raiz del proyecto: exitoso.
- Tests unitarios de `ai-domain`: exitosos.
- Tests unitarios de `ai-application`: exitosos.
- Smoke test del backend: exitoso.

Pendiente:

- Crear `ai-infrastructure` o adapter temporal para cargar historial real.
- Conectar EnergyOps con `AnalyzeEnergyWithAiUseCase`.
- Mantener JSON externo sin cambios durante la integracion.

## 15. Tercera implementacion real en ai-service: ai-infrastructure

Se implemento la Fase 3 de la IA hibrida explicable como capa de adapters demo en:

```text
ai-service/modules/ai-infrastructure
```

El modulo `ai-infrastructure` ya no esta vacio. Fue conectado al reactor Maven raiz y depende de `ai-application` y `ai-domain`.

Clases reales creadas:

- `com.energiaclara.ai.infrastructure.InMemoryEnergyHistoryProviderAdapter`
- `com.energiaclara.ai.infrastructure.DefaultEnergyAiEngineAdapter`
- `com.energiaclara.ai.infrastructure.ManualAiServiceFactory`

Responsabilidades implementadas:

- adapter demo para historial en memoria
- adapter para invocar el motor de IA del dominio
- composicion manual del caso de uso sin Spring
- pruebas de integracion ligera de IA

Limites respetados:

- `ai-infrastructure` no importa Spring.
- `ai-infrastructure` no importa JPA.
- `ai-infrastructure` no accede a SQL Server.
- `ai-infrastructure` no depende del backend principal.
- No se modificaron endpoints.
- No se modifico frontend.
- No se modifico base de datos.
- No se modifico JWT/IAM.
- No se modifico Analytics.
- No se movio codigo del backend.

Validacion realizada:

- `mvn clean test` desde la raiz del proyecto: exitoso.
- Tests unitarios de `ai-domain`: exitosos.
- Tests unitarios de `ai-application`: exitosos.
- Tests de `ai-infrastructure`: exitosos.
- Smoke test del backend: exitoso.

Pendiente:

- Crear adapter real de historial contra persistencia actual.
- Conectar EnergyOps real con `AnalyzeEnergyWithAiUseCase`.
- Mantener JSON externo y endpoints sin cambios en la siguiente fase.

## 16. Primera implementacion real en iam-service

Se implemento la Fase 1 de migracion IAM como modulo Maven interno en:

```text
iam-service
```

El modulo fue conectado al reactor Maven raiz como `com.energiaclara:iam-service`.

Clases reales movidas:

- `com.energiaclara.iam.domain.model.User`
- `com.energiaclara.iam.domain.model.Role`
- `com.energiaclara.iam.domain.model.vo.Email`
- `com.energiaclara.iam.application.service.AuthApplicationService`
- `com.energiaclara.iam.application.dto.LoginCommand`
- `com.energiaclara.iam.application.dto.LoginResult`
- `com.energiaclara.iam.application.dto.RegisterUserCommand`
- `com.energiaclara.iam.application.port.in.LoginUseCase`
- `com.energiaclara.iam.application.port.in.RegisterUserUseCase`
- `com.energiaclara.iam.application.port.out.UserRepositoryPort`
- `com.energiaclara.iam.application.port.out.PasswordHasherPort`
- `com.energiaclara.iam.application.port.out.TokenPort`

Direccion de dependencia:

```text
backend -> iam-service -> core-platform
backend -> core-platform
```

Limites respetados:

- `iam-service` no depende de backend.
- No se movio `AuthController`.
- No se movio `SecurityConfig`.
- No se movio `JwtAuthFilter`.
- No se movieron entidades JPA.
- No se cambiaron endpoints.
- No se cambio JSON publico.
- No se cambio frontend.
- No se cambio base de datos.
- No se toco IA.
- No se toco EnergyOps.
- No se toco Analytics.

Validacion realizada:

- `mvn clean test` desde la raiz del proyecto: exitoso.
- `mvn install -DskipTests` desde la raiz del proyecto: exitoso.
- Backend arranco con SQL Server.
- `POST /api/auth/login`: HTTP `200`.
- `POST /api/energyops/analyze-reading` con Bearer: HTTP `201`.
- `GET /api/analytics/anomalies` con Bearer: HTTP `200`.

Pendiente:

- Agregar tests unitarios especificos para `iam-service`.
- Mover adapters de password/token en una fase posterior.
- Mover persistence IAM solo cuando se prepare el escaneo JPA del modulo.
- Mantener `SecurityConfig` y `JwtAuthFilter` en backend durante el MVP.

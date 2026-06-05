# Decision report para migracion docente

Este reporte decide que migrar realmente desde la arquitectura actual hacia la estructura fisica ya creada:

```text
ai-service/modules/ai-api
ai-service/modules/ai-application
ai-service/modules/ai-bootstrap
ai-service/modules/ai-domain
ai-service/modules/ai-infrastructure
core-plataform/core-platform
iam-service
scripts
```

Restricciones consideradas:

- No romper backend.
- No romper frontend.
- No cambiar endpoints.
- No cambiar base de datos.
- No hacer una migracion falsa solo para copiar carpetas.

## 1. Clases reales actuales que deberian moverse a core-plataform/core-platform

### Candidatas reales

Estas clases tienen sentido como Core Platform si se busca una capa comun minima:

- `backend/src/main/java/com/energiaclara/domain/model/vo/TenantId.java`
- `backend/src/main/java/com/energiaclara/application/security/AuthenticatedUser.java`
- `backend/src/main/java/com/energiaclara/infrastructure/security/TenantContextHolder.java`

### Candidatas dudosas

Estas clases podrian parecer compartidas, pero hoy estan mas ligadas a IAM:

- `backend/src/main/java/com/energiaclara/domain/model/vo/UserId.java`
- `backend/src/main/java/com/energiaclara/domain/model/vo/Email.java`

Recomendacion sobre estas dos:

- `UserId` deberia moverse a `iam-service` si usuario se considera parte de identidad.
- `Email` deberia moverse a `iam-service` porque hoy se usa como dato de usuario/login, no como dato transversal de plataforma.

### Responsabilidades reales de Core que aun no existen como Java

La base de datos ya tiene responsabilidades core, pero no hay clases Java equivalentes:

- `core.inquilino`
- `core.edificio`
- `core.medidor`
- `core.equipo`

Por eso, mover solo `TenantId` y contexto tenant a `core-platform` tendria poco valor funcional inmediato. Seria una migracion pequena y riesgosa para el build si se convierte en modulo Maven antes de tiempo.

## 2. Clases reales actuales que deberian moverse a iam-service

IAM es el bloque mas claro para separar porque ya tiene API, aplicacion, dominio, infraestructura y seguridad.

### API IAM

- `backend/src/main/java/com/energiaclara/api/rest/auth/AuthController.java`
- `backend/src/main/java/com/energiaclara/api/rest/dto/LoginRequest.java`
- `backend/src/main/java/com/energiaclara/api/rest/dto/LoginResponse.java`
- `backend/src/main/java/com/energiaclara/api/rest/dto/RegisterRequest.java`

### Aplicacion IAM

- `backend/src/main/java/com/energiaclara/application/service/AuthApplicationService.java`
- `backend/src/main/java/com/energiaclara/application/dto/LoginCommand.java`
- `backend/src/main/java/com/energiaclara/application/dto/LoginResult.java`
- `backend/src/main/java/com/energiaclara/application/dto/RegisterUserCommand.java`
- `backend/src/main/java/com/energiaclara/application/port/in/LoginUseCase.java`
- `backend/src/main/java/com/energiaclara/application/port/in/RegisterUserUseCase.java`
- `backend/src/main/java/com/energiaclara/application/port/out/UserRepositoryPort.java`
- `backend/src/main/java/com/energiaclara/application/port/out/PasswordHasherPort.java`
- `backend/src/main/java/com/energiaclara/application/port/out/TokenPort.java`

### Dominio IAM

- `backend/src/main/java/com/energiaclara/domain/model/User.java`
- `backend/src/main/java/com/energiaclara/domain/model/Role.java`
- `backend/src/main/java/com/energiaclara/domain/model/vo/UserId.java`
- `backend/src/main/java/com/energiaclara/domain/model/vo/Email.java`

### Infraestructura IAM

- `backend/src/main/java/com/energiaclara/infrastructure/persistence/entity/UserEntity.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/entity/RoleEntity.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/entity/UserRoleEntity.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/repository/UserJpaRepository.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/repository/RoleJpaRepository.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/repository/UserRoleJpaRepository.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/adapter/UserRepositoryAdapter.java`
- `backend/src/main/java/com/energiaclara/infrastructure/security/BCryptPasswordHasherAdapter.java`
- `backend/src/main/java/com/energiaclara/infrastructure/security/JwtTokenAdapter.java`
- `backend/src/main/java/com/energiaclara/infrastructure/security/JwtAuthFilter.java`
- `backend/src/main/java/com/energiaclara/infrastructure/security/SecurityConfig.java`

### Comentario importante

Separar IAM fisicamente ahora tiene valor arquitectonico, pero tambien es la migracion con mayor riesgo operativo porque toca login, JWT, filtros, permisos y reglas de acceso. Si se hace mal, rompe frontend aunque no cambien endpoints.

## 3. Clases reales actuales que deberian moverse a ai-service

AI Service es el destino natural de energyops, analytics, consumo relacionado a lecturas, y parte de auditoria.

### ai-api

- `backend/src/main/java/com/energiaclara/api/rest/energyops/EnergyOpsController.java`
- `backend/src/main/java/com/energiaclara/api/rest/energyops/dto/AnalyzeReadingRequest.java`
- `backend/src/main/java/com/energiaclara/api/rest/energyops/dto/AnalyzeReadingResponse.java`
- `backend/src/main/java/com/energiaclara/api/rest/analytics/AnalyticsController.java`
- `backend/src/main/java/com/energiaclara/api/rest/analytics/dto/AnomalyDto.java`
- `backend/src/main/java/com/energiaclara/api/rest/analytics/dto/DashboardResponse.java`
- `backend/src/main/java/com/energiaclara/api/rest/analytics/dto/KpiSnapshotDto.java`
- `backend/src/main/java/com/energiaclara/api/rest/GlobalExceptionHandler.java`, si queda como advice del servicio principal.
- `backend/src/main/java/com/energiaclara/api/rest/audit/Audited.java`, si se mantiene auditoria AOP sobre endpoints del AI service.

### ai-application

- `backend/src/main/java/com/energiaclara/application/energyops/service/EnergyAnalysisService.java`
- `backend/src/main/java/com/energiaclara/application/analytics/service/AnalyticsQueryService.java`
- `backend/src/main/java/com/energiaclara/application/energyops/dto/AnalyzeEnergyReadingCommand.java`
- `backend/src/main/java/com/energiaclara/application/energyops/dto/AnalyzeEnergyReadingResult.java`
- `backend/src/main/java/com/energiaclara/application/energyops/dto/EnergyAnomalyRecord.java`
- `backend/src/main/java/com/energiaclara/application/energyops/dto/EnergyBaselineRecord.java`
- `backend/src/main/java/com/energiaclara/application/energyops/dto/EnergyKpiSnapshotRecord.java`
- `backend/src/main/java/com/energiaclara/application/energyops/dto/EnergyReadingRecord.java`
- `backend/src/main/java/com/energiaclara/application/analytics/dto/AnalyticsDashboardMetricsResult.java`
- `backend/src/main/java/com/energiaclara/application/analytics/dto/AnalyticsDashboardResult.java`
- `backend/src/main/java/com/energiaclara/application/analytics/dto/AnomalyResult.java`
- `backend/src/main/java/com/energiaclara/application/analytics/dto/KpiSnapshotResult.java`
- `backend/src/main/java/com/energiaclara/application/port/in/AnalyzeEnergyReadingUseCase.java`
- `backend/src/main/java/com/energiaclara/application/port/in/GetAnalyticsDashboardUseCase.java`
- `backend/src/main/java/com/energiaclara/application/port/in/GetAnomaliesUseCase.java`
- `backend/src/main/java/com/energiaclara/application/port/in/GetKpiSnapshotsUseCase.java`
- `backend/src/main/java/com/energiaclara/application/port/out/FindEnergyBaselinePort.java`
- `backend/src/main/java/com/energiaclara/application/port/out/LoadAnalyticsDashboardPort.java`
- `backend/src/main/java/com/energiaclara/application/port/out/LoadAnomaliesPort.java`
- `backend/src/main/java/com/energiaclara/application/port/out/LoadKpiSnapshotsPort.java`
- `backend/src/main/java/com/energiaclara/application/port/out/SaveEnergyAnomalyPort.java`
- `backend/src/main/java/com/energiaclara/application/port/out/SaveEnergyKpiSnapshotPort.java`
- `backend/src/main/java/com/energiaclara/application/port/out/SaveEnergyReadingPort.java`
- `backend/src/main/java/com/energiaclara/application/port/out/AuditPort.java`, si auditoria sigue como dependencia del flujo AI.

### ai-domain

- `backend/src/main/java/com/energiaclara/domain/energyops/AnomalySeverity.java`
- `backend/src/main/java/com/energiaclara/domain/energyops/AnomalyType.java`
- `backend/src/main/java/com/energiaclara/domain/energyops/EnergyAnalysisPolicy.java`
- `backend/src/main/java/com/energiaclara/domain/energyops/EnergyBaseline.java`
- `backend/src/main/java/com/energiaclara/domain/consumption/MeterStatus.java`
- `backend/src/main/java/com/energiaclara/domain/education/ChallengeStatus.java`
- `backend/src/main/java/com/energiaclara/domain/maintenance/TicketPriority.java`
- `backend/src/main/java/com/energiaclara/domain/maintenance/TicketStatus.java`

### ai-infrastructure

- `backend/src/main/java/com/energiaclara/infrastructure/persistence/entity/EnergyReadingEntity.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/entity/EnergyBaselineEntity.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/entity/EnergyAnomalyEntity.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/repository/EnergyReadingRepository.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/repository/EnergyBaselineRepository.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/repository/EnergyAnomalyRepository.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/adapter/EnergyReadingPersistenceAdapter.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/adapter/EnergyBaselinePersistenceAdapter.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/adapter/EnergyAnomalyPersistenceAdapter.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/adapter/EnergyKpiSnapshotPersistenceAdapter.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/adapter/AnalyticsDashboardPersistenceAdapter.java`

### Auditoria, decision pendiente

Estas clases no son puramente AI ni puramente IAM:

- `backend/src/main/java/com/energiaclara/domain/model/audit/AuditEvent.java`
- `backend/src/main/java/com/energiaclara/infrastructure/audit/AuditAspect.java`
- `backend/src/main/java/com/energiaclara/infrastructure/audit/AuditAsyncConfig.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/adapter/AuditAdapter.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/entity/AuditLogEntity.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/entity/AuditChangeEntity.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/repository/AuditLogJpaRepository.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/repository/AuditChangeJpaRepository.java`

Para MVP, conviene dejarlas donde estan hasta decidir si auditoria sera transversal, parte de core-platform o infraestructura compartida.

### ai-bootstrap

- `backend/src/main/java/com/energiaclara/bootstrap/EnergiaclaraApplication.java`
- `backend/src/main/resources/application.yml`

No conviene mover bootstrap ahora si no se integra Maven multi-modulo, porque es lo que mantiene vivo el backend actual.

## 4. Modulos que quedarian vacios si se migran ahora

Si se migrara solo lo que tiene sentido real hoy:

- `core-plataform/core-platform` quedaria casi vacio o con muy pocas clases: `TenantId`, posiblemente `AuthenticatedUser` y `TenantContextHolder`.
- `scripts` quedaria vacio si se respeta la regla de no mover base de datos. Solo tendria README.
- `ai-bootstrap` no deberia recibir clases hasta conectar formalmente el build. Si se deja el backend actual funcionando, queda como modulo preparado.

Si se migrara solo IAM:

- Todo `ai-service/modules/*` quedaria preparado pero vacio.
- `core-platform` quedaria vacio.
- `scripts` quedaria vacio.

Si se migrara solo AI Domain:

- `ai-api`, `ai-application`, `ai-infrastructure`, `ai-bootstrap`, `iam-service`, `core-platform` quedarian vacios.

Conclusion:

- Migrar una sola parte ahora deja carpetas vacias de todos modos.
- Eso no es necesariamente malo si estan documentadas como fase 2.
- Lo que si seria mala practica es copiar clases sin integrar Maven ni pruebas solo para aparentar uso.

## 5. Valor que aporta cada migracion

| Migracion | Valor real | Valor para MVP |
| --- | --- | --- |
| Core Platform | Define contratos comunes y tenant compartido | Bajo hoy, porque faltan entidades Java core reales |
| IAM Service | Separa autenticacion, usuarios, roles y seguridad | Medio/alto, pero riesgoso por JWT y filtros |
| AI Service completo | Alinea el proyecto con el ejemplo docente y separa capas fisicamente | Alto, pero es la migracion mas grande |
| AI Domain solamente | Baja complejidad; mueve clases puras sin Spring/JPA | Medio, sirve como primera migracion real y segura |
| Scripts | Ordena SQL y runbooks | Bajo si no se mueven los SQL actuales |
| Dejar modulos documentados | Evita migracion falsa y conserva backend estable | Alto para MVP si se acompaña con decision tecnica clara |

## 6. Riesgos de mover cada modulo

### Core Platform

Riesgos:

- Crear un modulo demasiado pequeño que no justifique el cambio.
- Duplicar conceptos entre Core e IAM, especialmente `UserId` y `Email`.
- Mover `TenantContextHolder` puede romper filtros o servicios que esperan su paquete actual.
- Si se conecta a Maven, puede introducir dependencias circulares con IAM y AI.

Riesgo general: medio si se mueve contexto tenant; bajo si solo se documenta.

### IAM Service

Riesgos:

- Romper login.
- Romper registro.
- Romper generacion o validacion de JWT.
- Cambiar accidentalmente JSON de `LoginResponse`.
- Cambiar reglas de `SecurityConfig`.
- Bloquear endpoints que hoy son publicos para demo.
- Romper inyeccion de `JwtAuthFilter`, `JwtTokenAdapter`, `BCryptPasswordHasherAdapter` o repositorios JPA.

Riesgo general: alto.

### AI Service

Riesgos:

- Romper controllers de `/api/energyops` y `/api/analytics`.
- Romper puertos entre aplicacion e infraestructura.
- Romper `@EntityScan` o repositorios JPA.
- Romper auditoria si `@Audited` o `AuditAspect` cambian de paquete.
- Mover `application.yml` o bootstrap podria impedir el arranque completo.

Riesgo general: alto si se mueve completo; bajo/medio si se empieza solo por dominio puro.

### Scripts

Riesgos:

- Si se mueven SQL, los runbooks o instrucciones actuales podrian quedar desactualizados.
- Si se duplican scripts, puede haber confusion sobre cual ejecutar.

Riesgo general: bajo si solo se documenta; medio si se mueve `database/`.

## 7. Recomendacion para un MVP universitario

Opciones evaluadas:

1. Migrar fisicamente todo ahora.
   - No recomendado.
   - Mucho riesgo para backend, endpoints, seguridad y JPA.
   - Requiere Maven multi-modulo, pruebas de contrato y ajuste de Spring Boot.

2. Migrar solo core-platform.
   - No recomendado como primera migracion real.
   - Hay pocas clases core reales; seria casi simbolico.
   - Puede terminar siendo una migracion falsa.

3. Migrar solo iam-service.
   - No recomendado para MVP inmediato.
   - Tiene valor, pero toca la zona mas sensible: login, JWT y seguridad.

4. Crear ai-service como modulo preparado.
   - Ya esta hecho como estructura fisica.
   - Es correcto dejarlo preparado, pero no basta como migracion real si el docente espera evidencia funcional.

5. Dejar modulos fisicos documentados para fase 2.
   - Recomendado para este momento.
   - Es honesto: reconoce que la arquitectura actual ya esta separada por capas en paquetes y evita romper la app.
   - Permite defender que la siguiente migracion real debe hacerse con pruebas.

### Recomendacion concreta

Para un MVP universitario, recomiendo:

- Mantener los modulos fisicos documentados para fase 2.
- No migrar IAM ni bootstrap todavia.
- No migrar core-platform todavia porque quedaria demasiado vacio.
- Si se necesita demostrar una migracion real minima, migrar solo `ai-domain` en una etapa posterior, porque contiene clases puras y de bajo riesgo.

Esta opcion evita una migracion falsa y protege la aplicacion. El argumento tecnico es fuerte: el proyecto ya tiene arquitectura por capas dentro de `backend`; ahora falta convertirla a multi-modulo de forma incremental y verificable, no mover archivos a ciegas.

## 8. Pasos exactos que seguiria despues

### Paso 1: Congelar contratos antes de mover

- Crear pruebas o documentacion ejecutable para:
  - `POST /api/auth/login`
  - `POST /api/auth/register`
  - `POST /api/energyops/analyze-reading`
  - `GET /api/analytics/dashboard`
  - `GET /api/analytics/kpis`
  - `GET /api/analytics/anomalies`
- Validar que `frontend/src/services/apiClient.js` siga usando `/api`.

### Paso 2: Crear un `pom.xml` raiz solo cuando sea necesario

- No conectar los nuevos modulos aun si no van a compilar clases.
- Cuando se conecten, usar un agregador Maven conservador.
- Mantener `backend` como modulo funcional hasta que `ai-bootstrap` pueda reemplazarlo.

### Paso 3: Primera migracion real recomendada: ai-domain

Mover solo clases puras:

- `AnomalySeverity`
- `AnomalyType`
- `EnergyAnalysisPolicy`
- `EnergyBaseline`
- `MeterStatus`
- `ChallengeStatus`
- `TicketPriority`
- `TicketStatus`

Validaciones despues:

- `mvn clean test`
- Pruebas de los endpoints existentes.
- Arranque Spring Boot.

### Paso 4: Segunda migracion: ai-application

- Mover servicios, DTOs internos y puertos de energyops/analytics.
- Mantener controllers en `backend` temporalmente si hace falta.
- Evitar que aplicacion dependa de infraestructura.

### Paso 5: Tercera migracion: ai-infrastructure

- Mover entidades, repositorios y adaptadores.
- Ajustar `@EntityScan` y repositorios.
- Validar contra SQL Server sin cambiar schema.

### Paso 6: Cuarta migracion: ai-api

- Mover controllers y DTOs REST.
- Confirmar que las rutas `/api/...` no cambien.
- Confirmar que el frontend no requiera cambios.

### Paso 7: Recién despues evaluar IAM

- Separar IAM como modulo interno, no como microservicio externo.
- Mantener `POST /api/auth/login` y `POST /api/auth/register`.
- Mantener formato de token y claims.
- Validar login desde frontend.

### Paso 8: Evaluar Core Platform con contenido real

- Crear o mover entidades de plataforma solo cuando haya casos de uso Java para:
  - tenant/inquilino
  - edificio
  - medidor
  - equipo
- Evitar que `core-platform` sea solo una carpeta con un value object.

### Paso 9: Scripts

- Mantener `database/` por ahora.
- Cuando se mueva a `scripts/database/`, actualizar runbooks y referencias.
- No cambiar SQL ni seeds sin una razon funcional.

## Decision final

No conviene migrar fisicamente todo ahora. Tampoco conviene migrar solo core-platform, porque tendria poco contenido real. La opcion mas sana para el MVP es dejar las carpetas creadas y documentadas para fase 2, y planificar como primera migracion real un `ai-domain` pequeño, verificable y sin impacto en endpoints, frontend ni base de datos.

Esto evita una migracion cosmetica y mantiene el backend estable mientras se prepara una evolucion real hacia la arquitectura del docente.

## 9. Decision actualizada: core-platform migrado

Se decidio avanzar con una primera migracion real hacia `core-platform`, limitada a responsabilidades transversales de bajo riesgo.

Clases migradas:

- `TenantId`
- `UserId`
- `AuthenticatedUser`
- `TenantContextHolder`
- `AuditEvent`

Valor aportado:

- `core-platform` ya no es una carpeta vacia: contiene contratos y value objects usados por backend.
- El backend depende de un modulo externo real mediante Maven.
- Se prueba una direccion de dependencia enterprise: `backend -> core-platform`.
- La migracion no toca rutas, JSON, frontend, base de datos, JPA, AuthController, JWT service, EnergyOps ni Analytics.

Riesgo asumido:

- `UserId` queda en Core Platform aunque tambien pertenece conceptualmente a IAM, porque auditoria y seguridad lo usan como identificador transversal.
- `TenantContextHolder` sigue siendo un ThreadLocal simple; cuando se migre IAM, se podra revisar si el contexto tenant pertenece a Core o a seguridad.

Decision para MVP despues de esta migracion:

- Mantener `iam-service` preparado, sin mover login/JWT todavia.
- Mantener `ai-service` preparado, sin mover EnergyOps/Analytics todavia.
- La siguiente migracion recomendable es `ai-domain` o contratos de IAM, pero solo despues de agregar pruebas de contrato HTTP.

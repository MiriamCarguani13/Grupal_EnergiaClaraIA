# Cleanup report post migraciones enterprise

Fecha de auditoria: 2026-06-02

## Alcance

Se audito la arquitectura despues de las migraciones reales a:

- `core-plataform/core-platform`
- `iam-service`
- `ai-service`
- `backend`

No se elimino ningun archivo. Este documento solo identifica elementos obsoletos, duplicados, huerfanos o candidatos a limpieza.

## Resumen ejecutivo

No se detectaron duplicados reales de clases criticas entre `backend`, `core-platform`, `iam-service` y `ai-service`.

Estado general:

- `core-platform`: limpio y utilizado.
- `iam-service`: contiene dominio/application reales de IAM; no hay duplicados antiguos activos en backend.
- `ai-service`: contiene domain/application/infrastructure reales; no depende de backend.
- `backend`: conserva adapters web/security/JPA necesarios; tambien conserva algunos paquetes vacios o conceptuales heredados.

Principal limpieza recomendada:

1. Eliminar directorios fuente vacios dejados por migraciones anteriores.
2. Revisar `package-info.java` que solo actuan como marcadores conceptuales.
3. Decidir si se conservan o eliminan enums MVP/conceptuales sin uso runtime.
4. No eliminar DTOs internos o REST que parecen similares, porque cumplen roles distintos por capa.

## 1. Clases duplicadas

### Resultado

No se encontraron duplicados reales por nombre de clase, excepto `package-info.java`, que es normal porque muchos paquetes tienen su propio marcador.

No hay duplicados activos de:

- `User`
- `Role`
- `Email`
- `AuthApplicationService`
- `LoginCommand`
- `LoginResult`
- `RegisterUserCommand`
- `LoginUseCase`
- `RegisterUserUseCase`
- `UserRepositoryPort`
- `PasswordHasherPort`
- `TokenPort`
- `TenantId`
- `UserId`
- `AuthenticatedUser`
- `TenantContextHolder`
- clases principales de IA

### Impacto

No hay eliminacion de clases duplicadas recomendada ahora.

## 2. Imports antiguos

### Resultado

No se detectaron imports activos hacia paquetes antiguos de IAM migrado:

- `com.energiaclara.domain.model.User`
- `com.energiaclara.domain.model.Role`
- `com.energiaclara.domain.model.vo.Email`
- `com.energiaclara.application.dto.LoginCommand`
- `com.energiaclara.application.dto.LoginResult`
- `com.energiaclara.application.dto.RegisterUserCommand`
- `com.energiaclara.application.service.AuthApplicationService`
- `com.energiaclara.application.port.in.LoginUseCase`
- `com.energiaclara.application.port.in.RegisterUserUseCase`
- `com.energiaclara.application.port.out.UserRepositoryPort`
- `com.energiaclara.application.port.out.PasswordHasherPort`
- `com.energiaclara.application.port.out.TokenPort`

Los imports actuales apuntan correctamente a:

- `com.energiaclara.iam.*`
- `com.energiaclara.core.*`
- `com.energiaclara.ai.*`

### Impacto

No hay imports antiguos que corregir en esta fase.

## 3. Directorios fuente vacios

Estos directorios estan vacios y pueden eliminarse de forma segura si se desea limpiar el arbol.

### Backend application

- `backend/src/main/java/com/energiaclara/application/analytics/port/in`
- `backend/src/main/java/com/energiaclara/application/analytics/port/out`
- `backend/src/main/java/com/energiaclara/application/dto`
- `backend/src/main/java/com/energiaclara/application/energyops/port/in`
- `backend/src/main/java/com/energiaclara/application/energyops/port/out`
- `backend/src/main/java/com/energiaclara/application/security`
- `backend/src/main/java/com/energiaclara/application/service`

Motivo:

- `application/dto` y `application/service` quedaron vacios despues de migrar IAM a `iam-service`.
- Los ports activos viven actualmente en `backend/src/main/java/com/energiaclara/application/port/in` y `backend/src/main/java/com/energiaclara/application/port/out`, no en subpaquetes por contexto.

Impacto de eliminar:

- Bajo.
- No afecta compilacion porque no contienen archivos.

### Backend domain

- `backend/src/main/java/com/energiaclara/domain/model/audit`
- `backend/src/main/java/com/energiaclara/domain/model/vo`
- `backend/src/main/java/com/energiaclara/domain/port/out`

Motivo:

- `domain/model/vo` quedo vacio despues de mover `Email` a `iam-service` y `TenantId`/`UserId` a `core-platform`.
- `domain/model/audit` quedo obsoleto tras mover `AuditEvent` a `core-platform`.

Impacto de eliminar:

- Bajo.
- No afecta compilacion porque no contienen archivos.

### Backend infrastructure persistence subpackages antiguos

- `backend/src/main/java/com/energiaclara/infrastructure/persistence/analytics/adapter`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/energyops/adapter`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/energyops/entity`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/energyops/repository`

Motivo:

- Los adapters reales estan en `backend/src/main/java/com/energiaclara/infrastructure/persistence/adapter`.
- Las entities reales estan en `backend/src/main/java/com/energiaclara/infrastructure/persistence/entity`.
- Los repositories reales estan en `backend/src/main/java/com/energiaclara/infrastructure/persistence/repository`.

Impacto de eliminar:

- Bajo.
- No afecta compilacion porque estan vacios.

### No considerar source cleanup

Directorios `target/generated-sources` y `target/generated-test-sources` aparecieron como vacios, pero son salida generada de Maven. No deben documentarse como limpieza de arquitectura fuente; se eliminan naturalmente con `mvn clean`.

## 4. `package-info.java` sin clases reales en el mismo paquete

Estos archivos son marcadores conceptuales. No rompen nada, pero varios no tienen clases reales en el mismo paquete.

### Candidatos a eliminar si se busca limpieza estricta

- `backend/src/main/java/com/energiaclara/api/rest/consumption/package-info.java`
- `backend/src/main/java/com/energiaclara/api/rest/education/package-info.java`
- `backend/src/main/java/com/energiaclara/api/rest/maintenance/package-info.java`
- `backend/src/main/java/com/energiaclara/application/audit/package-info.java`
- `backend/src/main/java/com/energiaclara/application/auth/package-info.java`
- `backend/src/main/java/com/energiaclara/application/consumption/package-info.java`
- `backend/src/main/java/com/energiaclara/application/education/package-info.java`
- `backend/src/main/java/com/energiaclara/application/maintenance/package-info.java`
- `backend/src/main/java/com/energiaclara/domain/analytics/package-info.java`
- `backend/src/main/java/com/energiaclara/domain/audit/package-info.java`
- `backend/src/main/java/com/energiaclara/domain/auth/package-info.java`
- `backend/src/main/java/com/energiaclara/infrastructure/config/package-info.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/analytics/package-info.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/audit/package-info.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/auth/package-info.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/consumption/package-info.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/education/package-info.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/energyops/package-info.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/maintenance/package-info.java`

Impacto de eliminar:

- Bajo en compilacion.
- Medio en documentacion arquitectonica, porque algunos marcadores ayudan a mostrar bounded contexts pendientes.

Recomendacion:

- No eliminarlos automaticamente.
- Conservarlos si se quieren mostrar contextos MVP/conceptuales al docente.
- Eliminarlos si el objetivo es limpieza estricta del arbol fuente.

### No eliminar por ahora

- `backend/src/main/java/com/energiaclara/api/rest/auth/package-info.java`: el paquete contiene `AuthController`.
- `backend/src/main/java/com/energiaclara/domain/consumption/package-info.java`: el paquete contiene `MeterStatus`.
- `backend/src/main/java/com/energiaclara/domain/education/package-info.java`: el paquete contiene `ChallengeStatus`.
- `backend/src/main/java/com/energiaclara/domain/maintenance/package-info.java`: el paquete contiene `TicketPriority` y `TicketStatus`.

## 5. Packages que ya no son utilizados

### Obsoletos por migracion IAM

- `backend/src/main/java/com/energiaclara/application/dto`
- `backend/src/main/java/com/energiaclara/application/service`
- `backend/src/main/java/com/energiaclara/domain/model/vo`

Estado:

- Vacias.
- Quedaron de la arquitectura anterior antes de migrar IAM a `iam-service`.

Puede eliminarse:

- Si, de forma segura, porque no contienen archivos.

### Obsoletos por migracion core-platform

- `backend/src/main/java/com/energiaclara/domain/model/audit`
- `backend/src/main/java/com/energiaclara/domain/model/vo`

Estado:

- Vacias.
- Responsabilidades ya estan en `core-platform`.

Puede eliminarse:

- Si, de forma segura, porque no contienen archivos.

### Obsoletos por reorganizacion persistence

- `backend/src/main/java/com/energiaclara/infrastructure/persistence/analytics/adapter`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/energyops/adapter`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/energyops/entity`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/energyops/repository`

Estado:

- Vacias.
- La implementacion real vive en paquetes comunes `adapter`, `entity`, `repository`.

Puede eliminarse:

- Si, de forma segura, porque no contienen archivos.

## 6. Clases huerfanas

Se detectaron enums conceptuales sin referencias runtime fuera de su propia declaracion:

- `backend/src/main/java/com/energiaclara/domain/consumption/MeterStatus.java`
- `backend/src/main/java/com/energiaclara/domain/education/ChallengeStatus.java`
- `backend/src/main/java/com/energiaclara/domain/maintenance/TicketPriority.java`
- `backend/src/main/java/com/energiaclara/domain/maintenance/TicketStatus.java`

Estado:

- No estan duplicadas.
- No rompen build.
- Representan contextos MVP/conceptuales todavia no implementados.

Impacto de eliminar:

- Bajo en compilacion actual.
- Medio en defensa academica si se quiere mostrar limites de contextos futuros.

Recomendacion:

- No eliminarlas todavia si se desea mantener la narrativa de bounded contexts `consumption`, `education` y `maintenance`.
- Eliminarlas solo si el docente exige que no existan clases sin comportamiento real.

## 7. DTOs duplicados

### Auth

No hay duplicado real.

- REST/publico en backend:
  - `LoginRequest`
  - `LoginResponse`
  - `RegisterRequest`
- Application/interno en `iam-service`:
  - `LoginCommand`
  - `LoginResult`
  - `RegisterUserCommand`

Impacto de eliminar:

- No eliminar.
- Los DTO REST preservan JSON publico.
- Los comandos/resultados IAM preservan separacion application.

### EnergyOps

No hay duplicado real.

- REST/publico:
  - `AnalyzeReadingRequest`
  - `AnalyzeReadingResponse`
- Application/interno:
  - `AnalyzeEnergyReadingCommand`
  - `AnalyzeEnergyReadingResult`
- Integracion IA interna:
  - `EnergyAiAnalysisRequest`
  - `EnergyAiAnalysisOutcome`
- ai-service application:
  - `EnergyAiAnalysisCommand`
  - `EnergyAiAnalysisResponse`

Impacto de eliminar:

- No eliminar.
- Tienen responsabilidades distintas por capa.
- Eliminar alguno romperia endpoint, caso de uso o adapter IA.

### Analytics

No hay duplicado real.

- REST/publico:
  - `DashboardResponse`
  - `AnomalyDto`
  - `KpiSnapshotDto`
- Application/interno:
  - `AnalyticsDashboardResult`
  - `AnalyticsDashboardMetricsResult`
  - `AnomalyResult`
  - `KpiSnapshotResult`

Impacto de eliminar:

- No eliminar.
- Son pares REST/application necesarios para no mezclar contrato HTTP con caso de uso.

## 8. Services duplicados

No se detectaron services duplicados.

Services activos:

- `iam-service`: `AuthApplicationService`
- `backend`: `EnergyAnalysisService`
- `backend`: `AnalyticsQueryService`
- `ai-service`: `AnalyzeEnergyWithAiService`

Impacto de eliminar:

- No eliminar.
- Cada service tiene una responsabilidad distinta.

## 9. Domain models duplicados

No se detectaron domain models duplicados despues de la migracion.

Estado actual:

- IAM domain vive en `iam-service`.
- Transversales viven en `core-platform`.
- EnergyOps domain vive en backend porque no se ha migrado EnergyOps.
- AI domain vive en `ai-service`.

No hay duplicados de:

- `User`
- `Role`
- `Email`
- `TenantId`
- `UserId`
- `AuditEvent`
- `HybridSeverity`
- `AnomalySeverity`

Nota:

- `HybridSeverity` y `AnomalySeverity` no son duplicados: pertenecen a contextos distintos.
  - `HybridSeverity`: severidad sugerida por IA.
  - `AnomalySeverity`: severidad operativa de EnergyOps y SQL Server.

## 10. Que sigue siendo utilizado y no debe eliminarse

### core-platform

No eliminar:

- `TenantId`
- `UserId`
- `AuthenticatedUser`
- `TenantContextHolder`
- `AuditEvent`

Motivo:

- Usados por IAM, security, audit y backend.

### iam-service

No eliminar:

- dominio IAM;
- comandos/resultados internos;
- puertos IAM;
- `AuthApplicationService`.

Motivo:

- Login y register dependen de estas clases.
- Backend depende de `iam-service`.

### ai-service

No eliminar:

- `ai-domain`;
- `ai-application`;
- `ai-infrastructure`.

Motivo:

- EnergyOps integrado usa `AnalyzeEnergyWithAiUseCase`.
- Backend adapter usa `DefaultEnergyAiEngineAdapter`.
- Tests de IA cubren domain/application/infrastructure.

### backend adapters

No eliminar:

- `AuthController`
- `SecurityConfig`
- `JwtAuthFilter`
- `JwtTokenAdapter`
- `BCryptPasswordHasherAdapter`
- `UserRepositoryAdapter`
- entities y repositories JPA IAM
- `HybridEnergyAiAnalysisAdapter`

Motivo:

- Conservan endpoints, JWT, Bearer, Spring Boot, SQL Server e integracion IA.

## 11. Que puede eliminarse de forma segura

Solo directorios vacios de source:

```text
backend/src/main/java/com/energiaclara/application/analytics/port/in
backend/src/main/java/com/energiaclara/application/analytics/port/out
backend/src/main/java/com/energiaclara/application/dto
backend/src/main/java/com/energiaclara/application/energyops/port/in
backend/src/main/java/com/energiaclara/application/energyops/port/out
backend/src/main/java/com/energiaclara/application/security
backend/src/main/java/com/energiaclara/application/service
backend/src/main/java/com/energiaclara/domain/model/audit
backend/src/main/java/com/energiaclara/domain/model/vo
backend/src/main/java/com/energiaclara/domain/port/out
backend/src/main/java/com/energiaclara/infrastructure/persistence/analytics/adapter
backend/src/main/java/com/energiaclara/infrastructure/persistence/energyops/adapter
backend/src/main/java/com/energiaclara/infrastructure/persistence/energyops/entity
backend/src/main/java/com/energiaclara/infrastructure/persistence/energyops/repository
```

Impacto:

- No deberia afectar compilacion.
- Reduce ruido visual de arquitectura anterior.

## 12. Que no debe eliminarse

No eliminar todavia:

- `ai-service/modules/ai-api/README.md`
- `ai-service/modules/ai-bootstrap/README.md`
- `iam-service`
- `core-platform`
- `backend` adapters web/security/JPA
- `package-info.java` si se quiere conservar documentacion de contexts MVP
- enums conceptuales si se quiere defender contexts futuros

Impacto de eliminar indebidamente:

- Perder alineacion visual con ejemplo del docente.
- Romper login/JWT.
- Romper EnergyOps/Analytics.
- Romper SQL Server/JPA.
- Debilitar defensa academica de bounded contexts.

## 13. Orden recomendado de limpieza

### Fase cleanup 1: directorios vacios

Eliminar solo directorios vacios listados en la seccion 11.

Validar:

```bash
mvn clean test
```

### Fase cleanup 2: package-info conceptuales

Decidir con criterio academico:

- conservar si ayudan a mostrar contexts;
- eliminar si se busca arbol de codigo estrictamente funcional.

Validar:

```bash
mvn clean test
```

### Fase cleanup 3: enums conceptuales

Solo si el docente pide no dejar clases sin uso:

- `MeterStatus`
- `ChallengeStatus`
- `TicketPriority`
- `TicketStatus`

Validar:

```bash
mvn clean test
```

### Fase cleanup 4: no hacer aun

No mover ni eliminar en esta limpieza:

- JPA IAM.
- `SecurityConfig`.
- `JwtAuthFilter`.
- adapters AI.
- DTOs REST/application.

## 14. Dictamen

La migracion dejo pocos residuos peligrosos. El principal ruido es estructural, no funcional:

- directorios vacios;
- `package-info.java` conceptuales;
- enums MVP sin uso runtime.

No hay evidencia de clases duplicadas criticas ni imports antiguos activos. La limpieza segura puede empezar por directorios vacios sin tocar comportamiento.

## 15. Limpieza segura aplicada

Fecha de limpieza: 2026-06-02

Se elimino unicamente la Fase cleanup 1: directorios fuente vacios. No se eliminaron clases, `package-info.java`, enums conceptuales, endpoints, DTOs, adapters, configuracion, frontend ni scripts de base de datos.

Carpetas eliminadas:

- `backend/src/main/java/com/energiaclara/application/analytics/port/in`
- `backend/src/main/java/com/energiaclara/application/analytics/port/out`
- `backend/src/main/java/com/energiaclara/application/dto`
- `backend/src/main/java/com/energiaclara/application/energyops/port/in`
- `backend/src/main/java/com/energiaclara/application/energyops/port/out`
- `backend/src/main/java/com/energiaclara/application/security`
- `backend/src/main/java/com/energiaclara/application/service`
- `backend/src/main/java/com/energiaclara/domain/model/audit`
- `backend/src/main/java/com/energiaclara/domain/model/vo`
- `backend/src/main/java/com/energiaclara/domain/port/out`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/analytics/adapter`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/energyops/adapter`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/energyops/entity`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/energyops/repository`

Impacto esperado:

- Sin impacto funcional.
- Sin cambios de imports.
- Sin cambios de rutas HTTP.
- Sin cambios en frontend.
- Sin cambios en base de datos.
- Sin cambios en IA, EnergyOps, Analytics ni IAM.

Validacion requerida:

```bash
mvn clean test
```

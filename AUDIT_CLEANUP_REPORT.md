# Auditoria de limpieza del proyecto

Fecha: 2026-06-05

Alcance: auditoria estatica sin eliminacion de archivos. Se excluyeron del analisis de duplicados exactos los directorios generados `target`, `node_modules`, `.git` y `dist`.

## Resumen ejecutivo

- No se detectaron archivos duplicados exactos por hash fuera de carpetas generadas.
- Si existen carpetas fuente vacias heredadas.
- Existen modulos/carpetas enterprise preparados pero todavia no conectados como modulos Maven (`ai-api`, `ai-bootstrap`).
- Existen enums conceptuales sin referencias activas.
- Existen exports mock de frontend sin uso (`mockTechnicians`, `getMockTicketById`), pero el archivo `mockService.js` completo no se puede eliminar porque aun alimenta pantallas de retos/dashboard.
- Existen documentos de arquitectura parcialmente obsoletos porque describen mantenimiento/education como mock o sin backend activo, aunque mantenimiento ya tiene backend real.
- Maven `dependency:analyze` reporta falsos positivos esperables con starters Spring Boot y dependencias runtime; no eliminar dependencias solo con ese resultado.

## Se puede eliminar con seguridad

### Carpetas vacias

Estas carpetas no contienen archivos ni `package-info.java`; pueden eliminarse sin afectar compilacion.

- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/application/analytics/port`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/application/energyops/port`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/domain/model`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/domain/port`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/test/java/com/energiaclara/bootstrap`

### Artefactos generados locales

Se pueden limpiar y regenerar con Maven/NPM. No son codigo fuente.

- `/Users/rivero/IdeaProjects/EnergiaClara-IA/core-plataform/core-platform/target`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/iam-service/target`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-domain/target`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-application/target`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-infrastructure/target`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/target`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/dist`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/node_modules`

Comandos regeneradores:

```bash
mvn clean test
cd frontend && npm install && npm run build
```

## Se puede archivar

### Mockups HTML/CSS no utilizados por React

Estos archivos ya aparecen como eliminados en `git status` y no forman parte del build React/Vite actual. Si se necesitan como evidencia visual del prototipo, conviene archivarlos fuera del codigo fuente activo.

- `/Users/rivero/IdeaProjects/EnergiaClara-IA/Pantallas/index.html`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/Pantallas/01-dashboard-kpis.html`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/Pantallas/02-registro-lectura.html`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/Pantallas/03-anomalia-ia.html`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/Pantallas/04-crear-ticket.html`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/Pantallas/05-retos-ranking.html`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/Pantallas/06-mobile-tickets.html`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/Pantallas/07-mobile-cierre.html`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/Pantallas/styles.css`

### Documentacion historica o de decision

No es codigo ejecutable. Puede archivarse cuando ya no se necesite defender la evolucion del proyecto.

- `/Users/rivero/IdeaProjects/EnergiaClara-IA/DECISION_REPORT_DOCENTE.md`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/MIGRATION_PLAN_DOCENTE.md`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/IAM_MIGRATION_PLAN.md`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/ARCHITECTURE_GAP_ANALYSIS.md`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/CLEANUP_REPORT.md`

Motivo: son utiles como trazabilidad academica, pero algunas secciones describen fases previas ya superadas.

### Evidencia demo cargada localmente

Archivo subido por pruebas de ticket. Se puede archivar si no se necesita demostrar evidencia fotografica.

- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/uploads/ticket-evidence/77777777-7777-7777-7777-777777777777-1780519640345.png`

## Requiere validacion manual

### Modulos/carpetas Maven o enterprise sin uso completo

No tienen `pom.xml` ni clases Java activas. Existen para alineacion con arquitectura docente.

- `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-api`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-api/README.md`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-bootstrap`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-bootstrap/README.md`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/scripts`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/scripts/README.md`

Recomendacion: no borrar si el docente espera ver la estructura objetivo. Archivar o eliminar solo si se decide que no se implementara `ai-api`, `ai-bootstrap` ni scripts operativos.

### Clases Java con baja o nula referencia directa

Pueden ser usadas por Spring via escaneo/reflexion o mantenerse como conceptos de dominio. No borrar sin correr `mvn clean test` y pruebas HTTP.

- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/api/rest/GlobalExceptionHandler.java`  
  Motivo: no se importa directamente, pero Spring lo detecta por `@RestControllerAdvice`.

- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/infrastructure/audit/AuditAsyncConfig.java`  
  Motivo: configuracion Spring por anotaciones.

- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/infrastructure/config/StaticUploadsConfig.java`  
  Motivo: configuracion Spring MVC para `/uploads/**`.

- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/infrastructure/persistence/adapter/EnergyKpiSnapshotPersistenceAdapter.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/application/port/out/SaveEnergyKpiSnapshotPort.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/application/energyops/dto/EnergyKpiSnapshotRecord.java`

Motivo: adapter/puerto de snapshot KPI existe, pero el comentario del adapter indica que el MVP calcula KPIs on-the-fly. Candidato a eliminar solo si se confirma que no habra persistencia de snapshots desde backend.

### Enums conceptuales no usados

No tienen referencias activas fuera de su declaracion.

- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/domain/consumption/MeterStatus.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/domain/education/ChallengeStatus.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/domain/maintenance/TicketPriority.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/domain/maintenance/TicketStatus.java`

Motivo: son conceptos de dominio para bounded contexts parcialmente implementados. No borrar si se quiere conservar evidencia arquitectonica.

### Exports mock sin uso dentro de archivo usado

El archivo `mockService.js` se usa, pero estos exports no tienen consumidores.

- `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/src/services/mockService.js` export `mockTechnicians`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/src/services/mockService.js` export `getMockTicketById`

Motivo: se pueden remover de forma puntual si se valida que no se usaran en una proxima pantalla.

### Documentacion obsoleta o parcialmente desactualizada

Contiene afirmaciones que ya no coinciden del todo con el estado actual.

- `/Users/rivero/IdeaProjects/EnergiaClara-IA/README.md`  
  Motivo: aun menciona `Pantallas/` como mockups HTML estaticos.

- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/ARCHITECTURE_REVIEW.md`  
  Motivo: menciona `maintenance` como estructura preparada o frontend mock, pero ya existe backend de tickets.

- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/ARCHITECTURE_TRANSVERSAL.md`  
  Motivo: menciona `maintenance` y `education` como mock/conceptual; maintenance ya no es solo mock.

- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/ARCHITECTURE_VALIDATION.md`  
  Motivo: menciona maintenance como sin backend hexagonal activo; requiere refresco.

- `/Users/rivero/IdeaProjects/EnergiaClara-IA/ARCHITECTURE_GAP_ANALYSIS.md`  
  Motivo: referencia `Pantallas/` y mocks como estado inicial.

- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/API_CONTRACTS_CURRENT.md`  
  Motivo: revisar porque indica proteccion JWT/RBAC pendiente para produccion en secciones que ya tienen reglas activas.

### Dependencias Maven reportadas por `dependency:analyze`

No eliminar automaticamente. El plugin no interpreta bien starters Spring Boot, dependencias transitivas usadas por anotaciones, ni dependencias runtime.

Backend reporto como "unused declared" pero deben conservarse salvo analisis especifico:

- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/pom.xml` dependency `org.springframework.boot:spring-boot-starter-web`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/pom.xml` dependency `org.springframework.boot:spring-boot-starter-security`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/pom.xml` dependency `org.springframework.boot:spring-boot-starter-data-jpa`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/pom.xml` dependency `org.springframework.boot:spring-boot-starter-validation`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/pom.xml` dependency `org.springframework.boot:spring-boot-starter-aop`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/pom.xml` dependency `com.microsoft.sqlserver:mssql-jdbc`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/pom.xml` dependency `io.jsonwebtoken:jjwt-impl`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/pom.xml` dependency `io.jsonwebtoken:jjwt-jackson`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/pom.xml` dependency `org.springframework.boot:spring-boot-starter-test`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/pom.xml` dependency `org.springframework.security:spring-security-test`

AI modules reportaron un ajuste posible de test dependency:

- `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-domain/pom.xml` declares `org.junit.jupiter:junit-jupiter`; analyzer sugiere `junit-jupiter-api`.
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-application/pom.xml` declares `org.junit.jupiter:junit-jupiter`; analyzer sugiere `junit-jupiter-api`.
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-infrastructure/pom.xml` declares `org.junit.jupiter:junit-jupiter`; analyzer sugiere `junit-jupiter-api`.

Recomendacion: dejar como esta si `mvn clean test` pasa; cambiar solo si se quiere afinar dependencias de test.

## No tocar

### Modulos activos

- `/Users/rivero/IdeaProjects/EnergiaClara-IA/pom.xml`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/pom.xml`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/core-plataform/core-platform/pom.xml`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/iam-service/pom.xml`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-domain/pom.xml`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-application/pom.xml`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-infrastructure/pom.xml`

Motivo: forman parte del reactor Maven activo.

### `package-info.java`

No se deben borrar si sirven como documentacion arquitectonica de bounded contexts.

- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/api/rest/auth/package-info.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/api/rest/consumption/package-info.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/api/rest/education/package-info.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/api/rest/maintenance/package-info.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/application/audit/package-info.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/application/auth/package-info.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/application/consumption/package-info.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/application/education/package-info.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/application/maintenance/package-info.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/domain/analytics/package-info.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/domain/audit/package-info.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/domain/auth/package-info.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/domain/consumption/package-info.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/domain/education/package-info.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/domain/maintenance/package-info.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/infrastructure/config/package-info.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/infrastructure/persistence/analytics/package-info.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/infrastructure/persistence/audit/package-info.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/infrastructure/persistence/auth/package-info.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/infrastructure/persistence/consumption/package-info.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/infrastructure/persistence/education/package-info.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/infrastructure/persistence/energyops/package-info.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/infrastructure/persistence/maintenance/package-info.java`

### Clases activas por endpoints, Spring, JPA o arquitectura hexagonal

No tocar sin una migracion especifica:

- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/api/rest/**`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/application/analytics/**`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/application/energyops/**`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/application/port/in/**`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/application/port/out/**`, excepto candidatos KPI marcados arriba.
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/infrastructure/persistence/entity/**`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/infrastructure/persistence/repository/**`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/infrastructure/persistence/adapter/**`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/infrastructure/security/**`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/core-plataform/core-platform/src/main/java/**`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/iam-service/src/main/java/**`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-domain/src/main/java/**`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-application/src/main/java/**`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-infrastructure/src/main/java/**`

### Frontend activo

No tocar:

- `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/src/App.jsx`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/src/main.jsx`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/src/components/**`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/src/context/AuthContext.jsx`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/src/pages/**`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/src/services/apiClient.js`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/src/services/analyticsService.js`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/src/services/authService.js`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/src/services/energyOpsService.js`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/src/services/maintenanceService.js`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/src/services/mockService.js`, salvo exports no usados indicados arriba.
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/src/styles/index.css`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/src/styles/pantallas.css`

## Duplicados

### Duplicados exactos por contenido

No se detectaron archivos duplicados exactos por hash fuera de carpetas generadas.

### Duplicados historicos por migracion

El `git status` muestra archivos antiguos eliminados y nuevas rutas activas no trackeadas. No restaurar los antiguos:

- Antiguo analytics: `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/analytics/**`
- Nuevo analytics activo: `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/api/rest/analytics/**` y `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/application/analytics/**`
- Antiguo auth: `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/api/rest/AuthController.java`
- Nuevo auth activo: `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/api/rest/auth/AuthController.java`
- Antiguo energyops: `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/energyops/**`
- Nuevo energyops activo: `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/api/rest/energyops/**`, `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/application/energyops/**`, `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/domain/energyops/**`
- Antiguo IAM/domain model: `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/domain/model/**`
- Nuevo IAM/core activo: `/Users/rivero/IdeaProjects/EnergiaClara-IA/iam-service/src/main/java/**` y `/Users/rivero/IdeaProjects/EnergiaClara-IA/core-plataform/core-platform/src/main/java/**`

## Validaciones recomendadas antes de cualquier limpieza

```bash
mvn clean test
mvn install -DskipTests
cd frontend && npm run build
```

Validaciones HTTP minimas:

- `POST /api/auth/login`
- `POST /api/energyops/analyze-reading`
- `GET /api/analytics/anomalies`
- `GET /api/maintenance/tickets`

# Reporte de comparacion y limpieza contra ProyectoAI

Fecha de auditoria: 2026-06-05

Proyecto auditado: `/Users/rivero/IdeaProjects/EnergiaClara-IA`

Proyecto ejemplo revisado: `~/Downloads/ProyectoAI.zip`, extraido temporalmente en `/tmp/ProyectoAI-example/ProyectoAI`.

No se modifico codigo ni se elimino ningun archivo. Este reporte clasifica candidatos de limpieza y diferencias arquitectonicas.

## 1. Diagnostico general

EnergíaClara AI ya esta parcialmente alineado al ejemplo enterprise del docente:

- Tiene reactor Maven raiz con `core-plataform/core-platform`, `iam-service`, `ai-service` y `backend`.
- Tiene `ai-service/pom.xml` como padre Maven y agrega solo los modulos reales: `ai-domain`, `ai-application`, `ai-infrastructure`.
- Tiene `core-platform` con responsabilidades transversales reales.
- Tiene `iam-service` como modulo Maven interno con dominio/application de IAM.
- Tiene `ai-service` con IA hibrida explicable real e integrada desde el backend.
- Conserva `backend` como bootstrap/web/security/JPA principal para no romper endpoints, JWT, SQL Server ni frontend.

La diferencia central frente a `ProyectoAI` es que el ejemplo del docente separa servicios mas agresivamente:

- `ai-service` del docente incluye `ai-api` y `ai-bootstrap` como modulos Maven reales con controllers, configuracion Spring Boot y runtime propio.
- `iam-service` del docente se parece mas a un microservicio independiente con Spring Boot, seguridad, actuator, OpenAPI y migraciones.
- `core-platform` del docente contiene mucha infraestructura y modelos IAM/audit reutilizables; EnergíaClara mantiene `core-platform` deliberadamente mas liviano.
- EnergíaClara tiene un `backend` monolitico modular y un `frontend` React, ambos ausentes en el ejemplo `ProyectoAI`.

## 2. Estructura comparada

### ProyectoAI docente

```text
ProyectoAI/
├── ai-service/
│   ├── pom.xml
│   └── modules/
│       ├── ai-api/
│       ├── ai-application/
│       ├── ai-bootstrap/
│       ├── ai-domain/
│       └── ai-infrastructure/
├── core-plataform/
│   ├── pom.xml
│   └── core-platform/
├── iam-service/
└── scripts/
```

### EnergíaClara AI actual

```text
EnergiaClara-IA/
├── pom.xml
├── backend/
├── frontend/
├── database/
├── ai-service/
│   ├── pom.xml
│   └── modules/
│       ├── ai-api/              # preparado, no modulo Maven
│       ├── ai-application/      # modulo Maven real
│       ├── ai-bootstrap/        # preparado, no modulo Maven
│       ├── ai-domain/           # modulo Maven real
│       └── ai-infrastructure/   # modulo Maven real
├── core-plataform/
│   └── core-platform/
├── iam-service/
└── scripts/
```

## 3. Diferencias arquitectonicas principales

| Area | ProyectoAI | EnergíaClara AI | Clasificacion |
| --- | --- | --- | --- |
| Runtime backend | Servicios separados o preparados como runtime propio | `backend` Spring Boot conserva controllers, security y JPA | Mantener |
| AI API | `ai-api` Maven real con controllers DTOs y OpenAPI | `ai-api` solo README preparado | Revisar manualmente |
| AI Bootstrap | `ai-bootstrap` Maven real con Spring Boot app | `ai-bootstrap` solo README preparado | Revisar manualmente |
| AI Domain/Application/Infrastructure | Modulos Maven reales | Modulos Maven reales | Mantener |
| IAM | Microservicio Spring Boot completo | Modulo Maven interno, backend conserva web/security/JPA | Mantener |
| Core Platform | Core amplio con IAM, audit, JPA, Mongo, Redis, Security | Core liviano con VO/contexto/audit transversal | Mantener |
| Frontend | No aparece | React + Vite funcional | Mantener |
| Database SQL Server | No aparece igual; ejemplo usa Postgres/pgvector en AI | Scripts SQL Server del MVP | Mantener |

## 4. Eliminar con seguridad

Estos elementos son generados o basura local. Pueden eliminarse sin afectar codigo fuente; se regeneran con Maven/Vite/npm.

| Ruta exacta | Motivo | Impacto |
| --- | --- | --- |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/target` | Build Maven generado | Se regenera con `mvn clean install` |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/core-plataform/core-platform/target` | Build Maven generado | Se regenera con `mvn clean install` |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/iam-service/target` | Build Maven generado | Se regenera con `mvn clean install` |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-domain/target` | Build Maven generado | Se regenera con `mvn clean install` |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-application/target` | Build Maven generado | Se regenera con `mvn clean install` |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-infrastructure/target` | Build Maven generado | Se regenera con `mvn clean install` |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/dist` | Build Vite generado | Se regenera con `npm run build` |

Nota: no se detectaron carpetas fuente vacias fuera de outputs generados.

## 5. Archivar

Estos archivos aportan trazabilidad academica o contexto historico, pero no son necesarios para ejecutar la aplicacion. Conviene moverlos a una carpeta tipo `docs/archive/` solo si el docente no exige ver el historial de migracion en raiz.

| Ruta exacta | Motivo | Impacto si se archiva |
| --- | --- | --- |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/ARCHITECTURE_GAP_ANALYSIS.md` | Analisis previo; algunas observaciones ya fueron superadas | Bajo; conservar enlace desde README si se archiva |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/DECISION_REPORT_DOCENTE.md` | Reporte de decision anterior a migraciones reales | Bajo; util como evidencia historica |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/CLEANUP_REPORT.md` | Reporte previo de limpieza post-migracion | Bajo; reemplazado parcialmente por reportes posteriores |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/AUDIT_CLEANUP_REPORT.md` | Auditoria previa; sigue util pero redundante con este reporte | Bajo; archivar despues de aceptar este reporte |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/ARCHITECTURE_REVIEW.md` | Revision antigua del backend; algunas secciones hablan de fase 2 ya avanzada | Bajo; no afecta build |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/ENERGYOPS_ANALYTICS_TESTING.md` | Documento especifico de pruebas anteriores | Bajo; mantener si se usa en defensa |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/API_CONTRACTS_CURRENT.md` | Contratos actuales, pero puede duplicar RUNBOOK/README | Medio; no archivar si se usa para defensa de endpoints |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/database/README_AI_DEMO_DATA.md` | Documentacion de demo AI especifica | Bajo; mantener cerca de scripts si se siguen usando |

## 6. Revisar manualmente

### 6.1 Modulos/carpetas Maven preparados

| Ruta exacta | Hallazgo | Recomendacion |
| --- | --- | --- |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-api` | Existe solo con `README.md`; en ProyectoAI es modulo Maven real | Mantener si se planea extraer controllers AI; eliminar solo si se descarta `ai-api` |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-api/README.md` | Documenta estructura futura | Mantener como evidencia docente o archivar con plan |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-bootstrap` | Existe solo con `README.md`; en ProyectoAI es modulo Maven real | Mantener si se planea bootstrap AI; eliminar solo si se confirma que backend sera el unico runtime |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-bootstrap/README.md` | Documenta estructura futura | Mantener como evidencia docente o archivar con plan |

No conviene activar estos modulos todavia porque no tienen `pom.xml` ni logica real. Hacerlo ahora seria cosmetico y podria romper el reactor Maven.

### 6.2 Documentacion potencialmente obsoleta

| Ruta exacta | Hallazgo | Recomendacion |
| --- | --- | --- |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/MIGRATION_PLAN_DOCENTE.md` | Incluye fases ya implementadas y secciones que dicen que AI/IAM siguen pendientes | Actualizar o archivar secciones antiguas |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/AI_HYBRID_IMPLEMENTATION_PLAN.md` | Contiene plan por fases; varias fases ya estan implementadas | Mantener si se marca estado final por fase |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/IAM_MIGRATION_PLAN.md` | Plan inicial; IAM fase 1 ya fue ejecutada | Mantener como plan historico, actualizar pendientes |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/ARCHITECTURE_VALIDATION.md` | Puede duplicar `ARCHITECTURE_DOCENTE_VALIDATION.md` | Unificar despues de defensa |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/ARCHITECTURE_VALIDATION.md` | Nombre duplicado con documento raiz | Revisar si contiene contenido distinto antes de archivar |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/RUNBOOK_LOCAL.md` | Sigue vigente, pero acumula muchas pruebas historicas | Mantener; limpiar solo despues de consolidar comandos finales |

### 6.3 Scripts SQL que parecen legacy o de alto riesgo

| Ruta exacta | Hallazgo | Recomendacion |
| --- | --- | --- |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/database/script.sql` | Script grande generado por SQL Server, codificacion UTF-16/bytes NUL visibles al leer; parece dump completo inicial | Revisar manualmente antes de archivar; probablemente es base schema principal |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/database/seeds.sql` | Seed base demo, usado por varios scripts | Mantener |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/database/seed-technician-maintenance-demo.sql` | Seed/migracion demo de tecnico y ficha tecnica | Mantener si la demo de tecnico sigue vigente |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/database/cleanup-maintenance-demo.sql` | Limpieza especifica de mantenimiento demo | Mantener como herramienta operativa |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/database/cleanup-anomalies-demo-visible.sql` | Limpieza visual de anomalías demo | Mantener si se siguen haciendo pruebas UI |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/database/cleanup-ai-demo-history.sql` | Borra historial demo AI; alto impacto por lecturas/anomalias | No eliminar; ejecutar solo manualmente |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/database/cleanup-energyops-ai-demo.sql` | Limpia datos AI demo | Mantener como runbook de pruebas |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/database/seed-energyops-ai-demo.sql` | Seed para baseline dinamico | Mantener |

### 6.4 Java con cero o una referencia textual

Conteo bajo no implica eliminacion segura. Spring Boot, JPA, AOP, controllers y tests pueden ser usados por convencion, anotaciones o el runtime.

| Ruta exacta | Hallazgo | Recomendacion |
| --- | --- | --- |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/infrastructure/audit/AuditAsyncConfig.java` | Una referencia textual; probable bean de configuracion Spring | Mantener, validar en runtime si se desea limpiar |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/infrastructure/config/StaticUploadsConfig.java` | Una referencia textual; configuracion Spring para `/uploads` | Mantener si evidencia de tickets usa imagenes |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/api/rest/GlobalExceptionHandler.java` | Una referencia textual; usado por `@ControllerAdvice` | Mantener |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/domain/consumption/MeterStatus.java` | Una referencia textual; enum conceptual no conectado | Revisar antes de eliminar |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/domain/education/ChallengeStatus.java` | Una referencia textual; enum conceptual de contexto education | Revisar antes de eliminar |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/domain/maintenance/TicketPriority.java` | Una referencia textual; enum conceptual de maintenance | Revisar antes de eliminar |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/domain/maintenance/TicketStatus.java` | Una referencia textual; enum conceptual de maintenance | Revisar antes de eliminar |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/infrastructure/persistence/adapter/EnergyKpiSnapshotPersistenceAdapter.java` | Una referencia textual; podria ser bean Spring usado por puerto | Mantener salvo prueba de no inyeccion |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-application/src/test/java/com/energiaclara/ai/application/AnalyzeEnergyWithAiServiceTest.java` | Una referencia textual; test ejecutado por Maven | Mantener |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-domain/src/test/java/com/energiaclara/ai/domain/HybridEnergyAiEngineTest.java` | Una referencia textual; test ejecutado por Maven | Mantener |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-infrastructure/src/test/java/com/energiaclara/ai/infrastructure/AiInfrastructureAdapterTest.java` | Una referencia textual; test ejecutado por Maven | Mantener |

### 6.5 `package-info.java`

Los siguientes archivos tienen cero referencias textuales, pero sirven como documentacion de bounded contexts y no deben eliminarse automaticamente:

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

### 6.6 DTOs, enums y modelos posiblemente muertos

| Ruta exacta | Tipo | Hallazgo | Recomendacion |
| --- | --- | --- | --- |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/domain/consumption/MeterStatus.java` | Enum | Sin consumidor claro | Revisar cuando se implemente Consumption real |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/domain/education/ChallengeStatus.java` | Enum | Sin consumidor claro | Revisar cuando se implemente Education real |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/domain/maintenance/TicketPriority.java` | Enum | Sin consumidor claro | Revisar contra `MaintenanceTicketEntity` y UI |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/domain/maintenance/TicketStatus.java` | Enum | Sin consumidor claro | Revisar contra estados persistidos |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/src/services/mockService.js` export `mockTechnicians` | Mock export | No se detecta import directo | Eliminar solo en limpieza JS especifica |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/src/services/mockService.js` export `getMockTicketById` | Mock export | No se detecta import directo | Eliminar solo en limpieza JS especifica |

No se detectaron DTOs Java duplicados eliminables con seguridad. Los DTOs de API REST y los DTOs internos application tienen roles distintos.

### 6.7 Assets, uploads y mockups

| Ruta exacta | Hallazgo | Recomendacion |
| --- | --- | --- |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/uploads/ticket-evidence/77777777-7777-7777-7777-777777777777-1780519640345.png` | Evidencia subida/demo, no asset fuente | Revisar manualmente; puede eliminarse solo si se limpia la referencia en BD o se confirma que es demo |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/index.html` | Entrada Vite | Mantener |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/src/styles/index.css` | Importado por `main.jsx` | Mantener |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/src/styles/pantallas.css` | Importado por `main.jsx` | Mantener |

No se detectaron mockups HTML/CSS externos no usados por React.

### 6.8 Dependencias Maven marcadas por `dependency:analyze`

El analizador reporta varios falsos positivos por starters Spring Boot y dependencias transitivas. No se recomienda borrar dependencias solamente con este resultado.

| Modulo | Hallazgo | Recomendacion |
| --- | --- | --- |
| `ai-domain` | `junit-jupiter` declarado, `junit-jupiter-api` usado transitivamente | Revisar si se quiere declarar `junit-jupiter-api` explicitamente; no urgente |
| `ai-application` | Mismo caso con `junit-jupiter` | Revisar manualmente |
| `ai-infrastructure` | Mismo caso con `junit-jupiter` | Revisar manualmente |
| `backend` | Starters Spring marcados unused y artefactos transitivos usados | No tocar; son necesarios para Boot/Web/Security/JPA/AOP |
| `backend` | `mssql-jdbc`, `jjwt-impl`, `jjwt-jackson` marcados unused por ser runtime | No tocar |
| `backend` | `spring-boot-starter-test`, `spring-security-test` marcados unused por agregacion test | Revisar solo si se reorganizan tests |

## 7. Mantener

### 7.1 Codigo funcional backend

Mantener sin cambios:

- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/api/rest/auth/AuthController.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/infrastructure/security/SecurityConfig.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/infrastructure/security/JwtAuthFilter.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/infrastructure/security/JwtTokenAdapter.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/api/rest/energyops/EnergyOpsController.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/application/energyops/service/EnergyAnalysisService.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/infrastructure/ai/HybridEnergyAiAnalysisAdapter.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/api/rest/analytics/AnalyticsController.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/api/rest/maintenance/MaintenanceTicketController.java`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/infrastructure/persistence/entity`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/src/main/java/com/energiaclara/infrastructure/persistence/repository`

Motivo: son adapters web/security/JPA necesarios para mantener Spring Boot, endpoints actuales, SQL Server y frontend.

### 7.2 Modulos enterprise reales

Mantener:

- `/Users/rivero/IdeaProjects/EnergiaClara-IA/core-plataform/core-platform`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/iam-service`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/pom.xml`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-domain`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-application`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-infrastructure`

Motivo: todos aportan valor real y estan en el reactor Maven.

### 7.3 Frontend React

Mantener:

- `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/src`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/package.json`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/package-lock.json`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/vite.config.js`
- `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/index.html`

Motivo: `npm run build` usa esta estructura; los servicios React consumen endpoints actuales.

### 7.4 Archivos de configuracion local

| Ruta exacta | Recomendacion |
| --- | --- |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/.gitignore` | Mantener |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/.idea/vcs.xml` | Revisar segun politica del equipo |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/.idea/workspace.xml` | Revisar; normalmente no se versiona, pero no eliminar sin confirmar |

## 8. Archivos/carpetas que existen en EnergíaClara y no en ProyectoAI

Estas diferencias no implican que sobren; muchas son propias del producto EnergíaClara.

| Ruta exacta | Motivo de diferencia | Clasificacion |
| --- | --- | --- |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend` | Runtime monolitico modular funcional; ProyectoAI separa mas servicios | Mantener |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/frontend` | React + Vite; ProyectoAI no incluye frontend | Mantener |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/database` | SQL Server schema/seeds/demo; ProyectoAI no trae esta carpeta equivalente | Mantener |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/AI_HYBRID_DEFENSE.md` | Defensa academica especifica del MVP | Mantener |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/AI_SERVICE_ARCHITECTURE.md` | Documenta IA hibrida del proyecto | Mantener |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/ARCHITECTURE_DOCENTE_VALIDATION.md` | Evidencia de alineacion al docente | Mantener |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/RUNBOOK_LOCAL.md` | Operacion local especifica | Mantener |
| `/Users/rivero/IdeaProjects/EnergiaClara-IA/backend/uploads` | Evidencias cargadas por mantenimiento | Revisar manualmente |

## 9. Modulos Maven que no aportan valor

No se detectaron modulos Maven activos sin valor.

- `core-platform`: aporta VOs, audit event y contexto de seguridad transversal.
- `iam-service`: aporta dominio/application de IAM.
- `ai-domain`: aporta nucleo puro de IA.
- `ai-application`: aporta casos de uso y puertos.
- `ai-infrastructure`: aporta adapters de IA.
- `backend`: aporta bootstrap, controllers, security, JPA y adapters reales.

`ai-api` y `ai-bootstrap` no son modulos Maven activos; son carpetas preparadas. Su valor es documental/arquitectonico hasta que se implementen.

## 10. Recomendacion de limpieza

### Limpieza segura inmediata

Solo limpiar outputs generados:

```bash
rm -rf /Users/rivero/IdeaProjects/EnergiaClara-IA/backend/target
rm -rf /Users/rivero/IdeaProjects/EnergiaClara-IA/core-plataform/core-platform/target
rm -rf /Users/rivero/IdeaProjects/EnergiaClara-IA/iam-service/target
rm -rf /Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-domain/target
rm -rf /Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-application/target
rm -rf /Users/rivero/IdeaProjects/EnergiaClara-IA/ai-service/modules/ai-infrastructure/target
rm -rf /Users/rivero/IdeaProjects/EnergiaClara-IA/frontend/dist
```

Validar despues:

```bash
mvn clean install
cd /Users/rivero/IdeaProjects/EnergiaClara-IA/frontend && npm run build
```

### No limpiar automaticamente

No eliminar por ahora:

- `ai-api` ni `ai-bootstrap`, porque representan la estructura docente pendiente.
- `package-info.java`, porque documentan bounded contexts.
- enums conceptuales no usados, porque pueden ser parte del modelo academico.
- scripts SQL, porque algunos son base de schema/demo y otros son herramientas de limpieza controlada.
- dependencies Maven marcadas unused por `dependency:analyze`, porque hay falsos positivos por Spring Boot.
- evidencia de uploads, porque puede tener referencia en SQL Server.

## 11. Conclusion

EnergíaClara AI cumple parcialmente y de forma prudente con la arquitectura enterprise del docente: tiene modulos reales para core, IAM y AI, pero conserva `backend` como runtime principal para proteger endpoints, frontend, JWT y SQL Server.

La limpieza segura real es pequena: principalmente `target/` y `frontend/dist`. Lo demas debe mantenerse o revisarse manualmente porque aporta trazabilidad academica, estructura objetivo o funcionalidad activa.

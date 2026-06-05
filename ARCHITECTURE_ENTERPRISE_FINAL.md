# Arquitectura Enterprise Final - EnergiaClara AI

Fecha: 2026-06-05

Este documento consolida el estado final de arquitectura de EnergiaClara AI despues de las migraciones reales hacia la estructura enterprise del ejemplo `ProyectoAI` del docente.

La conclusion tecnica es que EnergiaClara AI queda alineado parcialmente y de forma controlada con la arquitectura enterprise: conserva `backend` como shell Spring Boot/JPA funcional, y usa `core-platform`, `iam-service` y `ai-service` como modulos Maven internos con responsabilidades transversales y reutilizables.

No se realizo una separacion fisica riesgosa hacia microservicios reales porque el objetivo del MVP es mantener estables:

- endpoints actuales;
- contrato JSON consumido por frontend;
- login/JWT;
- SQL Server;
- EnergyOps;
- Analytics;
- flujo de mantenimiento/tickets;
- IA hibrida explicable integrada.

## 1. Estructura Maven final

El reactor Maven raiz queda organizado asi:

```text
EnergiaClara-IA/
├── pom.xml
├── core-plataform/
│   └── core-platform/
│       └── pom.xml
├── iam-service/
│   └── pom.xml
├── ai-service/
│   ├── pom.xml
│   └── modules/
│       ├── ai-domain/
│       │   └── pom.xml
│       ├── ai-application/
│       │   └── pom.xml
│       └── ai-infrastructure/
│           └── pom.xml
├── backend/
│   └── pom.xml
├── frontend/
└── database/
```

El `pom.xml` raiz agrega:

```text
core-plataform/core-platform
iam-service
ai-service
backend
```

El `ai-service/pom.xml` agrega internamente solo los modulos reales:

```text
modules/ai-domain
modules/ai-application
modules/ai-infrastructure
```

Los modulos `ai-api` y `ai-bootstrap` existen como carpetas preparadas, pero no estan activados en Maven.

## 2. Modulos existentes y responsabilidades

### `core-plataform/core-platform`

Responsabilidad: plataforma transversal reutilizable.

Contiene responsabilidades de bajo acoplamiento:

- value objects compartidos, como `TenantId` y `UserId`;
- contexto de tenant/autenticacion;
- modelo de usuario autenticado;
- contrato/evento de auditoria transversal.

Decision: se mantiene liviano para evitar que el core se convierta en un modulo dependiente de Spring Boot, JPA, SQL Server o del backend.

### `iam-service`

Responsabilidad: dominio y capa application de IAM/Auth.

Contiene:

- modelos de dominio IAM;
- value objects de identidad;
- comandos de login/registro;
- casos de uso;
- puertos de entrada y salida;
- servicio de aplicacion IAM.

Decision: `iam-service` es modulo Maven interno, no microservicio externo. El backend sigue conservando `AuthController`, `SecurityConfig`, `JwtAuthFilter`, adapters JWT/password y entidades JPA IAM para no romper el escaneo Spring Boot, login, JWT ni SQL Server.

### `ai-service`

Responsabilidad: IA hibrida explicable organizada en capas hexagonales.

`ai-domain`:

- nucleo puro de IA;
- baseline dinamico;
- promedio movil;
- desviacion estandar;
- Z-Score;
- anomaly score;
- severidad sugerida;
- confianza;
- explicacion dinamica;
- recomendacion dinamica;
- fallback deterministico.

`ai-application`:

- caso de uso `AnalyzeEnergyWithAiUseCase`;
- servicio de orquestacion;
- command/response;
- puertos para historial energetico y motor IA.

`ai-infrastructure`:

- adapters de IA;
- adapter default del motor;
- proveedor en memoria/demo;
- factory manual para integracion controlada.

Decision: `ai-service` no es microservicio real todavia. El backend lo consume como modulo interno mediante puertos/adapters para mantener el endpoint actual `POST /api/energyops/analyze-reading`.

### `backend`

Responsabilidad: shell principal Spring Boot/JPA del MVP.

Conserva:

- bootstrap Spring Boot;
- controllers REST;
- seguridad web;
- filtros JWT;
- configuracion Spring Security;
- entidades JPA;
- repositories;
- adapters de persistencia;
- integracion con SQL Server;
- endpoints publicos usados por frontend;
- adapters que conectan EnergyOps con `ai-service`;
- flujo de mantenimiento/tickets.

Justificacion: mover todo esto ahora a servicios separados implicaria cambiar escaneo de entidades, seguridad, wiring, CORS, despliegue y posiblemente contratos externos. Para el MVP universitario se prioriza arquitectura modular verificable sin romper funcionalidad.

### `frontend`

Responsabilidad: cliente React + Vite.

Se conserva fuera de la migracion Maven enterprise porque el ejemplo del docente esta enfocado en backend/servicios. No se modifico su estructura para no romper rutas, guards, login ni consumo de endpoints.

### `database`

Responsabilidad: scripts SQL Server del MVP.

Contiene:

- schema base;
- seeds demo;
- seeds tecnicos/mantenimiento;
- scripts de limpieza controlada para EnergyOps, Analytics, IA hibrida y mantenimiento.

No se migra a Flyway/Liquibase todavia para evitar cambiar el flujo de base de datos del proyecto.

## 3. Por que `ai-api` y `ai-bootstrap` quedan como placeholders

En el ejemplo `ProyectoAI`, `ai-api` y `ai-bootstrap` son modulos Maven reales:

- `ai-api`: controllers REST, DTOs, errores y OpenAPI.
- `ai-bootstrap`: aplicacion Spring Boot, wiring, seguridad y runtime propio.

En EnergiaClara AI quedan como placeholders documentados porque:

- no existe todavia un endpoint IA independiente que deba exponerse fuera de EnergyOps;
- la IA hibrida ya esta integrada de forma controlada al endpoint actual;
- activar modulos vacios seria una migracion cosmetica;
- crear otro Spring Boot app implicaria riesgos de CORS, seguridad, configuracion y despliegue;
- el frontend consume endpoints actuales y no debe cambiarse;
- el backend actual ya cumple la funcion de shell mientras se preserva el MVP.

Decision final: mantener `ai-api` y `ai-bootstrap` sin `pom.xml` hasta que exista una necesidad real de API IA separada o runtime IA independiente.

## 4. Flujo actual de IA hibrida explicable

```text
POST /api/energyops/analyze-reading
        |
        v
EnergyOpsController
        |
        v
EnergyAnalysisService
        |
        v
EnergyAiAnalysisPort
        |
        v
HybridEnergyAiAnalysisAdapter
        |
        v
AnalyzeEnergyWithAiUseCase
        |
        v
HybridEnergyAiEngine
        |
        v
baseline dinamico + desviacion + Z-Score + score + explicacion
        |
        v
persistencia de lectura/anomalia/KPI
        |
        v
GET /api/analytics/anomalies
```

La parte de IA corresponde al analisis estadistico explicable. La parte de negocio corresponde a severidad, impacto energetico, costo, CO2, recomendacion operativa y fallback deterministico.

No se usan LLMs ni modelos opacos.

## 5. Que se limpio

Durante la limpieza segura posterior a migraciones se eliminaron solo residuos estructurales de bajo riesgo:

- carpetas fuente vacias heredadas de etapas previas;
- directorios vacios que no contenian clases, `package-info.java`, enums ni documentacion arquitectonica;
- outputs generados cuando correspondia en validaciones locales.

La auditoria final identifico como limpiables de forma segura los outputs generados:

```text
backend/target
core-plataform/core-platform/target
iam-service/target
ai-service/modules/ai-domain/target
ai-service/modules/ai-application/target
ai-service/modules/ai-infrastructure/target
frontend/dist
```

Estos directorios no son fuente funcional; Maven/Vite los regeneran.

## 6. Que no se borro y por que

No se borraron:

- `ai-service/modules/ai-api`: placeholder de arquitectura objetivo del docente.
- `ai-service/modules/ai-bootstrap`: placeholder de arquitectura objetivo del docente.
- `package-info.java`: documentan bounded contexts y capas.
- enums conceptuales: pueden servir a futuras fases de dominio.
- scripts SQL: algunos son schema/seeds base y otros limpiezas controladas.
- documentacion de migracion: aporta trazabilidad academica y evidencia de decisiones.
- `backend/uploads`: puede contener evidencias referenciadas desde SQL Server.
- dependencias Spring Boot marcadas como no usadas por `dependency:analyze`: son falsos positivos frecuentes por uso transitivo de starters.
- `backend`: sigue siendo shell funcional necesario.
- `frontend`: no existe en el ejemplo docente, pero es parte real del producto.

Esta decision evita una limpieza agresiva que pueda romper runtime, pruebas, datos demo o defensa academica.

## 7. Comparacion con el ejemplo enterprise

| Elemento | ProyectoAI docente | EnergiaClara AI final |
| --- | --- | --- |
| `core-plataform/core-platform` | Core amplio con infraestructura e IAM reusable | Core liviano transversal |
| `iam-service` | Microservicio Spring Boot completo | Modulo Maven interno de dominio/application |
| `ai-domain` | Dominio IA puro | Dominio IA hibrida explicable puro |
| `ai-application` | Casos de uso/puertos IA | Casos de uso/puertos IA integrados a EnergyOps |
| `ai-infrastructure` | Adapters externos Spring AI/vector stores | Adapters internos/demo sin microservicio |
| `ai-api` | Modulo Maven real | Placeholder no activado |
| `ai-bootstrap` | Spring Boot app propia | Placeholder no activado |
| `backend` | No equivalente directo | Shell Spring Boot/JPA del MVP |
| `frontend` | No incluido | React + Vite funcional |
| `database` | No equivalente SQL Server | Scripts SQL Server del MVP |

## 8. Justificacion para defensa universitaria

La arquitectura puede defenderse como una migracion enterprise incremental, no cosmetica:

1. Se separaron responsabilidades reales en modulos Maven.
2. `core-platform` contiene elementos transversales reutilizables.
3. `iam-service` contiene dominio/application de identidad sin mover aun la seguridad web critica.
4. `ai-service` contiene la IA hibrida explicable en capas domain/application/infrastructure.
5. El backend opera como shell de integracion para preservar endpoints y estabilidad.
6. La IA esta conectada por puertos/adapters, respetando arquitectura hexagonal.
7. No se cambio el contrato externo del frontend.
8. No se cambio la base de datos salvo scripts controlados de demo/mantenimiento cuando fue necesario.
9. No se hizo una migracion falsa: `ai-api` y `ai-bootstrap` quedaron desactivados porque aun no tienen responsabilidad real.
10. La estrategia reduce riesgo y permite evolucionar en fases posteriores hacia servicios mas independientes.

La defensa tecnica recomendada:

> EnergiaClara AI adopta una arquitectura enterprise modular e incremental. Para el MVP se conserva un monolito modular con backend Spring Boot como shell, mientras las capacidades transversales, IAM y AI se extraen a modulos Maven reutilizables. Esta decision mantiene estabilidad funcional y demuestra separacion real de responsabilidades sin imponer una complejidad de microservicios innecesaria.

## 9. Comandos de validacion usados

Validacion Maven completa:

```bash
mvn clean install
```

Resultado esperado/observado:

```text
BUILD SUCCESS
```

Validacion de backend:

```bash
mvn -pl backend spring-boot:run
```

Observacion: si el puerto `8080` esta ocupado, Spring Boot falla por puerto en uso. Para validar que el problema no es Maven ni arquitectura:

```bash
mvn -pl backend spring-boot:run -Dspring-boot.run.arguments=--server.port=8087
```

Resultado esperado/observado:

```text
Started EnergiaclaraApplication
BUILD SUCCESS al detener la app limpiamente
```

Validacion frontend:

```bash
cd frontend
npm run build
```

Resultado esperado/observado:

```text
vite build
✓ built
```

Validaciones funcionales recomendadas:

```http
POST /api/auth/login
POST /api/energyops/analyze-reading
GET /api/analytics/anomalies
```

Tambien se recomienda validar:

- login ADMIN;
- login TECNICO;
- analisis normal de lectura;
- analisis anomalo con IA hibrida;
- visualizacion de explicacion dinamica en Analytics;
- flujo Anomalia -> Ticket;
- cierre/reparacion de ticket;
- persistencia de ficha tecnica.

## 10. Pendientes controlados

Quedan como fase futura:

- convertir `ai-api` en modulo Maven real solo si se expone API IA independiente;
- convertir `ai-bootstrap` en runtime propio solo si se necesita microservicio AI;
- mover adapters JWT/password y seguridad web a una estructura IAM mas completa;
- mover entidades/repositories IAM fuera de backend solo con validacion de `@EntityScan` y `@EnableJpaRepositories`;
- consolidar documentacion historica en `docs/archive`;
- revisar exports mock puntuales del frontend;
- evaluar Flyway/Liquibase para SQL Server en una fase de infraestructura.

## 11. Conclusion final

EnergiaClara AI cumple parcialmente con la estructura enterprise del docente y lo hace de manera adecuada para un MVP universitario.

Cumple porque:

- existe reactor Maven enterprise;
- existen modulos reales para core, IAM y AI;
- AI esta separada en domain/application/infrastructure;
- backend depende de modulos internos y actua como shell;
- los contratos externos siguen funcionando.

Cumple parcialmente porque:

- `ai-api` y `ai-bootstrap` aun no son modulos Maven reales;
- IAM aun no es microservicio independiente;
- backend conserva JPA/security/controllers por estabilidad;
- frontend y SQL Server son propios del producto, no del ejemplo base.

Esta parcialidad es deliberada y defendible: prioriza una migracion real, incremental y verificable sobre una reestructuracion cosmetica que podria romper el MVP.

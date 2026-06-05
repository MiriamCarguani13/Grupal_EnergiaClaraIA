# Validacion arquitectura docente

Fecha de validacion: 2026-06-02

## Conclusion

La arquitectura **cumple parcialmente** con el ejemplo enterprise del docente.

Cumple porque ya existen modulos reales con responsabilidades migradas:

- `core-plataform/core-platform`
- `iam-service`
- `ai-service/modules/ai-domain`
- `ai-service/modules/ai-application`
- `ai-service/modules/ai-infrastructure`
- `backend`

Cumple parcialmente porque el backend conserva adaptadores web, seguridad y JPA necesarios para mantener Spring Boot estable. Esto es intencional para un MVP universitario: se demuestra modularidad enterprise sin convertir el sistema en microservicios ni romper endpoints, frontend o base de datos.

## Reactor Maven validado

```text
energiaclara-enterprise
├── core-plataform/core-platform
├── iam-service
├── ai-service/modules/ai-domain
├── ai-service/modules/ai-application
├── ai-service/modules/ai-infrastructure
└── backend
```

El reactor actual alinea la forma fisica del proyecto con la arquitectura objetivo. `ai-api` y `ai-bootstrap` existen como estructura preparada, pero todavia no tienen logica porque no se expone un endpoint IA ni un microservicio externo.

## 1. core-platform

Estado: cumple.

Contiene responsabilidades transversales reales:

- `TenantId`
- `UserId`
- `AuthenticatedUser`
- `TenantContextHolder`
- `AuditEvent`

Estas clases son compartidas por IAM, seguridad, auditoria y flujos operativos. No dependen de Spring, JPA, backend ni infraestructura concreta.

## 2. iam-service

Estado: cumple parcialmente.

Contiene dominio y application de IAM:

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

Direccion de dependencia:

```text
iam-service -> core-platform
backend -> iam-service
```

No hay dependencia de `iam-service` hacia backend.

Nota de MVP: `AuthApplicationService` conserva `@Service` y `@Transactional` para que Spring Boot lo descubra desde el backend sin crear configuracion adicional. El dominio IAM no depende de Spring ni JPA. En una fase posterior se puede volver application mas pura y componer beans desde backend o un modulo bootstrap.

## 3. ai-service

Estado: cumple.

`ai-domain` contiene el nucleo puro de IA hibrida explicable:

- baseline dinamico
- promedio movil
- desviacion estandar
- Z-Score
- desviacion porcentual
- anomaly score
- severidad sugerida
- confianza
- explicacion dinamica
- recomendacion dinamica

`ai-application` contiene caso de uso y puertos:

- `AnalyzeEnergyWithAiUseCase`
- `AnalyzeEnergyWithAiService`
- `EnergyHistoryProviderPort`
- `EnergyAiEnginePort`

`ai-infrastructure` contiene adapters demo puros:

- `DefaultEnergyAiEngineAdapter`
- `InMemoryEnergyHistoryProviderAdapter`
- `ManualAiServiceFactory`

Validacion de dependencias:

- `ai-service` no depende de backend.
- `ai-domain` no importa Spring.
- `ai-domain` no importa JPA.
- `ai-application` trabaja por puertos.
- La integracion real con EnergyOps vive en backend infrastructure como adapter controlado.

## 4. Backend

Estado: cumple para monolito modular.

El backend conserva:

- controllers REST;
- DTOs HTTP publicos;
- `SecurityConfig`;
- `JwtAuthFilter`;
- adapters JWT/password;
- adapters JPA;
- entities JPA;
- bootstrap Spring Boot;
- integracion EnergyOps con IA por adapter.

Esto preserva:

- rutas actuales;
- request/response JSON;
- login JWT;
- Bearer token;
- SQL Server;
- frontend;
- EnergyOps;
- Analytics.

## 5. Dependencias Maven

Backend depende de:

- `core-platform`
- `iam-service`
- `ai-application`
- `ai-infrastructure`

`iam-service` depende de:

- `core-platform`

`ai-application` depende de:

- `ai-domain`

`ai-infrastructure` depende de:

- `ai-application`
- `ai-domain`

No se detecto dependencia inversa desde `iam-service` o `ai-service` hacia backend.

## 6. Por que SecurityConfig/JwtAuthFilter/JPA IAM quedan en backend

### SecurityConfig

Se queda porque configura seguridad web global:

- CORS para frontend.
- CSRF.
- sesiones stateless.
- `permitAll` de login y endpoints demo.
- method security.
- posicionamiento del filtro JWT.

Moverlo ahora podria romper todos los endpoints, no solo IAM.

### JwtAuthFilter

Se queda porque interpreta `Authorization: Bearer` para toda la aplicacion:

- AuthController.
- EnergyOps.
- Analytics.
- Auditoria.
- `TenantContextHolder`.
- `SecurityContextHolder`.

Moverlo ahora exigiria convertir IAM en un security starter o microservicio, lo cual no es necesario para el MVP.

### JPA IAM

Se queda porque las entities y repositories se escanean desde backend:

- `iam.usuario`
- `iam.rol`
- `iam.usuario_rol`

Mover JPA ahora requeriria ajustar `@EntityScan` y `@EnableJpaRepositories`, con riesgo sobre SQL Server. Se recomienda hacerlo en una fase posterior, manteniendo mappings exactos.

## 7. Frontend

Estado: no roto.

No se modifico el frontend para estas migraciones. Los contratos HTTP consumidos por frontend se mantienen:

- `POST /api/auth/login`
- `POST /api/energyops/analyze-reading`
- `GET /api/analytics/anomalies`

## 8. SQL Server

Estado: funcionando.

Evidencia runtime:

- Spring Boot inicio correctamente.
- Hikari conecto a SQL Server.
- JPA encontro 8 repositories.
- Login leyo usuario demo.
- EnergyOps persistio lectura.
- Analytics consulto anomalias.

No se cambio schema, tablas ni scripts.

## 9. Validaciones ejecutadas

### Maven

```bash
mvn clean test
```

Resultado:

```text
BUILD SUCCESS
```

Modulos exitosos:

- `core-platform`
- `iam-service`
- `ai-domain`
- `ai-application`
- `ai-infrastructure`
- `energiaclara-ai`

```bash
mvn install -DskipTests
```

Resultado:

```text
BUILD SUCCESS
```

### Spring Boot

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=0
```

Resultado:

- Backend arranco correctamente.
- SQL Server conecto correctamente.
- Puerto de validacion: `65000`.

### HTTP

Login:

- `POST /api/auth/login`
- Resultado: HTTP `200`.
- JSON con `token`, `userId`, `tenantId`, `roles`.

EnergyOps:

- `POST /api/energyops/analyze-reading`
- Header: `Authorization: Bearer <token>`.
- Resultado: HTTP `201`.
- JSON conservado.

Analytics:

- `GET /api/analytics/anomalies`
- Header: `Authorization: Bearer <token>`.
- Resultado: HTTP `200`.
- Analytics conserva explicacion dinamica de IA.

## 10. Dictamen final

Respecto al ejemplo del docente, EnergiaClara AI queda en estado:

```text
CUMPLE PARCIALMENTE, CON MIGRACIONES REALES Y FUNCIONALIDAD PRESERVADA.
```

No es una migracion cosmetica:

- `core-platform` tiene clases transversales reales.
- `iam-service` tiene dominio y application reales.
- `ai-service` tiene dominio, application e infrastructure reales.
- EnergyOps consume IA por puerto/adaptador.
- Backend usa los modulos Maven migrados.

La parte pendiente es razonable para MVP:

- mover JPA IAM en una fase posterior;
- evaluar `JwtTokenAdapter` y password adapter;
- no mover `SecurityConfig`/`JwtAuthFilter` hasta que se decida un modulo security interno;
- no crear microservicios externos.

## 11. Validacion final despues de limpieza segura

Fecha de validacion: `2026-06-02`.

La limpieza segura previa no elimino clases, logica funcional, endpoints, frontend, tablas, enums conceptuales ni `package-info.java`. Solo se retiraron directorios fuente vacios heredados.

### Build Maven

```bash
mvn clean test
mvn install -DskipTests
```

Resultado:

```text
BUILD SUCCESS
```

La compilacion confirma que:

- `backend` sigue dependiendo correctamente de `core-platform`, `iam-service`, `ai-application` y `ai-infrastructure`;
- `iam-service` no depende de backend;
- `ai-service` no depende de backend;
- `ai-domain` permanece libre de Spring/JPA.

### Runtime backend

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=0
```

Resultado:

- Spring Boot arranco correctamente.
- SQL Server conecto correctamente mediante Hikari.
- El backend se detuvo correctamente despues de la prueba.

### Contratos HTTP

Validaciones realizadas:

- `POST /api/auth/login`: HTTP `200`, token JWT emitido.
- `POST /api/energyops/analyze-reading` normal: HTTP `201`, respuesta publica conservada.
- `POST /api/energyops/analyze-reading` anomalica: HTTP `201`, IA hibrida aplicada con severidad `HIGH`.
- `GET /api/analytics/anomalies`: HTTP `200`, muestra explicacion dinamica persistida.

Evidencia de explicabilidad:

```text
Se uso baseline dinamico calculado con 7 lecturas historicas. Lectura=270.00 kWh, esperado=147.71 kWh, desviacion=82.79%, zScore=1.79, severidad=HIGH.
```

### Frontend

```bash
cd frontend
npm run dev -- --host 127.0.0.1
```

Resultado:

- `http://127.0.0.1:5173/`: HTTP `200`.
- Proxy Vite con backend en `8080`: `POST /api/auth/login` HTTP `200`.

### Dictamen post-cleanup

```text
CUMPLE PARCIALMENTE ALINEADO AL EJEMPLO ENTERPRISE, CON MODULOS REALES Y FUNCIONALIDAD PRESERVADA.
```

La alineacion es suficiente para MVP universitario: `core-platform`, `iam-service` y `ai-service` contienen responsabilidades reales, mientras backend conserva temporalmente web/security/JPA para no romper Spring Boot, JWT, SQL Server ni el frontend.

# Plan de migracion IAM/Auth hacia iam-service

Fecha de analisis: 2026-06-02

## 1. Clases actuales que pertenecen a IAM/Auth

### API REST

- `backend/src/main/java/com/energiaclara/api/rest/auth/AuthController.java`
- `backend/src/main/java/com/energiaclara/api/rest/dto/LoginRequest.java`
- `backend/src/main/java/com/energiaclara/api/rest/dto/LoginResponse.java`
- `backend/src/main/java/com/energiaclara/api/rest/dto/RegisterRequest.java`

Responsabilidad actual:

- Publicar `POST /api/auth/login`.
- Publicar `POST /api/auth/register`.
- Convertir DTO REST a comandos de aplicacion.
- Aplicar auditoria con `@Audited`.
- Proteger registro con `@PreAuthorize("hasRole('ADMIN_INSTITUCION')")`.

### Application

- `backend/src/main/java/com/energiaclara/application/service/AuthApplicationService.java`
- `backend/src/main/java/com/energiaclara/application/dto/LoginCommand.java`
- `backend/src/main/java/com/energiaclara/application/dto/LoginResult.java`
- `backend/src/main/java/com/energiaclara/application/dto/RegisterUserCommand.java`
- `backend/src/main/java/com/energiaclara/application/port/in/LoginUseCase.java`
- `backend/src/main/java/com/energiaclara/application/port/in/RegisterUserUseCase.java`
- `backend/src/main/java/com/energiaclara/application/port/out/UserRepositoryPort.java`
- `backend/src/main/java/com/energiaclara/application/port/out/PasswordHasherPort.java`
- `backend/src/main/java/com/energiaclara/application/port/out/TokenPort.java`

Responsabilidad actual:

- Validar credenciales.
- Validar usuario activo.
- Verificar password.
- Generar JWT.
- Registrar usuarios.
- Asignar roles.
- Usar puertos para persistencia, hash y token.

### Domain

- `backend/src/main/java/com/energiaclara/domain/model/User.java`
- `backend/src/main/java/com/energiaclara/domain/model/Role.java`
- `backend/src/main/java/com/energiaclara/domain/model/vo/Email.java`
- `backend/src/main/java/com/energiaclara/domain/auth/package-info.java`

Responsabilidad actual:

- Modelo de usuario.
- Roles del sistema.
- Value object de email.
- Reglas basicas de usuario activo, roles y creacion de usuario.

### Infrastructure persistence

- `backend/src/main/java/com/energiaclara/infrastructure/persistence/adapter/UserRepositoryAdapter.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/entity/UserEntity.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/entity/RoleEntity.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/entity/UserRoleEntity.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/repository/UserJpaRepository.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/repository/RoleJpaRepository.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/repository/UserRoleJpaRepository.java`
- `backend/src/main/java/com/energiaclara/infrastructure/persistence/auth/package-info.java`

Responsabilidad actual:

- Mapear tablas `iam.usuario`, `iam.rol`, `iam.usuario_rol`.
- Cargar roles de usuario.
- Guardar usuarios y asignaciones de roles.

### Infrastructure security

- `backend/src/main/java/com/energiaclara/infrastructure/security/SecurityConfig.java`
- `backend/src/main/java/com/energiaclara/infrastructure/security/JwtAuthFilter.java`
- `backend/src/main/java/com/energiaclara/infrastructure/security/JwtTokenAdapter.java`
- `backend/src/main/java/com/energiaclara/infrastructure/security/BCryptPasswordHasherAdapter.java`

Responsabilidad actual:

- Configurar Spring Security.
- Configurar CORS.
- Configurar `PasswordEncoder`.
- Validar `Authorization: Bearer`.
- Poblar `SecurityContextHolder`.
- Poblar `TenantContextHolder`.
- Generar y leer JWT.
- Hashear/verificar passwords.

### Core Platform relacionado

- `core-plataform/core-platform/src/main/java/com/energiaclara/core/security/AuthenticatedUser.java`
- `core-plataform/core-platform/src/main/java/com/energiaclara/core/security/TenantContextHolder.java`
- `core-plataform/core-platform/src/main/java/com/energiaclara/core/model/vo/TenantId.java`
- `core-plataform/core-platform/src/main/java/com/energiaclara/core/model/vo/UserId.java`

Estas clases ya son transversales y no deben moverse a IAM.

## 2. Clases que deberian moverse a iam-service

Primera migracion real recomendada, si se hace por fases:

### iam-service domain

- `User`
- `Role`
- `Email`

Paquete objetivo sugerido:

```text
iam-service/src/main/java/com/energiaclara/iam/domain
```

### iam-service application

- `AuthApplicationService`
- `LoginCommand`
- `LoginResult`
- `RegisterUserCommand`
- `LoginUseCase`
- `RegisterUserUseCase`
- `UserRepositoryPort`
- `PasswordHasherPort`
- `TokenPort`

Paquetes objetivo sugeridos:

```text
iam-service/src/main/java/com/energiaclara/iam/application
iam-service/src/main/java/com/energiaclara/iam/application/port/in
iam-service/src/main/java/com/energiaclara/iam/application/port/out
```

### iam-service infrastructure, en fase posterior

- `UserRepositoryAdapter`
- `UserEntity`
- `RoleEntity`
- `UserRoleEntity`
- `UserJpaRepository`
- `RoleJpaRepository`
- `UserRoleJpaRepository`
- `JwtTokenAdapter`
- `BCryptPasswordHasherAdapter`

Paquetes objetivo sugeridos:

```text
iam-service/src/main/java/com/energiaclara/iam/infrastructure/persistence
iam-service/src/main/java/com/energiaclara/iam/infrastructure/security
```

## 3. Clases que deberian quedarse temporalmente en backend

Para conservar endpoints, JSON, frontend, JWT y el arranque Spring sin riesgo alto, deben quedarse temporalmente:

- `AuthController`
- `LoginRequest`
- `LoginResponse`
- `RegisterRequest`
- `SecurityConfig`
- `JwtAuthFilter`
- `@Audited`
- `AuditAspect`
- `AuditAdapter`
- `GlobalExceptionHandler`
- `EnergiaclaraApplication`

Motivo:

- `AuthController` conserva rutas y JSON publicos.
- `SecurityConfig` es configuracion global del backend completo, no solo de IAM.
- `JwtAuthFilter` protege o interpreta Bearer para todos los contextos, incluyendo EnergyOps y Analytics.
- Auditoria esta aplicada por AOP sobre endpoints del backend.
- El bootstrap actual escanea packages de backend y repositories JPA.

## 4. Dependencias actuales de IAM

### Con core-platform

IAM depende correctamente de core-platform para:

- `TenantId`
- `UserId`
- `AuthenticatedUser`
- `TenantContextHolder`

Direccion deseable:

```text
iam-service -> core-platform
backend -> core-platform
```

Core Platform no debe depender de IAM.

### Con backend

Actualmente IAM esta dentro del backend y depende indirectamente de:

- Spring Web por `AuthController`.
- Spring Security por `SecurityConfig`, `JwtAuthFilter`, `@PreAuthorize`.
- Spring Transaction por `AuthApplicationService`.
- Spring Data JPA por repositories/entities.
- Auditoria por `@Audited`.
- Bootstrap scanning del backend.

Riesgo: si se mueve todo de golpe, Spring puede dejar de encontrar controllers, beans, filters o repositories.

### Con SQL Server

IAM usa tablas existentes:

- `iam.usuario`
- `iam.rol`
- `iam.usuario_rol`

Columnas criticas:

- `usuario_id`
- `inquilino_id`
- `correo`
- `contrasena_hash`
- `esta_activo`
- `rol_id`
- `nombre`
- `asignado_por`

Restriccion: no conviene cambiar schema. La migracion debe reutilizar las mismas entidades o moverlas manteniendo exactamente los mismos mappings JPA.

### Con SecurityConfig

`SecurityConfig` define:

- CSRF deshabilitado.
- CORS para `http://localhost:5173`.
- Sesion stateless.
- `POST /api/auth/login` como `permitAll`.
- `POST /api/energyops/analyze-reading` y `/api/analytics/**` como `permitAll` temporal de demo.
- `JwtAuthFilter` antes de `UsernamePasswordAuthenticationFilter`.
- Bean `PasswordEncoder`.

Aunque EnergyOps y Analytics estan en `permitAll` temporal, el filtro JWT sigue interpretando `Authorization: Bearer` cuando se envia el header. Esto importa para mantener compatibilidad con clientes que ya mandan token.

### Con JWT

`JwtTokenAdapter`:

- genera token con `subject=email`;
- agrega claims `userId`, `tenantId`, `roles`;
- firma con `app.jwt.secret`;
- usa expiracion `app.jwt.expiration-ms`;
- valida y extrae claims para `JwtAuthFilter`.

`JwtAuthFilter`:

- lee `Authorization: Bearer <token>`;
- valida token;
- crea `AuthenticatedUser`;
- crea authorities `ROLE_*`;
- llena `SecurityContextHolder`;
- llena `TenantContextHolder`.

### Con audit

Auth tiene auditoria sobre:

- `LOGIN`
- `REGISTER_USER`

Dependencias:

- `@Audited` vive en API backend.
- `AuditAspect` lee `SecurityContextHolder`, `AuthenticatedUser` y `TenantContextHolder`.
- `AuditAdapter` persiste eventos en SQL Server.

Riesgo: mover controller o security antes de audit puede perder usuario/tenant en eventos.

## 5. Riesgos al mover login/JWT

- Romper `POST /api/auth/login` si cambia controller, package scanning o DTO.
- Romper JSON si se cambian records REST.
- Romper `POST /api/auth/register` si `@PreAuthorize` no recibe authorities iguales.
- Romper Bearer para EnergyOps/Analytics si cambia formato de roles o claims JWT.
- Romper auditoria si `SecurityContextHolder` o `TenantContextHolder` no se llenan igual.
- Romper SQL Server si entities cambian `schema`, `table`, columnas o constraints.
- Duplicar beans si backend e iam-service definen al mismo tiempo `LoginUseCase`, `TokenPort`, `PasswordHasherPort` o repositories.
- Crear ciclos Maven si `iam-service` depende de backend y backend depende de `iam-service`.
- Romper component scanning si `iam-service` queda fuera del paquete base de Spring Boot.
- Dificultar pruebas si se convierte prematuramente en microservicio.

## 6. Como mantener funcionando endpoints y Bearer

### POST /api/auth/login

Mantener temporalmente:

- misma ruta en `AuthController`;
- mismo `LoginRequest`;
- mismo `LoginResponse`;
- misma semantica de error;
- mismo claim JWT;
- misma configuracion `app.jwt.secret` y `app.jwt.expiration-ms`.

Migracion recomendada:

1. Mover primero el caso de uso a `iam-service`.
2. Hacer que `AuthController` del backend inyecte `com.energiaclara.iam.application.port.in.LoginUseCase`.
3. No mover controller hasta que el modulo IAM este estable.

### POST /api/auth/register

Mantener temporalmente:

- misma ruta;
- mismo request;
- misma respuesta `{"userId": "<uuid>"}`;
- `@PreAuthorize("hasRole('ADMIN_INSTITUCION')")`;
- `@AuthenticationPrincipal AuthenticatedUser`.

Migracion recomendada:

1. Mover `RegisterUserUseCase` y comando a IAM.
2. Mantener `AuthController` en backend para conservar `@Audited` y JSON.
3. Mover persistence de usuarios solo despues de validar login y registro.

### Authorization Bearer para EnergyOps y Analytics

Mantener:

- `JwtAuthFilter` activo en backend.
- Claims actuales: `userId`, `tenantId`, `roles`.
- Prefix de authorities: `ROLE_`.
- `AuthenticatedUser` de core-platform.
- `TenantContextHolder`.

Aunque hoy EnergyOps y Analytics esten permitidos para demo, no se debe cambiar el comportamiento del filtro: si el cliente envia Bearer valido, debe seguir poblando usuario y tenant.

## 7. Opciones para iam-service

### Opcion A: modulo Maven interno

Recomendado para MVP universitario.

Forma:

```text
iam-service/
├── pom.xml
└── src/main/java/com/energiaclara/iam/
    ├── domain/
    ├── application/
    └── infrastructure/
```

Dependencias:

```text
backend -> iam-service -> core-platform
backend -> core-platform
```

Ventajas:

- Migracion real, no cosmetica.
- Mantiene un solo deploy.
- No rompe frontend.
- No exige red, gateway ni discovery.
- Permite mover dominio y casos de uso sin tocar endpoints.

Riesgos:

- Hay que cuidar component scanning y duplicidad de beans.
- Si se mueve infrastructure JPA, backend debe escanear repositories/entities de IAM o importar configuracion.

### Opcion B: servicio separado real

No recomendado ahora.

Problemas:

- Cambiaria despliegue.
- Exigiria llamadas HTTP internas o gateway.
- Complicaria JWT, refresh, CORS y auditoria.
- Aumentaria riesgo de romper login.
- Seria excesivo para MVP universitario.

### Opcion C: modulo preparado para fase 2

Util si se quiere maxima seguridad antes de migrar.

Forma:

- Crear `pom.xml`.
- Crear packages y tests de arquitectura.
- No mover logica hasta tener plan de imports.

Desventaja:

- Aporta menos valor que mover dominio/application.
- Puede parecer estructura vacia si se prolonga.

## 8. Orden de migracion por fases

### Fase 0: documentacion y pruebas base

Sin mover codigo.

Validar estado actual:

- `mvn clean test`.
- `POST /api/auth/login`.
- `POST /api/auth/register`.
- `POST /api/energyops/analyze-reading`.
- `GET /api/analytics/anomalies`.

### Fase 1: crear iam-service como modulo Maven interno

Crear:

- `iam-service/pom.xml`.
- packages `domain`, `application`, `application/port/in`, `application/port/out`, `infrastructure`.
- dependencia a `core-platform`.
- tests vacios/minimos si hace falta.

No mover todavia:

- controller;
- security config;
- filter JWT;
- JPA;
- endpoints.

Validar:

- `mvn clean test`.
- Backend smoke test.

### Fase 2: mover dominio IAM puro

Mover a `iam-service`:

- `User`.
- `Role`.
- `Email`.

Actualizar imports en backend:

- `AuthApplicationService`.
- `JwtTokenAdapter`.
- `UserRepositoryAdapter`.

Reglas:

- Domain IAM no debe importar Spring.
- Domain IAM no debe importar JPA.
- Domain IAM puede depender de core-platform.

Validar:

- `mvn clean test`.
- Login HTTP `200`.
- POST EnergyOps.
- GET Analytics.

### Fase 3: mover application IAM

Mover a `iam-service`:

- `AuthApplicationService`.
- comandos/resultados de login/registro.
- puertos de entrada.
- puertos de salida IAM: `UserRepositoryPort`, `PasswordHasherPort`, `TokenPort`.

Backend conserva:

- `AuthController`.
- REST DTOs.
- `SecurityConfig`.
- `JwtAuthFilter`.
- adapters infrastructure.

Validar:

- `mvn clean test`.
- Login HTTP `200`.
- Register HTTP con Bearer admin.
- POST EnergyOps.
- GET Analytics.
- Verificar que no hay beans duplicados.

### Fase 4: mover adapters de password/token

Mover a `iam-service` o dejar en backend segun riesgo:

- `BCryptPasswordHasherAdapter`.
- `JwtTokenAdapter`.

Recomendacion:

- Mover `BCryptPasswordHasherAdapter` antes.
- Mover `JwtTokenAdapter` con mas cuidado porque `JwtAuthFilter` lo usa para validar tokens en todo el backend.

Backend conserva:

- `JwtAuthFilter`.
- `SecurityConfig`.

Validar:

- Token emitido sigue teniendo mismos claims.
- Bearer sigue autenticando `@AuthenticationPrincipal`.
- Register sigue pasando `@PreAuthorize`.
- EnergyOps y Analytics no cambian.

### Fase 5: mover persistence IAM

Mover con mucho cuidado:

- `UserRepositoryAdapter`.
- `UserEntity`.
- `RoleEntity`.
- `UserRoleEntity`.
- `UserJpaRepository`.
- `RoleJpaRepository`.
- `UserRoleJpaRepository`.

Requisitos:

- Mantener mappings JPA exactos.
- Configurar entity scan/repository scan para packages IAM.
- No cambiar schema SQL.
- No cambiar seeds.

Validar:

- Login contra usuarios existentes.
- Registro crea usuario en `iam.usuario`.
- Registro crea asignacion en `iam.usuario_rol`.
- Roles se cargan desde `iam.rol`.

### Fase 6: evaluar mover AuthController

No recomendado para primera migracion.

Si se mueve, debe hacerse manteniendo:

- ruta `/api/auth`;
- DTOs publicos;
- `@Audited`;
- `@PreAuthorize`;
- `@AuthenticationPrincipal AuthenticatedUser`.

Riesgo:

- Mezcla IAM con API global y audit del backend.
- Puede exigir component scan adicional.

### Fase 7: evaluar SecurityConfig/JwtAuthFilter

No mover en MVP inicial.

Motivo:

- Es seguridad global de toda la aplicacion, no solo IAM.
- Afecta EnergyOps, Analytics, CORS, method security y Bearer.

Puede extraerse en una fase futura como `iam-security-starter` interno, pero no ahora.

## 9. Validaciones despues de cada fase

### Maven

```bash
mvn clean test
```

Debe pasar el reactor completo:

- `core-platform`
- `ai-domain`
- `ai-application`
- `ai-infrastructure`
- `iam-service`, cuando se conecte
- `backend`

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{
    "tenantId": "11111111-1111-1111-1111-111111111111",
    "email": "admin@demo.edu",
    "password": "Admin1234!"
  }'
```

Esperado:

- HTTP `200`.
- JSON con `token`, `userId`, `tenantId`, `roles`.
- Claims JWT mantienen `userId`, `tenantId`, `roles`.

### Register

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer <token-admin>" \
  -d '{
    "tenantId": "11111111-1111-1111-1111-111111111111",
    "email": "nuevo@demo.edu",
    "fullName": "Usuario Nuevo",
    "password": "Admin1234!",
    "roles": ["OPERADOR_ENERGIA"]
  }'
```

Esperado:

- HTTP `200`.
- JSON `{"userId":"<uuid>"}`.
- Usuario insertado en SQL Server sin cambiar schema.

### EnergyOps

```bash
curl -X POST http://localhost:8080/api/energyops/analyze-reading \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer <token>" \
  -d '{
    "facilityId": "Sede Central - Bloque B - Aula 3B",
    "meterId": "MED-DEMO-001",
    "measuredAt": "2026-06-03T15:00:00Z",
    "kwh": 101,
    "voltage": 220,
    "powerFactor": 0.95
  }'
```

Esperado:

- HTTP `201`.
- Mismo contrato JSON.
- IA hibrida no se toca.

### Analytics

```bash
curl -X GET http://localhost:8080/api/analytics/anomalies \
  -H "Authorization: Bearer <token>"
```

Esperado:

- HTTP `200`.
- Mismo contrato JSON.
- Explicaciones dinamicas de IA se mantienen.

### Seguridad/Bearer

Validar despues de cada fase:

- Bearer valido llena `SecurityContextHolder`.
- Bearer valido llena `TenantContextHolder`.
- `@AuthenticationPrincipal AuthenticatedUser` funciona.
- `@PreAuthorize("hasRole('ADMIN_INSTITUCION')")` funciona.
- Bearer invalido no autentica.
- Login sigue `permitAll`.

## 10. Recomendacion final para MVP universitario

Recomendacion: migrar IAM como modulo Maven interno, no como microservicio real.

Orden recomendado:

1. Crear `iam-service` Maven interno.
2. Mover dominio IAM puro.
3. Mover application IAM y puertos.
4. Mantener `AuthController`, `SecurityConfig` y `JwtAuthFilter` en backend.
5. Mover adapters de password/token solo cuando login y register esten estables.
6. Mover persistence IAM al final, manteniendo mappings JPA exactos.
7. No tocar IA, EnergyOps ni Analytics durante esta migracion.

La defensa academica seria:

> IAM se separa como modulo enterprise interno con dominio y casos de uso propios, manteniendo el backend como bootstrap/API gateway del monolito modular. Esto demuestra arquitectura hexagonal y separacion de responsabilidades sin asumir la complejidad operativa de microservicios ni romper contratos existentes.

No conviene:

- mover todo IAM de una vez;
- convertir IAM en microservicio real ahora;
- cambiar JWT claims;
- cambiar endpoints;
- cambiar JSON;
- cambiar base de datos;
- tocar IA/EnergyOps/Analytics durante la migracion IAM.

## Fase 1 implementada: iam-service Maven interno

Se convirtio `iam-service` en un modulo Maven real conectado al reactor raiz.

Dependencias actuales:

```text
iam-service -> core-platform
backend -> iam-service
backend -> core-platform
```

`iam-service` no depende del backend.

### Clases movidas a iam-service

Dominio IAM:

- `com.energiaclara.iam.domain.model.User`
- `com.energiaclara.iam.domain.model.Role`
- `com.energiaclara.iam.domain.model.vo.Email`

Application IAM:

- `com.energiaclara.iam.application.service.AuthApplicationService`
- `com.energiaclara.iam.application.dto.LoginCommand`
- `com.energiaclara.iam.application.dto.LoginResult`
- `com.energiaclara.iam.application.dto.RegisterUserCommand`
- `com.energiaclara.iam.application.port.in.LoginUseCase`
- `com.energiaclara.iam.application.port.in.RegisterUserUseCase`
- `com.energiaclara.iam.application.port.out.UserRepositoryPort`
- `com.energiaclara.iam.application.port.out.PasswordHasherPort`
- `com.energiaclara.iam.application.port.out.TokenPort`

### Clases que quedaron en backend

Se dejaron temporalmente en backend:

- `AuthController`: conserva `/api/auth/login`, `/api/auth/register` y JSON publico.
- `LoginRequest`, `LoginResponse`, `RegisterRequest`: son DTOs HTTP publicos.
- `SecurityConfig`: configura seguridad web global del backend.
- `JwtAuthFilter`: valida Bearer para todos los contextos.
- `JwtTokenAdapter`: adapter Spring/JWT usado por el filtro global y por el puerto `TokenPort`.
- `BCryptPasswordHasherAdapter`: adapter Spring Security para `PasswordEncoder`.
- `UserRepositoryAdapter`: adapter JPA contra SQL Server.
- `UserEntity`, `RoleEntity`, `UserRoleEntity`: mappings JPA existentes.
- `UserJpaRepository`, `RoleJpaRepository`, `UserRoleJpaRepository`: repositories escaneados por backend.

Motivo:

- Evitar romper component scanning de Spring.
- Evitar mover JPA antes de preparar entity/repository scan para IAM.
- Mantener JWT y Bearer iguales.
- Mantener endpoints y JSON sin cambios.
- Mantener SQL Server sin cambios.

### Validacion de Fase 1

Comandos ejecutados:

```bash
mvn clean test
mvn install -DskipTests
cd backend
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=0
```

Resultados:

- Reactor completo con `iam-service`: `BUILD SUCCESS`.
- `iam-service`: compilo 12 clases.
- Backend arranco correctamente.
- SQL Server conecto correctamente.
- Puerto de validacion: `64929`.

HTTP validado:

- `POST /api/auth/login`: HTTP `200`.
- `POST /api/energyops/analyze-reading` con Bearer: HTTP `201`.
- `GET /api/analytics/anomalies` con Bearer: HTTP `200`.

### Pendiente para Fase 2

- Agregar tests unitarios propios de `iam-service`.
- Evaluar mover `BCryptPasswordHasherAdapter`.
- Evaluar mover `JwtTokenAdapter` con cuidado, manteniendo claims.
- Preparar migracion JPA IAM solo despues de validar scan de entities/repositories.
- No mover `SecurityConfig` ni `JwtAuthFilter` todavia.

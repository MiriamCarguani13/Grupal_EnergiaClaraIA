# EnergíaClara AI

Plataforma multi-tenant de gestión energética institucional: detección de anomalías, tickets de mantenimiento, retos educativos.

> **Materia:** SI416 Desarrollo de aplicaciones web · **Grupo A · 2026**

---

## Arquitectura

**Monorepo Maven multi-módulo** + **Hexagonal (Ports & Adapters)** + **DDD táctico**.

```
energiaclara-platform/                   pom raíz (Spring Boot 3.2.5 parent, BOM)
├── core-platform                        shared kernel: TenantId, KwhValue, aggregates, eventos (SIN Spring)
├── iam-service                          bounded context Auth (SIN Spring, wired desde infra)
├── energiaclara-application             use cases EnergiaClara (SIN Spring)
├── energiaclara-infrastructure          @SpringBootApplication + REST + JPA + Security
│   ├── persistence/iam                  IMPLEMENTADO (login E2E)
│   ├── consumption                      SKELETON - equipo Consumption
│   ├── energyops                        SKELETON - equipo EnergyOps
│   ├── maintenance                      SKELETON - equipo Maintenance
│   ├── education                        SKELETON - equipo Education
│   └── analytics                        STUB funcional - equipo Analytics
└── ai-service                           bounded context IA (estilo microservicio)
    ├── ai-domain
    ├── ai-application
    ├── ai-infrastructure
    ├── ai-api
    └── ai-bootstrap                     SKELETON completo - equipo AI
```

**Reglas:** ver `docs/adr/` (9 ADRs documentando cada decisión). Boundaries forzados por **ArchUnit** (14 reglas).

**Cada equipo trabaja en su carpeta.** Skeleton + README dentro de cada `<context>/`.

---

## Prerrequisitos

| Herramienta | Versión | Instalar |
|---|---|---|
| Java JDK | 21 | `winget install Microsoft.OpenJDK.21` |
| Maven | 3.9+ | `winget install Apache.Maven` |
| Node.js | 20+ | `winget install OpenJS.NodeJS.LTS` |
| SQL Server | 2019+ | `winget install Microsoft.SQLServer.2022.Developer` |
| SSMS | Última | `winget install Microsoft.SQLServerManagementStudio` |

Cerrar y reabrir terminal después de instalar (para refrescar PATH).

```bash
java --version
mvn --version
node --version
```

---

## 1. Base de datos (SQL Server)

### Setup local SQL Server (Windows)

SQL Server por default usa **Windows Authentication only** + **TCP/IP deshabilitado**. Hay que habilitar modo Mixed + `sa` + TCP 1433.

**Script automatizado (correr como Admin):** `_enable_sa.ps1` + `_enable_tcp.ps1` (fuera del repo). O manual:

1. **Cambiar a modo Mixed** (PowerShell admin):
   ```powershell
   Set-ItemProperty -Path "HKLM:\SOFTWARE\Microsoft\Microsoft SQL Server\MSSQL17.MSSQLSERVER\MSSQLServer" -Name LoginMode -Value 2
   Restart-Service MSSQLSERVER -Force
   ```
   *(Ajustar nombre instancia si difiere — listar con `Get-ChildItem "HKLM:\SOFTWARE\Microsoft\Microsoft SQL Server"`)*

2. **Habilitar sa + password**:
   ```bash
   sqlcmd -S localhost -E -Q "ALTER LOGIN sa ENABLE; ALTER LOGIN sa WITH PASSWORD = 'YourStrong!Passw0rd' UNLOCK;"
   ```

3. **Habilitar TCP 1433** (PowerShell admin):
   ```powershell
   $tcp = "HKLM:\SOFTWARE\Microsoft\Microsoft SQL Server\MSSQL17.MSSQLSERVER\MSSQLServer\SuperSocketNetLib\Tcp"
   Set-ItemProperty -Path $tcp -Name Enabled -Value 1
   Set-ItemProperty -Path "$tcp\IPAll" -Name TcpPort -Value '1433'
   Set-ItemProperty -Path "$tcp\IPAll" -Name TcpDynamicPorts -Value ''
   Restart-Service MSSQLSERVER -Force
   ```

4. **Verificar**:
   ```bash
   sqlcmd -S localhost -U sa -P "YourStrong!Passw0rd" -Q "SELECT 'ok'"
   ```

### Cargar schema + seeds

Schema canónico: `database/script.sql` (DBA-owned, UTF-16 LE). `ddl-auto: none` + Flyway off (ver ADR-005).

```bash
# 1. Crear DB
sqlcmd -S localhost -U sa -P "YourStrong!Passw0rd" -Q "CREATE DATABASE EnergiaClaraDB;"

# 2. Cargar schema (32 tablas en 8 schemas: iam, core, consumo, energiaops, audit, analitica, educacion, mantenimiento)
sqlcmd -S localhost -U sa -P "YourStrong!Passw0rd" -d EnergiaClaraDB -i "database/script.sql"

# 3. Cargar roles base (6 roles: ADMIN_INSTITUCION, DIRECTOR, DOCENTE, TECNICO, ESTUDIANTE, AUDITOR)
sqlcmd -S localhost -U sa -P "YourStrong!Passw0rd" -d EnergiaClaraDB -i "../_seed_roles.sql"

# 4. Cargar seeds demo (tenant 11111111..., admin@demo.edu)
sqlcmd -S localhost -U sa -P "YourStrong!Passw0rd" -d EnergiaClaraDB -i "database/seeds.sql"
```

---

## 2. Backend

### Build completo (primera vez)

```bash
mvn clean install -DskipTests
```

Instala 11 módulos al repo Maven local. Necesario antes del primer `spring-boot:run`.

### Arrancar

```bash
mvn spring-boot:run -pl energiaclara-infrastructure
```

Corre en `http://localhost:8080`.

### Tests

```bash
mvn test
```

Incluye **14 reglas ArchUnit** verificando Dependency Rule en CI. Si rompes una, build falla.

### Swagger UI

`http://localhost:8080/swagger-ui.html` — todos los endpoints documentados con `bearer-jwt`.

### Variables de entorno

| Variable | Default | Descripción |
|---|---|---|
| `DB_URL` | `jdbc:sqlserver://localhost:1433;databaseName=EnergiaClaraDB;encrypt=false;trustServerCertificate=true` | JDBC URL |
| `DB_USERNAME` | `sa` | Usuario SQL Server |
| `DB_PASSWORD` | `YourStrong!Passw0rd` | Contraseña |
| `JWT_SECRET` | *(default dev)* | Clave JWT — **cambiar en prod** |
| `JWT_ENABLED` | `true` | `false` desactiva auth para dev sin token |

> `ddl-auto: none` + Flyway off. DBA gestiona schema (ADR-005).

---

## 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Corre en `http://localhost:5173`. Proxy `/api` → `http://localhost:8080`.

---

## Credenciales demo

| Campo | Valor |
|---|---|
| ID de Institución | `11111111-1111-1111-1111-111111111111` |
| Email | `admin@demo.edu` |
| Contraseña | `Admin1234!` |

---

## Endpoints disponibles (Hito 1)

| Método | Ruta | Auth | Estado |
|---|---|---|---|
| POST | `/api/auth/login` | público | ✅ implementado |
| POST | `/api/auth/register` | ADMIN_INSTITUCION | ✅ implementado |
| GET | `/api/auth/me` | autenticado | ✅ implementado |
| GET | `/api/analytics/dashboard` | autenticado | ✅ stub funcional (JdbcTemplate tenant-scoped) |
| GET | `/api/analytics/kpis` | autenticado | ✅ stub funcional |
| GET | `/api/analytics/anomalies` | autenticado | ✅ stub funcional |
| POST | `/api/energyops/**` | autenticado | 🟡 SKELETON (501) — equipo EnergyOps |
| POST | `/api/tickets/**` | varios | 🟡 SKELETON (501) — equipo Maintenance |
| POST | `/api/challenges/**` | varios | 🟡 SKELETON (501) — equipo Education |
| POST | `/api/ai/**` | autenticado | 🟡 SKELETON — equipo AI |

### Ejemplo login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "11111111-1111-1111-1111-111111111111",
    "email": "admin@demo.edu",
    "password": "Admin1234!"
  }'
```

Respuesta:
```json
{
  "accessToken": "eyJhbGciOiJIUzM4NCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "tenantId": "11111111-1111-1111-1111-111111111111",
  "userId": "44444444-4444-4444-4444-444444444444",
  "email": "admin@demo.edu",
  "roles": ["ADMIN_INSTITUCION"]
}
```

### Ejemplo endpoint protegido

```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <accessToken>"
```

---

## Roles disponibles

Nombres coinciden EXACTO con `[iam].[rol].nombre` y `Role` enum (ver ADR-004).

| Rol | Nivel | Función |
|---|---|---|
| `ADMIN_INSTITUCION` | Estratégico | Configura tarifas, crea usuarios |
| `DIRECTOR` | Estratégico | Supervisa KPIs, aprueba campañas |
| `DOCENTE` | Táctico | Crea retos educativos |
| `TECNICO` | Operativo | Atiende tickets de mantenimiento |
| `ESTUDIANTE` | Operativo | Participa en retos |
| `AUDITOR` | Táctico | Revisa anomalías y logs |

---

## División de trabajo por equipo

Cada equipo trabaja **dentro de su carpeta**. ArchUnit valida que no cruces fronteras.

| Equipo | Ubicación | README local |
|---|---|---|
| Consumption (lecturas) | `energiaclara-infrastructure/src/main/java/com/energiaclara/infrastructure/consumption/` | [README](energiaclara-infrastructure/src/main/java/com/energiaclara/infrastructure/consumption/README.md) |
| EnergyOps (anomalías) | `.../infrastructure/energyops/` | [README](energiaclara-infrastructure/src/main/java/com/energiaclara/infrastructure/energyops/README.md) |
| Maintenance (tickets) | `.../infrastructure/maintenance/` | [README](energiaclara-infrastructure/src/main/java/com/energiaclara/infrastructure/maintenance/README.md) |
| Education (retos) | `.../infrastructure/education/` | [README](energiaclara-infrastructure/src/main/java/com/energiaclara/infrastructure/education/README.md) |
| Analytics (KPIs) | `.../infrastructure/analytics/` | [README](energiaclara-infrastructure/src/main/java/com/energiaclara/infrastructure/analytics/README.md) |
| AI (motor IA) | `ai-service/ai-*/` | [README](ai-service/README.md) |

**Cómo arrancar tu parte:**
1. Abre README de tu carpeta — lista endpoints, aggregates, reglas Fase 2
2. Lee `docs/adr/` — decisiones arquitectónicas
3. Lee patrón implementado: `energiaclara-infrastructure/src/main/java/com/energiaclara/infrastructure/persistence/iam/` y `api/rest/AuthController.java`
4. Llena tus stubs siguiendo ese patrón
5. `mvn test` verifica que no rompiste ArchUnit
6. Branch + PR a `master`

---

## Documentación arquitectónica

- **ADRs** (Architecture Decision Records): `docs/adr/` — 9 decisiones documentadas
  - ADR-001: Monorepo multi-módulo
  - ADR-002: Hexagonal pragmática + DDD táctico
  - ADR-003: IAM Spring-free
  - ADR-004: Role enum alineado con DB
  - ADR-005: Flyway deshabilitado
  - ADR-006: Audit vía JdbcTemplate
  - ADR-007: Multi-tenant discriminador
  - ADR-008: ArchUnit boundaries
  - ADR-009: Sub-paquetes por bounded context
- **Convenciones naming**: ver ADR-009 §10
- **OpenAPI**: auto-generado en `http://localhost:8080/swagger-ui.html`

---

## Estructura completa del proyecto

```
EnergiaClara-IA-main/
├── pom.xml                            ← raíz multi-módulo + BOM Spring Boot 3.2.5
├── core-platform/                     ← shared kernel (domain)
│   └── src/main/java/com/energiaclara/core/domain/
│       ├── shared/                    ← TenantId, UserId, Email, KwhValue, Money, DomainEvent
│       ├── tenant/                    ← Tenant, TenantName, TenantStatus
│       ├── energy/                    ← EnergyReading, EnergyAnomaly, EnergyBaseline
│       ├── ticket/                    ← MaintenanceTicket, TicketPriority, TicketStatus
│       └── challenge/                 ← EnergyChallenge, ChallengeStatus
├── iam-service/                       ← bounded context Auth (Spring-free)
│   └── src/main/java/com/energiaclara/iam/
│       ├── domain/                    ← IamUser, Role, AuthenticatedPrincipal
│       └── application/               ← LoginUseCase, RegisterUserUseCase, ports, IamApplicationService
├── energiaclara-application/          ← use cases EnergiaClara
│   └── src/main/java/com/energiaclara/application/
│       ├── port/in/                   ← RegisterEnergyReadingUseCase, DetectAnomalyUseCase, ...
│       ├── port/out/                  ← *RepositoryPort, EnergyBaselineProviderPort, ...
│       └── service/                   ← EnergyOperationsApplicationService
├── energiaclara-infrastructure/       ← Spring Boot app + adapters
│   └── src/main/
│       ├── java/com/energiaclara/infrastructure/
│       │   ├── EnergiaClaraApplication.java
│       │   ├── api/                   ← transversal (AuthController, GlobalRestExceptionHandler)
│       │   ├── audit/                 ← AuditTrailService
│       │   ├── config/                ← cors, openapi, ratelimit, security, wiring
│       │   ├── security/              ← JWT adapters, context
│       │   ├── persistence/iam/       ← entities + repos + adapter IAM
│       │   ├── consumption/           ← [SKELETON] equipo Consumption
│       │   ├── energyops/             ← [SKELETON] equipo EnergyOps
│       │   ├── maintenance/           ← [SKELETON] equipo Maintenance
│       │   ├── education/             ← [SKELETON] equipo Education
│       │   └── analytics/             ← stub funcional + README refactor
│       └── resources/
│           ├── application.yml        ← config global
│           └── application-dev.yml    ← overrides DEV (jwt disabled, debug)
├── ai-service/                        ← bounded context IA (multi-módulo)
│   ├── pom.xml                        ← agregador
│   ├── ai-domain/                     ← [SKELETON] equipo AI
│   ├── ai-application/                ← [SKELETON]
│   ├── ai-infrastructure/             ← [SKELETON]
│   ├── ai-api/                        ← [SKELETON]
│   └── ai-bootstrap/                  ← [SKELETON, opcional standalone]
├── database/
│   ├── script.sql                     ← schema canónico SQL Server (UTF-16 LE)
│   └── seeds.sql                      ← tenant demo + admin + edificio + medidor + baseline
├── frontend/                          ← React 18 + Vite
│   └── src/{components,context,pages,services,styles}
├── Pantallas/                         ← mockups HTML estáticos
└── docs/
    └── adr/                           ← 9 Architecture Decision Records
```

---

## Comandos útiles

```bash
# Build completo + tests + ArchUnit
mvn clean install

# Solo build sin tests
mvn install -DskipTests

# Tests ArchUnit únicamente
mvn test -pl core-platform,iam-service,energiaclara-application,energiaclara-infrastructure

# Arrancar backend (después de install)
mvn spring-boot:run -pl energiaclara-infrastructure

# Arrancar frontend
cd frontend && npm run dev

# Verificar DB
sqlcmd -S localhost -U sa -P "YourStrong!Passw0rd" -d EnergiaClaraDB -Q "SELECT COUNT(*) FROM iam.usuario"
```

---

## Troubleshooting

**`Could not find artifact com.energiaclara:core-platform:jar`**
→ Correr `mvn install` raíz primero (instala módulos al repo local).

**`Login failed for user 'sa'`**
→ SQL Server no está en modo Mixed. Ver §1 setup arriba.

**`Connection refused: getsockopt port 1433`**
→ TCP/IP deshabilitado en SQL Server. Ver §1 paso 3.

**`Port 8080 already in use`**
→ Backend previo no murió. `Get-NetTCPConnection -LocalPort 8080 | Stop-Process -Id $_.OwningProcess -Force`

**`ArchUnit test failed: domain depends on Spring`**
→ Eliminaste un import. Revisa que no agregaste `@Service`/`@Entity` en `core-platform` o `iam-service`.

---

## Stack

- **Backend:** Spring Boot 3.2.5 · Java 21 · SQL Server 2019+ · JWT HS384 · JJWT 0.12.6 · Springdoc 2.3 · Hibernate (ddl-auto none) · ArchUnit 1.2.1
- **Frontend:** React 18 · Vite 5 · Axios · React Router 6
- **DB:** SQL Server con schemas `iam`, `core`, `consumo`, `energiaops`, `audit`, `analitica`, `educacion`, `mantenimiento`
- **Tests arquitectónicos:** ArchUnit (14 reglas activas)

---

## Equipo

Grupo A — SI416 Desarrollo de aplicaciones web · Santa Cruz de la Sierra, Bolivia · 2026

- Reyna Miriam Carguani Calle
- Paulo Andres Mayta Costas
- Sebastián Nicolas Camacho Mercado
- Rony Javier Rivero Paniagua
- José Manuel Navia Sanchez
- Rodrigo Camacho Cedeño *(arquitecto plataforma)*

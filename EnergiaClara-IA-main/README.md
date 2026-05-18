# EnergíaClara AI

Plataforma de gestión energética institucional con detección de anomalías, tickets de mantenimiento y retos educativos.

---

## Prerrequisitos

| Herramienta | Versión | Instalar |
|---|---|---|
| Java JDK | 21 | `winget install Microsoft.OpenJDK.21` |
| Maven | 3.9+ | `winget install Apache.Maven` |
| Node.js | 20+ | `winget install OpenJS.NodeJS.LTS` |
| SQL Server | 2019+ | `winget install Microsoft.SQLServer.2022.Developer` |
| SSMS | Última | `winget install Microsoft.SQLServerManagementStudio` |

> Después de instalar, cerrar y reabrir la terminal para actualizar el PATH.

Verificar:
```bash
java --version
mvn --version
node --version
```

---

## 1. Base de datos (SQL Server)

El schema canónico es `database/script.sql` (mantenido por el DBA del equipo). Es un dump completo generado desde SSMS con todos los schemas: `iam`, `core`, `consumo`, `energiaops`, `audit`, `analitica`, `educacion`, `mantenimiento`.

**Crear la DB y ejecutar el script:**

1. Abrir SSMS, conectar al servidor local.
2. Crear DB: `CREATE DATABASE EnergiaClaraDB;`
3. Abrir `database/script.sql` (encoding UTF-16 LE — SSMS lo lee directo).
4. Seleccionar la DB `EnergiaClaraDB` y ejecutar (F5).

**Seeds mínimos + extensiones de columnas (energyops + audit):**

Ejecutar `database/seeds.sql` después de `database/script.sql`. El script es idempotente
(usa `IF NOT EXISTS` / `COL_LENGTH`) y crea:

- Catálogo de roles (`iam.rol`)
- Tenant demo `11111111-...` (`core.inquilino`)
- Admin demo `admin@demo.edu` / `Admin1234!` con UUID fijo `44444444-...`
- Edificio demo `22222222-...` (`core.edificio`)
- Medidor demo `33333333-...` (`core.medidor`) — referenciado por `app.energyops.demo-medidor-id`
- Línea base demo activa (`energiaops.snapshot_linea_base`)
- `ALTER TABLE` añadiendo columnas auxiliares a `[consumo].[lectura]`, `[energiaops].[anomalia]`,
  `[energiaops].[snapshot_linea_base]` y `[audit].[evento_auditoria]` que el backend usa.

> Para regenerar el hash de la contraseña: [bcrypt-generator.com](https://bcrypt-generator.com), rounds = 10.

---

## 2. Backend

```bash
cd backend
mvn spring-boot:run
```

Corre en `http://localhost:8080`.

Variables de entorno (defaults para desarrollo local):

| Variable | Default | Descripción |
|---|---|---|
| `DB_URL` | `jdbc:sqlserver://localhost:1433;databaseName=EnergiaClaraDB;encrypt=false;trustServerCertificate=true` | JDBC URL |
| `DB_USERNAME` | `sa` | Usuario SQL Server |
| `DB_PASSWORD` | `YourStrong!Passw0rd` | Contraseña SQL Server |
| `JWT_SECRET` | *(ver application.yml)* | Clave JWT — **cambiar en producción** |

> **Nota:** `ddl-auto: none` está activo. Hibernate no valida ni modifica schema en el arranque — el DBA es la fuente de verdad.

---

## 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Corre en `http://localhost:5173`. Hace proxy de `/api` → `http://localhost:8080`.

---

## Credenciales de prueba

| Campo | Valor |
|---|---|
| ID de Institución | `11111111-1111-1111-1111-111111111111` |
| Email | `admin@demo.edu` |
| Contraseña | `Admin1234!` |

---

## Estructura del proyecto

```
EnergiaClara-IA/
├── backend/                        # Spring Boot 3.2, Java 21
│   └── src/main/java/com/energiaclara/
│       ├── domain/                 # Aggregates, Value Objects, puertos
│       ├── application/            # Casos de uso, DTOs, servicios
│       ├── infrastructure/         # JPA (schema [iam]), JWT, Spring Security
│       ├── api/                    # REST controllers, DTOs, exception handler
│       └── bootstrap/              # Main class
├── frontend/                       # React 18 + Vite
│   └── src/
│       ├── context/                # AuthContext (JWT en localStorage)
│       ├── services/               # Axios + interceptor de token
│       ├── components/             # ProtectedRoute
│       └── pages/                  # LoginPage, DashboardPage
├── database/
│   └── script.sql                  # Schema canónico SQL Server (UTF-16 LE)
└── Pantallas/                      # Mockups HTML estáticos (dashboard, lecturas, etc.)
```

---

## Endpoints disponibles (MVP Auth)

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | `/api/auth/login` | Público | Retorna JWT (auditado en `[audit].[evento_auditoria]`) |
| POST | `/api/auth/register` | ADMIN_INSTITUCION | Crea usuario en el tenant (auditado) |
| POST | `/api/energyops/analyze-reading` | Autenticado | Persiste lectura en `[consumo].[lectura]`, detecta anomalía → `[energiaops].[anomalia]` |
| GET  | `/api/analytics/dashboard` | Autenticado | KPIs derivados de lecturas + anomalías + baseline activa |
| GET  | `/api/analytics/kpis` | Autenticado | KPIs por lectura (cálculo on-the-fly) |
| GET  | `/api/analytics/anomalies` | Autenticado | Anomalías recientes (`[energiaops].[anomalia]`) |

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

### Ejemplo register (requiere token de admin)

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "tenantId": "11111111-1111-1111-1111-111111111111",
    "email": "tecnico@demo.edu",
    "fullName": "Juan Técnico Pérez",
    "password": "Tecnico1234!",
    "roles": ["TECNICO"]
  }'
```

---

## Roles disponibles

Los nombres deben coincidir EXACTAMENTE con `[iam].[rol].nombre` en la DB.

| Rol | Nivel | Descripción |
|---|---|---|
| `ADMIN_INSTITUCION` | Estratégico | Configura tarifas, crea usuarios |
| `DIRECTOR` | Estratégico | Supervisa KPIs, aprueba campañas |
| `DOCENTE` | Táctico | Crea retos educativos |
| `TECNICO` | Operativo | Atiende tickets de mantenimiento |
| `ESTUDIANTE` | Operativo | Participa en retos |
| `AUDITOR` | Táctico | Revisa anomalías y logs |

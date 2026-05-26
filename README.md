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
| Python | 3.10+ | Instalado para el microservicio de IA |

> Después de instalar, cerrar y reabrir la terminal para actualizar el PATH.

Verificar:
```bash
java --version
mvn --version
node --version
python --version
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

## 2. Ejecutar los Microservicios

El sistema consta de 3 partes que deben correr en simultáneo, cada una en una terminal distinta.

### A. Backend Principal (Spring Boot / Java)
Gestiona la base de datos, seguridad, usuarios y mantenimiento de tickets.
```bash
cd backend
mvn spring-boot:run
```
Corre en `http://localhost:8080`.

**Variables de entorno** (defaults para desarrollo local):

| Variable | Default | Descripción |
|---|---|---|
| `DB_URL` | `jdbc:sqlserver://localhost:1433;databaseName=EnergiaClaraDB;encrypt=false;trustServerCertificate=true` | JDBC URL |
| `DB_USERNAME` | `sa` | Usuario SQL Server |
| `DB_PASSWORD` | `YourStrong!Passw0rd` | Contraseña SQL Server |
| `JWT_SECRET` | *(ver application.yml)* | Clave JWT — **cambiar en producción** |

> **Nota:** `ddl-auto: none` está activo. Hibernate no valida ni modifica schema en el arranque — el DBA es la fuente de verdad.

### B. Microservicio de IA (FastAPI / Python)
Evalúa las lecturas de energía para detectar anomalías usando un modelo algorítmico y heurístico.
```bash
cd ai-service
.\.venv\Scripts\Activate.ps1
uvicorn main:app --host 0.0.0.0 --port 8090 --reload
```
Corre en `http://localhost:8090`.

### C. Frontend (React / Vite)
Interfaz de usuario de la plataforma.
```bash
cd frontend
npm install
npm run dev
```
Corre en `http://localhost:5173`. Hace proxy de `/api` → `http://localhost:8080`.

---

## 3. Funcionamiento de la IA (Detección de Anomalías)

La detección de anomalías se basa en comparar el **consumo real (kWh)** con un **consumo esperado (Línea base / Baseline)** que es dinámicamente ajustado por la Inteligencia Artificial.

### ¿Qué variables analiza la IA?
La IA toma el "consumo habitual" del medidor y lo multiplica por factores de corrección basados en la física y el comportamiento:
1. **Hora del Día**: El consumo nocturno o de madrugada debe ser mucho más bajo (aplica un factor reductor del 0.65x). Durante horas pico (9 AM - 5 PM) permite un factor del 1.05x.
2. **Día de la Semana**: Los fines de semana el consumo esperado se reduce al 80% (0.80x) respecto a un día laborable.
3. **Factor de Potencia**: Si la eficiencia eléctrica del edificio cae por debajo de 0.80, la IA espera un consumo "falso" o de penalización mayor (1.08x).
4. **Voltaje**: Variaciones de voltaje fuera de rango (200V-240V) aumentan ligeramente el consumo esperado por el estrés de los equipos (1.05x).

### ¿Cómo determina si es una anomalía y su severidad?
Compara la Lectura Real vs la Predicción Ajustada calculando un **porcentaje de desviación**. Si esa desviación supera la "tolerancia configurada" (por ej: 15%), se marca automáticamente como **ANOMALÍA**.

La severidad se asigna en función de qué tanto se sobrepasó la tolerancia:
- **CRÍTICA:** Desviación de 100% o más (El consumo es el doble de lo normal).
- **ALTA:** Desviación mayor o igual al 50%.
- **MEDIA:** Desviación mayor o igual al 25%.
- **BAJA:** Desviación menor al 25% pero aún fuera del rango de tolerancia.

---

## 4. Módulo de Tickets de Mantenimiento

El módulo permite conectar las anomalías detectadas con el equipo de soporte técnico.

* **Administrador:** Inicia sesión con `admin@demo.edu` y puede convertir cualquier anomalía en un ticket de soporte. Asigna la prioridad, área afectada, el equipo y selecciona a qué **Técnico** disponible le delegará la tarea.
* **Técnico:** Inicia sesión en su aplicación móvil (URL `/m/tickets`) con sus credenciales (ej: `juan.tecnico@demo.edu` / `Tecnico1234!`). Allí puede ver su lista de tickets asignados, los detalles del problema, y tiene la opción de cerrarlos tras escanear un QR del equipo intervenido.

---

## Credenciales de prueba

| Campo | Administrador | Técnico |
|---|---|---|
| ID de Institución | `11111111-1111-1111-1111-111111111111` | `11111111-1111-1111-1111-111111111111` |
| Email | `admin@demo.edu` | `juan.tecnico@demo.edu` |
| Contraseña | `Admin1234!` | `Tecnico1234!` |

---

## Estructura del proyecto

```text
EnergiaClara-IA/
├── backend/                        # Spring Boot 3.2, Java 21
│   └── src/main/java/com/energiaclara/
│       ├── domain/                 # Aggregates, Value Objects, dominio transversal
│       ├── application/            # Casos de uso, DTOs internos, puertos in/out, servicios
│       ├── infrastructure/         # JPA/adapters ([iam], [consumo], [energiaops], [audit]), JWT, Spring Security
│       ├── api/                    # REST controllers, DTOs externos, exception handler
│       └── bootstrap/              # Main class
├── ai-service/                     # FastAPI, Python 3.10+ (Microservicio de predicciones IA)
├── frontend/                       # React 18 + Vite
│   └── src/
│       ├── context/                # AuthContext (JWT en localStorage)
│       ├── services/               # Axios + interceptor de token
│       ├── components/             # ProtectedRoute
│       └── pages/                  # LoginPage, DashboardPage, CrearTicketPage, etc.
├── database/                       # Scripts y Seeds de SQL Server
└── Pantallas/                      # Mockups HTML estáticos
```

---

## Endpoints disponibles (MVP)

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | `/api/auth/login` | Público | Retorna JWT (auditado en `[audit].[evento_auditoria]`) |
| POST | `/api/auth/register` | ADMIN_INSTITUCION | Crea usuario en el tenant (auditado) |
| GET  | `/api/auth/users` | ADMIN_INSTITUCION | Lista usuarios por rol (ej: `?role=TECNICO`) |
| POST | `/api/maintenance/tickets` | Autenticado | Crea un nuevo ticket de mantenimiento |
| POST | `/api/energyops/analyze-reading` | Demo temporal (`permitAll`) | Persiste lectura en `[consumo].[lectura]`, detecta anomalía → `[energiaops].[anomalia]` |
| GET  | `/api/analytics/dashboard` | Demo temporal (`permitAll`) | KPIs derivados de lecturas + anomalías + baseline activa |
| GET  | `/api/analytics/kpis` | Demo temporal (`permitAll`) | KPIs por lectura (cálculo on-the-fly) |
| GET  | `/api/analytics/anomalies` | Demo temporal (`permitAll`) | Anomalías recientes (`[energiaops].[anomalia]`) |

> Para el MVP demo, EnergyOps y Analytics estan abiertos para no romper el flujo del frontend local.
> Antes de produccion deben protegerse con JWT/RBAC.

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

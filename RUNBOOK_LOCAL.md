# Runbook Local

## Prerrequisitos

- Java 21 o superior disponible en `PATH`.
- Maven 3.9 o superior.
- SQL Server escuchando en `localhost:1433`.
- Base de datos `EnergiaClaraDB` creada con `database/script.sql` y `database/seeds.sql`.

## Variables de entorno

El backend tiene defaults locales en `backend/src/main/resources/application.yml`. Si tu SQL Server usa otros valores, exportalos antes de arrancar:

```bash
export DB_URL='jdbc:sqlserver://localhost:1433;databaseName=EnergiaClaraDB;encrypt=false;trustServerCertificate=true'
export DB_USERNAME='sa'
export DB_PASSWORD='Energia2026!'
export JWT_SECRET='energiaclara-super-secret-key-32-chars-minimum!!'
```

## Preparar base de datos

Desde la raiz del proyecto, usando `sqlcmd`:

```bash
sqlcmd -S localhost,1433 -U sa -P 'Energia2026!' -Q "IF DB_ID('EnergiaClaraDB') IS NULL CREATE DATABASE EnergiaClaraDB"
sqlcmd -S localhost,1433 -U sa -P 'Energia2026!' -d EnergiaClaraDB -i database/script.sql
sqlcmd -S localhost,1433 -U sa -P 'Energia2026!' -d EnergiaClaraDB -i database/seeds.sql
```

Si usas SSMS o Azure Data Studio, ejecuta primero `database/script.sql` y despues `database/seeds.sql` sobre `EnergiaClaraDB`.

## Verificar backend

```bash
cd backend
mvn clean test
```

Resultado esperado:

```text
BUILD SUCCESS
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

## Ejecutar backend

```bash
cd backend
mvn spring-boot:run
```

Resultado esperado:

```text
HikariPool-1 - Start completed.
Tomcat started on port 8080 (http) with context path ''
Started EnergiaclaraApplication
```

Backend disponible en:

```text
http://localhost:8080
```

## Credenciales demo

```text
tenantId: 11111111-1111-1111-1111-111111111111
email: admin@demo.edu
password: Admin1234!
```

## Login de prueba

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{
    "tenantId": "11111111-1111-1111-1111-111111111111",
    "email": "admin@demo.edu",
    "password": "Admin1234!"
  }'
```

## Pruebas EnergyOps con IA hibrida

Obtener token:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{
    "tenantId": "11111111-1111-1111-1111-111111111111",
    "email": "admin@demo.edu",
    "password": "Admin1234!"
  }' | sed -E 's/.*"token":"([^"]+)".*/\1/')
```

Lectura normal:

```bash
curl -X POST http://localhost:8080/api/energyops/analyze-reading \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "facilityId": "Sede Central - Bloque B - Aula 3B",
    "meterId": "MED-DEMO-001",
    "measuredAt": "2026-06-03T10:00:00Z",
    "kwh": 101,
    "voltage": 220,
    "powerFactor": 0.95
  }'
```

Resultado esperado: `201`, `anomalyDetected=false`.

Lectura anomalica:

```bash
curl -X POST http://localhost:8080/api/energyops/analyze-reading \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "facilityId": "Sede Central - Bloque B - Aula 3B",
    "meterId": "MED-DEMO-001",
    "measuredAt": "2026-06-03T11:00:00Z",
    "kwh": 250,
    "voltage": 220,
    "powerFactor": 0.95
  }'
```

Resultado esperado: `201`, `anomalyDetected=true`, severidad compatible con `LOW`, `MEDIUM`, `HIGH` o `CRITICAL`.

Consultar anomalias:

```bash
curl -X GET http://localhost:8080/api/analytics/anomalies \
  -H "Authorization: Bearer $TOKEN"
```

Cuando hay historial suficiente para el medidor, la anomalia puede incluir una explicacion dinamica similar a:

```text
Se uso baseline dinamico calculado con 7 lecturas historicas...
```

Cuando no hay historial suficiente, el dominio IA devuelve fallback y EnergyOps conserva el calculo deterministico anterior. El contrato externo no muestra `aiUsed`, `confidence` ni `modelVersion` para no romper frontend; el fallback esta cubierto por tests unitarios.

### Postman / Thunder Client

Crear una variable:

```text
baseUrl = http://localhost:8080
```

#### 1. Login

Metodo:

```text
POST {{baseUrl}}/api/auth/login
```

Headers:

```text
Content-Type: application/json
```

Body:

```json
{
  "tenantId": "11111111-1111-1111-1111-111111111111",
  "email": "admin@demo.edu",
  "password": "Admin1234!"
}
```

Respuesta esperada:

```json
{
  "token": "<jwt>",
  "userId": "44444444-4444-4444-4444-444444444444",
  "tenantId": "11111111-1111-1111-1111-111111111111",
  "roles": ["ADMIN_INSTITUCION"]
}
```

Guardar el valor `token` como variable `token`.

#### 2. Lectura normal

Metodo:

```text
POST {{baseUrl}}/api/energyops/analyze-reading
```

Headers:

```text
Content-Type: application/json
Authorization: Bearer {{token}}
```

Body:

```json
{
  "facilityId": "Sede Central - Bloque B - Aula 3B",
  "meterId": "MED-DEMO-001",
  "measuredAt": "2026-06-03T13:00:00Z",
  "kwh": 101,
  "voltage": 220,
  "powerFactor": 0.95
}
```

Respuesta esperada:

```json
{
  "readingId": "<uuid>",
  "anomalyId": null,
  "anomalyDetected": false,
  "severity": null,
  "deviationPercent": -32.41,
  "recommendation": "Consumo dentro del patron esperado; continuar monitoreo regular.",
  "estimatedCostImpact": 0.00,
  "estimatedCo2Impact": 0.00
}
```

El valor exacto de `deviationPercent` puede variar si el historial cambia por nuevas pruebas, pero debe conservar el contrato JSON.

#### 3. Lectura anomalica

Metodo:

```text
POST {{baseUrl}}/api/energyops/analyze-reading
```

Headers:

```text
Content-Type: application/json
Authorization: Bearer {{token}}
```

Body:

```json
{
  "facilityId": "Sede Central - Bloque B - Aula 3B",
  "meterId": "MED-DEMO-001",
  "measuredAt": "2026-06-03T14:00:00Z",
  "kwh": 260,
  "voltage": 220,
  "powerFactor": 0.95
}
```

Respuesta esperada:

```json
{
  "readingId": "<uuid>",
  "anomalyId": "<uuid>",
  "anomalyDetected": true,
  "severity": "HIGH",
  "deviationPercent": 88.21,
  "recommendation": "Inspeccionar equipos de alto consumo, climatizacion e iluminacion del area.",
  "estimatedCostImpact": 183.55,
  "estimatedCo2Impact": 53.62
}
```

#### 4. Analytics con explicacion dinamica

Metodo:

```text
GET {{baseUrl}}/api/analytics/anomalies
```

Headers:

```text
Authorization: Bearer {{token}}
```

Respuesta esperada en la primera anomalia reciente:

```json
{
  "type": "EXCESS_CONSUMPTION",
  "severity": "HIGH",
  "deviationPercent": 88.2100,
  "explanation": "Se uso baseline dinamico calculado con 7 lecturas historicas. Lectura=260.00 kWh, esperado=138.14 kWh, desviacion=88.21%, zScore=2.44, severidad=HIGH.",
  "recommendation": "Inspeccionar equipos de alto consumo, climatizacion e iluminacion del area.",
  "estimatedCostImpact": 183.55,
  "estimatedCo2Impact": 53.62
}
```

Evidencia clave para defensa: la explicacion contiene `baseline dinamico`, numero de lecturas historicas, lectura actual, esperado, desviacion, `zScore` y severidad.

## Problemas frecuentes

- `No se pudo realizar la conexion TCP/IP al host localhost, puerto 1433`: SQL Server no esta corriendo, TCP/IP no esta habilitado, el puerto no es `1433` o las credenciales no coinciden.
- `Login failed for user 'sa'`: ajustar `DB_USERNAME`/`DB_PASSWORD` o habilitar autenticacion SQL Server.
- `Address already in use` en `8080`: detener el proceso que usa el puerto o ejecutar con `mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081`.
- `The database EnergiaClaraDB does not exist`: crear la base y ejecutar los scripts indicados arriba.

## Evidencia arquitectura docente

Documentos de cierre:

- `ARCHITECTURE_DOCENTE_VALIDATION.md`: validacion final contra la arquitectura enterprise del docente.
- `MIGRATION_SUMMARY.md`: resumen de migraciones reales a `core-platform`, `iam-service` y `ai-service`.

Validacion final ejecutada:

```bash
mvn clean test
mvn install -DskipTests
cd backend
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=0
```

Resultado esperado y validado:

- Login HTTP `200`.
- EnergyOps con Bearer HTTP `201`.
- Analytics con Bearer HTTP `200`.
- SQL Server conectado.
- Frontend no modificado.

## Validacion final post-cleanup

Fecha de validacion: `2026-06-02`.

### 1. Build completo

Desde la raiz del proyecto:

```bash
mvn clean test
mvn install -DskipTests
```

Resultado validado:

```text
BUILD SUCCESS
```

### 2. Backend

Para validar runtime aislado:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=0
```

Para validar proxy del frontend:

```bash
cd backend
mvn spring-boot:run
```

Resultado validado:

- Spring Boot arranca.
- SQL Server conecta.
- Login, EnergyOps y Analytics responden correctamente.

### 3. Requests finales validados

Login:

```http
POST /api/auth/login
Content-Type: application/json
```

Resultado: HTTP `200`.

EnergyOps normal:

```http
POST /api/energyops/analyze-reading
Authorization: Bearer <token>
Content-Type: application/json
```

Body usado:

```json
{
  "facilityId": "Sede Central - Bloque B - Aula 3B",
  "meterId": "MED-DEMO-001",
  "measuredAt": "2026-06-03T18:00:00Z",
  "kwh": 101,
  "voltage": 220,
  "powerFactor": 0.95
}
```

Resultado validado: HTTP `201`, `anomalyDetected=false`.

EnergyOps anomalico con IA hibrida:

```json
{
  "facilityId": "Sede Central - Bloque B - Aula 3B",
  "meterId": "MED-DEMO-001",
  "measuredAt": "2026-06-03T19:00:00Z",
  "kwh": 270,
  "voltage": 220,
  "powerFactor": 0.95
}
```

Resultado validado: HTTP `201`, `anomalyDetected=true`, `severity=HIGH`.

Analytics:

```http
GET /api/analytics/anomalies
Authorization: Bearer <token>
```

Resultado validado: HTTP `200`.

Explicacion dinamica observada:

```text
Se uso baseline dinamico calculado con 7 lecturas historicas. Lectura=270.00 kWh, esperado=147.71 kWh, desviacion=82.79%, zScore=1.79, severidad=HIGH.
```

### 4. Frontend

```bash
cd frontend
npm run dev -- --host 127.0.0.1
```

Resultado validado:

- `http://127.0.0.1:5173/`: HTTP `200`.
- Con backend en `8080`, `POST http://127.0.0.1:5173/api/auth/login`: HTTP `200`.

### 5. Cierre

La validacion confirma que la limpieza segura no rompio la aplicacion. La arquitectura sigue alineada al ejemplo del docente con modulos reales en `core-platform`, `iam-service` y `ai-service`, manteniendo backend, SQL Server, JWT, EnergyOps, Analytics y frontend funcionales.

## Validacion IA hibrida con datos realistas

Esta prueba prepara un historial limpio para `MED-DEMO-001` y luego genera la anomalia por API. No cambia codigo, frontend, endpoints, arquitectura ni logica IA.

### 1. Preparar SQL Server

Si la base demo aun no tiene tenant, usuario admin, edificio, medidor y baseline, ejecutar primero:

```bash
sqlcmd -S localhost,1433 -d EnergiaClaraDB -U sa -P 'Energia2026!' -i database/seeds.sql
```

Limpiar datos EnergyOps/Analytics anteriores solo para el medidor demo:

```bash
sqlcmd -S localhost,1433 -d EnergiaClaraDB -U sa -P 'Energia2026!' -i database/cleanup-energyops-ai-demo.sql
```

Insertar historial normal realista:

```bash
sqlcmd -S localhost,1433 -d EnergiaClaraDB -U sa -P 'Energia2026!' -i database/seed-energyops-ai-demo.sql
```

Lecturas insertadas:

```text
118, 122, 125, 130, 128, 134, 137, 140, 136, 142 kWh
```

### 2. Levantar backend

```bash
cd backend
mvn spring-boot:run
```

### 3. Login

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json
```

```json
{
  "email": "admin@demo.edu",
  "password": "Admin1234!",
  "tenantId": "11111111-1111-1111-1111-111111111111"
}
```

Esperado: HTTP `200` con `token`.

### 4. Generar anomalia por API

```http
POST http://localhost:8080/api/energyops/analyze-reading
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "facilityId": "Sede Central - Bloque B - Aula 3B",
  "meterId": "MED-DEMO-001",
  "measuredAt": "2026-06-03T19:00:00Z",
  "kwh": 270,
  "voltage": 220,
  "powerFactor": 0.95
}
```

Esperado:

- HTTP `201`.
- `anomalyDetected=true`.
- `severity=CRITICAL` con el historial demo propuesto, o `HIGH`/superior si el historial vigente cambia.
- Recomendacion dinamica.

### 5. Verificar Analytics

```http
GET http://localhost:8080/api/analytics/anomalies
Authorization: Bearer <token>
```

Esperado:

- HTTP `200`.
- Explicacion con baseline dinamico, historial, desviacion y Z-Score.

Ejemplo de evidencia esperada:

```text
Se uso baseline dinamico calculado con 7 lecturas historicas. Lectura=270.00 kWh, esperado=<baseline> kWh, desviacion=<porcentaje>%, zScore=<valor>, severidad=CRITICAL.
```

El detalle completo de estos scripts esta en `database/README_AI_DEMO_DATA.md`.

### 6. Evidencia validada

Validacion local ejecutada el `2026-06-03`:

- `mvn clean test`: `BUILD SUCCESS`.
- Cleanup SQL demo: ejecutado correctamente.
- Seed SQL demo: ejecutado correctamente.
- Backend `mvn spring-boot:run`: arranque correcto.
- Login: HTTP `200`.
- EnergyOps con `270 kWh`: HTTP `201`, `anomalyDetected=true`, `severity=CRITICAL`.
- Analytics: HTTP `200`, mostrando baseline dinamico, historial, desviacion y Z-Score.

Explicacion dinamica observada:

```text
Se uso baseline dinamico calculado con 7 lecturas historicas. Lectura=270.00 kWh, esperado=135.29 kWh, desviacion=99.57%, zScore=28.78, severidad=CRITICAL.
```

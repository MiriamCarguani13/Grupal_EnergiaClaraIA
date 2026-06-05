# Datos demo para IA hibrida explicable

Estos scripts preparan datos limpios y realistas para probar la IA hibrida explicable de EnergyOps sin modificar codigo, endpoints, frontend, arquitectura ni base IAM.

## Archivos

- `cleanup-energyops-ai-demo.sql`: elimina datos de prueba de EnergyOps/Analytics para el tenant demo y el medidor `MED-DEMO-001`.
- `seed-energyops-ai-demo.sql`: inserta 10 lecturas historicas normales para que la IA pueda calcular baseline dinamico, desviacion estandar y Z-Score.

## Datos que se conservan

Los scripts no borran:

- usuarios;
- roles;
- tenant/institucion;
- edificio;
- medidor `MED-DEMO-001`;
- `energiaops.snapshot_linea_base`;
- tablas IAM/security.

Tambien se conservan anomalias que tengan tickets asociados para no romper foreign keys de mantenimiento.

## Ejecutar cleanup

Desde SQL Server Management Studio, Azure Data Studio o `sqlcmd`, ejecutar:

```sql
:r database/cleanup-energyops-ai-demo.sql
```

Con `sqlcmd` desde la raiz del proyecto:

```bash
sqlcmd -S localhost,1433 -d EnergiaClaraDB -U sa -P 'Energia2026!' -i database/cleanup-energyops-ai-demo.sql
```

## Ejecutar seed

Luego ejecutar:

```sql
:r database/seed-energyops-ai-demo.sql
```

Con `sqlcmd` desde la raiz del proyecto:

```bash
sqlcmd -S localhost,1433 -d EnergiaClaraDB -U sa -P 'Energia2026!' -i database/seed-energyops-ai-demo.sql
```

El seed inserta estas lecturas normales para `MED-DEMO-001`:

```text
118, 122, 125, 130, 128, 134, 137, 140, 136, 142 kWh
```

No inserta la anomalia final. La anomalia se genera por API para probar el flujo real.

## Levantar backend

```bash
cd backend
mvn spring-boot:run
```

Base URL:

```text
http://localhost:8080
```

## Login

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json
```

Body:

```json
{
  "email": "admin@demo.edu",
  "password": "Admin1234!",
  "tenantId": "11111111-1111-1111-1111-111111111111"
}
```

Resultado esperado:

```text
HTTP 200
```

Guardar el campo `token`.

## Generar anomalia por API

```http
POST http://localhost:8080/api/energyops/analyze-reading
Authorization: Bearer <token>
Content-Type: application/json
```

Body:

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

Resultado esperado:

```json
{
  "readingId": "<uuid>",
  "anomalyId": "<uuid>",
  "anomalyDetected": true,
  "severity": "CRITICAL",
  "deviationPercent": "<valor calculado>",
  "recommendation": "<recomendacion dinamica>",
  "estimatedCostImpact": "<valor calculado>",
  "estimatedCo2Impact": "<valor calculado>"
}
```

Con el historial demo propuesto, `270 kWh` puede clasificarse como `CRITICAL` porque el consumo queda muy lejos del promedio movil y genera un Z-Score alto.

## Consultar Analytics

```http
GET http://localhost:8080/api/analytics/anomalies
Authorization: Bearer <token>
```

Resultado esperado:

```text
HTTP 200
```

La primera anomalia reciente debe mostrar una explicacion dinamica similar a:

```text
Se uso baseline dinamico calculado con 7 lecturas historicas. Lectura=270.00 kWh, esperado=<baseline> kWh, desviacion=<porcentaje>%, zScore=<valor>, severidad=CRITICAL.
```

Esa frase es la evidencia de que EnergyOps uso historial real, baseline dinamico, desviacion y Z-Score a traves de la IA hibrida explicable.

## Orden recomendado

1. Ejecutar `database/seeds.sql` si la base demo aun no tiene tenant, usuario y medidor.
2. Ejecutar `database/cleanup-energyops-ai-demo.sql`.
3. Ejecutar `database/seed-energyops-ai-demo.sql`.
4. Levantar backend.
5. Hacer login.
6. Enviar la lectura anomala de `270 kWh`.
7. Consultar Analytics.

## Validacion ejecutada

Validacion local ejecutada el `2026-06-03`:

- `mvn clean test`: `BUILD SUCCESS`.
- `cleanup-energyops-ai-demo.sql`: ejecutado correctamente con JDBC temporal.
- `seed-energyops-ai-demo.sql`: ejecutado correctamente con JDBC temporal.
- Backend `mvn spring-boot:run`: arranque correcto.
- `POST /api/auth/login`: HTTP `200`.
- `POST /api/energyops/analyze-reading` con `270 kWh`: HTTP `201`, `anomalyDetected=true`, `severity=CRITICAL`.
- `GET /api/analytics/anomalies`: HTTP `200`.

Explicacion dinamica observada:

```text
Se uso baseline dinamico calculado con 7 lecturas historicas. Lectura=270.00 kWh, esperado=135.29 kWh, desviacion=99.57%, zScore=28.78, severidad=CRITICAL.
```

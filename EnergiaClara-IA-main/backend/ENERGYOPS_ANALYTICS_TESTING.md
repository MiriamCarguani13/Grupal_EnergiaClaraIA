# EnergyOps y Analytics - Prueba demo

Endpoints a probar:

- `POST /api/energyops/analyze-reading`
- `GET /api/analytics/dashboard`
- `GET /api/analytics/kpis`
- `GET /api/analytics/anomalies`

Antes de probar, ejecutar `database/energyops-sqlserver.sql` sobre la base SQL Server usada por el backend. El seed incluye baseline activo:

- `facilityId`: `FAC-001`
- `meterId`: `M-001`
- `expectedKwh`: `100`
- `tolerancePercent`: `15`

Para SQL Server, configurar el backend por entorno segun la configuracion general del proyecto:

```bash
DB_URL='jdbc:sqlserver://localhost:1433;databaseName=energiaclara;encrypt=true;trustServerCertificate=true'
DB_USERNAME='sa'
DB_PASSWORD='tu_password'
DB_HIBERNATE_DIALECT='org.hibernate.dialect.SQLServerDialect'
```

Si la seguridad general no tiene estos endpoints en `permitAll`, usar un JWT valido en las llamadas:

```bash
-H "Authorization: Bearer <token>"
```

## Analizar lectura

```bash
curl -X POST http://localhost:8080/api/energyops/analyze-reading \
  -H "Content-Type: application/json" \
  -d '{
    "facilityId": "FAC-001",
    "meterId": "M-001",
    "measuredAt": "2026-05-12T10:00:00Z",
    "kwh": 180,
    "voltage": 220,
    "powerFactor": 0.95
  }'
```

Respuesta esperada: `anomalyDetected=true`, `severity=HIGH`, `deviationPercent=80.00`.

## Consultas

```bash
curl http://localhost:8080/api/analytics/dashboard
curl http://localhost:8080/api/analytics/kpis
curl http://localhost:8080/api/analytics/anomalies
```

# API Contracts Current

Contratos actuales del backend antes de migrar `energyops` y `analytics` a la arquitectura transversal.
Este documento congela rutas, metodos HTTP y JSON publico existente. No representa la arquitectura objetivo.

Los DTO REST descritos aqui son el contrato externo del backend. Los modelos internos de `application`
pueden evolucionar durante la migracion siempre que estos JSON publicos se mantengan sin cambios.
Los controllers y DTO REST de `energyops` y `analytics` viven ahora bajo `com.energiaclara.api.rest.*`.

## Seguridad actual

- `POST /api/auth/login`: publico.
- `POST /api/auth/register`: requiere JWT y rol `ADMIN_INSTITUCION`.
- `POST /api/energyops/analyze-reading`: temporalmente `permitAll` en `SecurityConfig`.
- `GET /api/analytics/dashboard`: temporalmente `permitAll` en `SecurityConfig`.
- `GET /api/analytics/kpis`: temporalmente `permitAll` en `SecurityConfig`.
- `GET /api/analytics/anomalies`: temporalmente `permitAll` en `SecurityConfig`.

Nota: README y SecurityConfig quedan alineados para el MVP demo: EnergyOps y Analytics son `permitAll`
temporalmente para no romper el frontend local. La proteccion JWT/RBAC queda pendiente para produccion.

## POST /api/auth/login

Autenticacion: publico.

Request body:

```json
{
  "email": "admin@demo.edu",
  "password": "Admin1234!",
  "tenantId": "11111111-1111-1111-1111-111111111111"
}
```

Response 200:

```json
{
  "token": "<jwt>",
  "userId": "44444444-4444-4444-4444-444444444444",
  "tenantId": "11111111-1111-1111-1111-111111111111",
  "roles": ["ADMIN_INSTITUCION"]
}
```

## POST /api/auth/register

Autenticacion: requiere JWT con rol `ADMIN_INSTITUCION`.

Request body:

```json
{
  "tenantId": "11111111-1111-1111-1111-111111111111",
  "email": "tecnico@demo.edu",
  "fullName": "Juan Tecnico Perez",
  "password": "Tecnico1234!",
  "roles": ["TECNICO"]
}
```

Response 200:

```json
{
  "userId": "<uuid>"
}
```

## POST /api/energyops/analyze-reading

Autenticacion: temporalmente `permitAll`.

Request body:

```json
{
  "facilityId": "Edificio Principal",
  "meterId": "Medidor Demo",
  "measuredAt": "2026-05-18T12:00:00Z",
  "kwh": 135.50,
  "voltage": 220.00,
  "powerFactor": 0.95
}
```

Response 201:

```json
{
  "readingId": "<uuid>",
  "anomalyId": "<uuid-or-null>",
  "anomalyDetected": true,
  "severity": "MEDIUM",
  "deviationPercent": 35.50,
  "recommendation": "Revisar equipos activos fuera de horario o consumo superior al baseline.",
  "estimatedCostImpact": 53.47,
  "estimatedCo2Impact": 15.62
}
```

Notas:

- `severity` puede ser `null` si no se detecta anomalia.
- `anomalyId` puede ser `null` si no se detecta anomalia.
- `severity` usa los valores actuales: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`.

## GET /api/analytics/dashboard

Autenticacion: temporalmente `permitAll`.

Request body: no aplica.

Response 200:

```json
{
  "totalReadings": 1,
  "totalAnomalies": 1,
  "latestKwh": 135.50,
  "latestDeviationPercent": 35.50,
  "latestAnomalyDetected": true,
  "kpis": [
    {
      "id": "<uuid>",
      "readingId": "<uuid>",
      "facilityId": "Edificio Principal",
      "meterId": "Medidor Demo",
      "measuredAt": "2026-05-18T12:00:00Z",
      "kwh": 135.50,
      "baselineKwh": 100.00,
      "deviationPercent": 35.50,
      "anomalyDetected": true,
      "estimatedCostImpact": 53.47,
      "estimatedCo2Impact": 15.62
    }
  ],
  "anomalies": [
    {
      "id": "<uuid>",
      "readingId": "<uuid>",
      "facilityId": "Edificio Principal",
      "meterId": "Medidor Demo",
      "measuredAt": "2026-05-18T12:00:00Z",
      "type": "EXCESS_CONSUMPTION",
      "severity": "MEDIUM",
      "deviationPercent": 35.50,
      "explanation": "La lectura supera el baseline configurado para el medidor.",
      "recommendation": "Revisar equipos activos fuera de horario o consumo superior al baseline.",
      "estimatedCostImpact": 53.47,
      "estimatedCo2Impact": 15.62
    }
  ]
}
```

## GET /api/analytics/kpis

Autenticacion: temporalmente `permitAll`.

Request body: no aplica.

Response 200:

```json
[
  {
    "id": "<uuid>",
    "readingId": "<uuid>",
    "facilityId": "Edificio Principal",
    "meterId": "Medidor Demo",
    "measuredAt": "2026-05-18T12:00:00Z",
    "kwh": 135.50,
    "baselineKwh": 100.00,
    "deviationPercent": 35.50,
    "anomalyDetected": true,
    "estimatedCostImpact": 53.47,
    "estimatedCo2Impact": 15.62
  }
]
```

## GET /api/analytics/anomalies

Autenticacion: temporalmente `permitAll`.

Request body: no aplica.

Response 200:

```json
[
  {
    "id": "<uuid>",
    "readingId": "<uuid>",
    "facilityId": "Edificio Principal",
    "meterId": "Medidor Demo",
    "measuredAt": "2026-05-18T12:00:00Z",
    "type": "EXCESS_CONSUMPTION",
    "severity": "MEDIUM",
    "deviationPercent": 35.50,
    "explanation": "La lectura supera el baseline configurado para el medidor.",
    "recommendation": "Revisar equipos activos fuera de horario o consumo superior al baseline.",
    "estimatedCostImpact": 53.47,
    "estimatedCo2Impact": 15.62
  }
]
```

# Payloads demo para IA hibrida EnergyOps/Analytics

Usar estos payloads despues de ejecutar `database/reset-demo-energyops-analytics.sql`, iniciar backend/frontend y hacer login.

Endpoint:

```http
POST /api/energyops/analyze-reading
Authorization: Bearer <TOKEN_ADMIN>
Content-Type: application/json
```

Tenant demo usado por login:

```json
{
  "tenantId": "11111111-1111-1111-1111-111111111111",
  "email": "admin@demo.edu",
  "password": "Admin1234!"
}
```

Notas:

- Enviar las lecturas en orden.
- No insertar estas lecturas directamente por SQL: el endpoint crea la lectura, ejecuta la IA hibrida, genera explicacion/recomendacion y persiste anomalias reales si corresponde.
- Los timestamps son distintos para respetar `uq_lectura_periodo`.
- Medidor: `MED-DEMO-001`.
- Area: `Sede Central - Bloque B - Aula 3B`.
- Voltaje: `220`.
- Factor de potencia: `0.95`.

## 1. Normal - 96 kWh

```json
{
  "facilityId": "Sede Central - Bloque B - Aula 3B",
  "meterId": "MED-DEMO-001",
  "measuredAt": "2026-06-19T08:00:00Z",
  "kwh": 96,
  "voltage": 220,
  "powerFactor": 0.95
}
```

## 2. Normal - 101 kWh

```json
{
  "facilityId": "Sede Central - Bloque B - Aula 3B",
  "meterId": "MED-DEMO-001",
  "measuredAt": "2026-06-19T09:00:00Z",
  "kwh": 101,
  "voltage": 220,
  "powerFactor": 0.95
}
```

## 3. Normal - 98 kWh

```json
{
  "facilityId": "Sede Central - Bloque B - Aula 3B",
  "meterId": "MED-DEMO-001",
  "measuredAt": "2026-06-19T10:00:00Z",
  "kwh": 98,
  "voltage": 220,
  "powerFactor": 0.95
}
```

## 4. Normal - 104 kWh

```json
{
  "facilityId": "Sede Central - Bloque B - Aula 3B",
  "meterId": "MED-DEMO-001",
  "measuredAt": "2026-06-19T11:00:00Z",
  "kwh": 104,
  "voltage": 220,
  "powerFactor": 0.95
}
```

## 5. Normal - 99 kWh

```json
{
  "facilityId": "Sede Central - Bloque B - Aula 3B",
  "meterId": "MED-DEMO-001",
  "measuredAt": "2026-06-19T12:00:00Z",
  "kwh": 99,
  "voltage": 220,
  "powerFactor": 0.95
}
```

## 6. Normal - 102 kWh

```json
{
  "facilityId": "Sede Central - Bloque B - Aula 3B",
  "meterId": "MED-DEMO-001",
  "measuredAt": "2026-06-19T13:00:00Z",
  "kwh": 102,
  "voltage": 220,
  "powerFactor": 0.95
}
```

## 7. Sospechosa - 125 kWh

```json
{
  "facilityId": "Sede Central - Bloque B - Aula 3B",
  "meterId": "MED-DEMO-001",
  "measuredAt": "2026-06-19T14:00:00Z",
  "kwh": 125,
  "voltage": 220,
  "powerFactor": 0.95
}
```

## 8. Anomala - 150 kWh

```json
{
  "facilityId": "Sede Central - Bloque B - Aula 3B",
  "meterId": "MED-DEMO-001",
  "measuredAt": "2026-06-19T15:00:00Z",
  "kwh": 150,
  "voltage": 220,
  "powerFactor": 0.95
}
```

## 9. Anomala - 165 kWh

```json
{
  "facilityId": "Sede Central - Bloque B - Aula 3B",
  "meterId": "MED-DEMO-001",
  "measuredAt": "2026-06-19T16:00:00Z",
  "kwh": 165,
  "voltage": 220,
  "powerFactor": 0.95
}
```

## 10. Anomala - 180 kWh

```json
{
  "facilityId": "Sede Central - Bloque B - Aula 3B",
  "meterId": "MED-DEMO-001",
  "measuredAt": "2026-06-19T17:00:00Z",
  "kwh": 180,
  "voltage": 220,
  "powerFactor": 0.95
}
```

## Verificacion esperada

| # | kWh | Resultado esperado |
|---|-----|--------------------|
| 1 | 96 | Lectura normal |
| 2 | 101 | Lectura normal |
| 3 | 98 | Lectura normal |
| 4 | 104 | Lectura normal |
| 5 | 99 | Lectura normal; desde aqui hay historial minimo para baseline dinamico |
| 6 | 102 | Lectura normal con historial disponible |
| 7 | 125 | Lectura sospechosa; puede o no generar anomalia segun baseline dinamico y Z-Score |
| 8 | 150 | Debe generar anomalia |
| 9 | 165 | Debe generar anomalia |
| 10 | 180 | Debe generar anomalia alta o critica |

La IA hibrida necesita historial suficiente. Las primeras lecturas construyen ese historial; las lecturas altas deben mostrar desviacion, Z-Score, confidence y explicacion dinamica si el historial ya esta disponible.

## Orden de prueba final

1. Ejecutar `database/reset-demo-energyops-analytics.sql` en SQL Server.
2. Levantar backend.
3. Levantar frontend.
4. Hacer login como ADMIN.
5. Registrar/enviar las 10 lecturas por endpoint o por pantalla.
6. Ver Dashboard.
7. Ver Anomalias.
8. Entrar al detalle IA de una anomalia.
9. Derivar anomalia a mantenimiento.
10. Hacer login como tecnico.
11. Resolver ticket.
12. Verificar que la anomalia queda resuelta.

## Endpoints de verificacion

Revisar:

```http
GET /api/analytics/dashboard
GET /api/analytics/anomalies
```

4. En frontend:

- Dashboard: KPIs, historial, panel analitico y ultimas anomalias.
- Anomalias: cards con severidad, baseline, desviacion y confidence.
- Detalle de anomalia: seccion "Explicabilidad IA".

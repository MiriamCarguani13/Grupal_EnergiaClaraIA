# Guia de prueba final - EnergiaClara AI

Esta guia valida el flujo completo desde login hasta generacion de anomalias, visualizacion en Dashboard/Anomalias y derivacion a mantenimiento.

## 1. Evidencia revisada en el proyecto

- Usuario demo ADMIN: existe en `database/seeds.sql`.
- Credenciales ADMIN:

```json
{
  "tenantId": "11111111-1111-1111-1111-111111111111",
  "email": "admin@demo.edu",
  "password": "Admin1234!"
}
```

- Rol ADMIN: `ADMIN_INSTITUCION`.
- Endpoint de analisis energetico: `POST /api/energyops/analyze-reading`.
- Controller real: `backend/src/main/java/com/energiaclara/api/rest/energyops/EnergyOpsController.java`.
- Seguridad: `/api/energyops/analyze-reading` requiere rol `ADMIN_INSTITUCION`, `DIRECTOR` o `AUDITOR`.
- Respuesta esperada del endpoint: HTTP `201 Created`.

## 2. Preparacion de datos

Ejecutar primero:

```sql
:r database/reset-demo-energyops-analytics.sql
```

Ese script:

- Limpia datos operativos demo de `MED-DEMO-001`.
- Conserva usuarios, roles, tenant, medidores y seguridad.
- Asegura baseline activo aproximado de `100 kWh` con tolerancia `15%`.
- No inserta anomalias falsas.

Las lecturas deben pasar por el endpoint para que se ejecute la IA hibrida real.

## 3. Levantar el sistema

Backend:

```bash
mvn -pl backend spring-boot:run
```

Frontend:

```bash
cd frontend
npm run dev
```

Abrir:

```text
http://localhost:5173
```

## 4. Login

Endpoint:

```http
POST http://localhost:8080/api/auth/login
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

Guardar el token devuelto y usarlo como:

```http
Authorization: Bearer <TOKEN_ADMIN>
```

## 5. Enviar lecturas EnergyOps

Endpoint:

```http
POST http://localhost:8080/api/energyops/analyze-reading
Authorization: Bearer <TOKEN_ADMIN>
Content-Type: application/json
```

Enviar en orden para que la IA construya historial.

### 1. Normal - 96 kWh

Esperado: normal.

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

### 2. Normal - 101 kWh

Esperado: normal.

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

### 3. Normal - 98 kWh

Esperado: normal.

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

### 4. Normal - 104 kWh

Esperado: normal.

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

### 5. Normal - 99 kWh

Esperado: normal. A partir de aqui ya existe historial minimo para IA hibrida dinamica.

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

### 6. Normal - 102 kWh

Esperado: normal con baseline dinamico.

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

### 7. Sospechosa - 125 kWh

Esperado: puede generar anomalia. Aunque la desviacion frente a 100 kWh es moderada, con historial estable el Z-Score puede elevar la severidad.

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

### 8. Anomala - 150 kWh

Esperado: anomalia.

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

### 9. Anomala - 165 kWh

Esperado: anomalia.

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

### 10. Anomala - 180 kWh

Esperado: anomalia de severidad alta o critica.

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

## 6. Resultado esperado por consumo

| # | kWh | Resultado esperado |
|---|-----|--------------------|
| 1 | 96  | Normal |
| 2 | 101 | Normal |
| 3 | 98  | Normal |
| 4 | 104 | Normal |
| 5 | 99  | Normal |
| 6 | 102 | Normal con baseline dinamico |
| 7 | 125 | Sospechosa; puede generar anomalia por Z-Score |
| 8 | 150 | Anomalia |
| 9 | 165 | Anomalia |
| 10 | 180 | Anomalia alta/critica |

La IA hibrida usa minimo 5 lecturas historicas. Antes de eso, el sistema usa fallback deterministico con baseline fijo.

## 7. Verificaciones API

Dashboard:

```http
GET http://localhost:8080/api/analytics/dashboard
Authorization: Bearer <TOKEN_ADMIN>
```

Anomalias:

```http
GET http://localhost:8080/api/analytics/anomalies
Authorization: Bearer <TOKEN_ADMIN>
```

Tickets:

```http
GET http://localhost:8080/api/maintenance/tickets
Authorization: Bearer <TOKEN_ADMIN>
```

## 8. Verificaciones frontend

Dashboard:

- KPIs principales actualizados.
- Historial de consumo vs baseline visible cuando existan lecturas suficientes.
- Panel Analitico con severidad, medidores criticos, impacto y ahorro.
- Ultimas anomalias visibles.
- Actividad reciente con lecturas, anomalias y tickets.

Anomalias:

- Las anomalias aparecen como cards.
- Cada card muestra medidor, severidad, estado, consumo, baseline, desviacion y confidence si existe.
- Boton `Derivar a mantenimiento` aparece solo si el estado es `DETECTADA`.

Detalle de anomalia:

- Seccion `Explicabilidad IA`.
- Debe mostrar baseline esperado, Z-Score, confidence, muestras historicas, tipo de baseline, modelo y explicacion.
- La recomendacion debe estar visible.

Mantenimiento:

- Al crear un ticket desde una anomalia, el backend debe marcar la anomalia como `DERIVADA` si la operacion se completa.
- Si un tecnico actualiza el ticket a `REPARADO` o `CERRADO`, la anomalia asociada debe pasar a `RESUELTA` si el backend lo soporta.

## 9. Checklist de defensa

- [ ] Login ADMIN funciona.
- [ ] Dashboard carga sin error.
- [ ] Registrar lectura normal: `96 kWh`.
- [ ] Registrar varias lecturas normales para formar historial.
- [ ] Registrar lectura anomalamente alta: `150`, `165` o `180 kWh`.
- [ ] Ver respuesta con `anomalyDetected=true`.
- [ ] Abrir listado de Anomalias.
- [ ] Abrir detalle de anomalia.
- [ ] Mostrar explicacion IA: baseline dinamico, Z-Score, confidence, muestras historicas y recomendacion.
- [ ] Volver al Dashboard y mostrar KPIs/Panel Analitico.
- [ ] Derivar anomalia a mantenimiento.
- [ ] Ver ticket creado.
- [ ] Login TECNICO funciona.
- [ ] Tecnico ve ticket.
- [ ] Tecnico llena ficha tecnica.
- [ ] Tecnico marca ticket como reparado/cerrado.
- [ ] Anomalia pasa a `RESUELTA`.

## 10. Validaciones de build

Backend:

```bash
mvn clean install
```

Frontend:

```bash
cd frontend
npm run build
```

## 11. Notas para defensa

- La IA hibrida no usa LLM.
- La parte estadistica calcula baseline dinamico, desviacion estandar, Z-Score, anomaly score y confidence.
- La parte de reglas define severidad, impacto, costo, CO2 y recomendacion.
- La explicabilidad se genera con valores calculados, no con texto fijo sin contexto.
- La resiliencia se mantiene con fallback deterministico si no hay historial suficiente.

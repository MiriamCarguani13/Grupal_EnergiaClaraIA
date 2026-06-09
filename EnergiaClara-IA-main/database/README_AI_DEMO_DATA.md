# Datos demo para la IA híbrida explicable (EnergyOps)

Prepara historial limpio y realista para probar el motor AI híbrido (`HybridEnergyAiEngine`):
baseline dinámico (promedio móvil), desviación estándar, Z-Score y recomendación explicable.
No modifica arquitectura, IAM ni schema canónico.

> **Nota:** `database/seeds.sql` (sección 5) **ya inserta** estas 10 lecturas demo en una sola corrida.
> Los scripts de abajo sirven para **re-sembrar / limpiar** el historial sin volver a correr todo `seeds.sql`.

## Archivos

EnergyOps (IA híbrida):
- `cleanup-energyops-ai-demo.sql` — borra lecturas/anomalías demo de EnergyOps+Analytics del tenant demo y medidor `MED-DEMO-001`. Conserva IAM, tenant, edificio, medidor, baseline y anomalías con ticket asociado (FKs de mantenimiento).
- `seed-energyops-ai-demo.sql` — inserta 10 lecturas históricas normales para `MED-DEMO-001`.

Mantenimiento (acceso TÉCNICO + ficha técnica):
- `seed-technician-maintenance-demo.sql` — agrega columnas de ficha técnica a `mantenimiento.ticket`, amplía los CHECK de estado, y siembra técnico demo + SLA + 1 ticket.
- `cleanup-maintenance-demo.sql` — borra solo los tickets demo (conserva lecturas, anomalías, baselines, KPIs e historial IA).

> `database/seeds.sql` ya incluye **ambas** demos (sección 5 = historial IA, sección 6 = mantenimiento) en una sola corrida.

## IDs demo (fijos, definidos en `seeds.sql`)

| Recurso   | UUID                                   |
|-----------|----------------------------------------|
| tenant    | `11111111-1111-1111-1111-111111111111` |
| edificio  | `22222222-2222-2222-2222-222222222222` |
| **medidor** | `33333333-3333-3333-3333-333333333333` |
| admin     | `44444444-4444-4444-4444-444444444444` |
| técnico   | `55555555-5555-5555-5555-555555555555` (`tecnico@demo.edu`) |
| SLA demo  | `66666666-6666-6666-6666-666666666666` |
| ticket demo | `77777777-7777-7777-7777-777777777777` |

Historial sembrado (`MED-DEMO-001`): `118, 122, 125, 130, 128, 134, 137, 140, 136, 142` kWh.

## Orden de ejecución (sqlcmd)

```bash
# 1. Schema + seeds base (incluye ya el historial demo de la sección 5)
sqlcmd -S localhost,1433 -d master         -U sa -P 'Energia2026!' -i database/script.sql
sqlcmd -S localhost,1433 -d EnergiaClaraDB  -U sa -P 'Energia2026!' -i database/seeds.sql

# 2. (Opcional) re-sembrar solo el historial AI
sqlcmd -S localhost,1433 -d EnergiaClaraDB  -U sa -P 'Energia2026!' -i database/cleanup-energyops-ai-demo.sql
sqlcmd -S localhost,1433 -d EnergiaClaraDB  -U sa -P 'Energia2026!' -i database/seed-energyops-ai-demo.sql

# 3. (Opcional) re-sembrar solo la demo de mantenimiento
sqlcmd -S localhost,1433 -d EnergiaClaraDB  -U sa -P 'Energia2026!' -i database/cleanup-maintenance-demo.sql
sqlcmd -S localhost,1433 -d EnergiaClaraDB  -U sa -P 'Energia2026!' -i database/seed-technician-maintenance-demo.sql
```

## Levantar backend

```bash
mvn -pl energiaclara-infrastructure -am spring-boot:run
# Base URL: http://localhost:8080
```

## Probar el flujo (genera la anomalía por API)

`POST /api/energyops/analyze-reading` (requiere `Authorization: Bearer <token>` de `/api/auth/login`).

**Importante:** `meterId` es el **UUID** del medidor, no el código `MED-DEMO-001`.

```json
{
  "facilityId": "22222222-2222-2222-2222-222222222222",
  "meterId":    "33333333-3333-3333-3333-333333333333",
  "kwhValue":   270,
  "timestamp":  "2026-06-03T19:00:00Z"
}
```

Respuesta esperada (`EnergyAiAnalysisResponse`) — con el historial demo, 270 kWh cae lejos
del promedio móvil → Z-Score alto → `CRITICAL`:

```json
{
  "expectedKwh":      "135.29",
  "baselineSource":   "HISTORY",
  "sampleCount":      7,
  "deviationPercent": "99.57",
  "zScore":           "28.78",
  "severity":         "CRITICAL",
  "anomalyDetected":  true,
  "fallback":         false,
  "explanation":      "Se uso baseline dinamico calculado con 7 lecturas historicas. Lectura=270.00 kWh, esperado=135.29 kWh, desviacion=99.57%, zScore=28.78, severidad=CRITICAL.",
  "recommendation":   "Escalar a mantenimiento y auditoria energetica para revision prioritaria.",
  "modelVersion":     "hybrid-stat-rules-v1.0"
}
```

La frase de `explanation` con baseline dinámico, desviación y Z-Score es la evidencia de que
EnergyOps consumió historial real vía la IA híbrida explicable.

# Defensa de IA hibrida explicable integrada

Fecha de evidencia: 2026-06-02

## Tesis de defensa

EnergiaClara AI implementa una IA hibrida explicable para el analisis energetico porque combina estadistica historica, reglas de negocio del dominio energetico y explicaciones trazables sin usar LLMs ni modelos de caja negra.

La IA esta integrada en EnergyOps mediante arquitectura hexagonal. El endpoint externo se mantiene igual y Analytics muestra la explicacion dinamica generada por el motor.

## Flujo tecnico completo

```text
POST /api/energyops/analyze-reading
  -> EnergyOpsController
  -> EnergyAnalysisService
  -> EnergyAiAnalysisPort
  -> HybridEnergyAiAnalysisAdapter
      -> EnergyReadingRepository
      -> AnalyzeEnergyWithAiUseCase
      -> AnalyzeEnergyWithAiService
      -> DefaultEnergyAiEngineAdapter
      -> HybridEnergyAiEngine
          -> baseline dinamico
          -> desviacion estandar
          -> Z-Score
          -> score de anomalia
          -> severidad sugerida
          -> explicacion dinamica
          -> recomendacion dinamica
  -> EnergyAnalysisService
  -> persistencia de lectura
  -> persistencia de anomalia si aplica
  -> GET /api/analytics/anomalies
```

Este flujo demuestra inversion de dependencias:

- EnergyOps no conoce detalles internos del motor.
- EnergyOps depende de `EnergyAiAnalysisPort`.
- El adapter traduce entre backend real y `ai-service`.
- `ai-domain` no depende de Spring, JPA, SQL Server ni backend.

## Que parte es IA

La parte de IA es estadistica, deterministica y explicable:

- Promedio movil: calcula un baseline dinamico con lecturas historicas recientes.
- Desviacion estandar: mide dispersion del historial.
- Z-Score: mide cuan lejos esta la lectura nueva frente al patron historico.
- Anomaly score: resume desviacion porcentual y distancia estadistica.
- Confidence: expresa mayor confianza cuando existe historial suficiente y menor confianza cuando se activa fallback.
- Explicacion dinamica: incluye valores calculados como lectura, consumo esperado, desviacion, Z-Score y severidad.

No se usa LLM. No se usa entrenamiento pesado. No hay predicciones opacas.

## Que parte son reglas de negocio

Las reglas de negocio convierten los calculos estadisticos en decisiones operativas:

- Severidad: `NORMAL`, `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`.
- Impacto energetico: exceso o desviacion respecto al consumo esperado.
- Costo estimado: exceso energetico multiplicado por costo por kWh.
- CO2 estimado: exceso energetico multiplicado por factor kg CO2/kWh.
- Recomendacion: accion sugerida segun severidad y desviacion.
- Fallback deterministico: mantiene la logica anterior cuando la IA no puede calcular con seguridad.

## Explicabilidad sin LLM

La explicacion se genera con plantillas dinamicas y datos calculados:

```text
Se uso baseline dinamico calculado con 7 lecturas historicas.
Lectura=260.00 kWh, esperado=138.14 kWh, desviacion=88.21%,
zScore=2.44, severidad=HIGH.
```

Esto es defendible academicamente porque cada conclusion puede rastrearse:

- El baseline esperado viene del promedio movil.
- La desviacion viene de comparar lectura actual contra baseline dinamico.
- El Z-Score viene de la desviacion estandar historica.
- La severidad viene de reglas explicitas.
- La recomendacion viene de reglas del dominio energetico.

## Resiliencia

El sistema mantiene continuidad operativa:

- Si no hay historial suficiente, el dominio IA marca `fallback=true`.
- Si el adapter IA falla, EnergyOps captura la excepcion.
- Si se activa fallback, EnergyOps conserva el calculo deterministico anterior.
- No se rompe el endpoint actual.
- No se rompe el frontend.
- No se cambia la base de datos.
- No se altera login/JWT.

## Contrato externo conservado

`POST /api/energyops/analyze-reading` mantiene:

- misma ruta
- mismo request JSON
- mismo response JSON

No se exponen todavia `aiUsed`, `confidence` ni `modelVersion` en el response publico para no romper consumidores existentes. La explicacion dinamica se observa en Analytics cuando se persiste una anomalia.

## Evidencia HTTP final

### Login

Request:

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "tenantId": "11111111-1111-1111-1111-111111111111",
  "email": "admin@demo.edu",
  "password": "Admin1234!"
}
```

Resultado validado: HTTP `200`, respuesta con `token`, `userId`, `tenantId` y `roles`.

### Lectura normal

Request:

```http
POST /api/energyops/analyze-reading
Authorization: Bearer <token>
Content-Type: application/json
```

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

Respuesta validada:

```json
{
  "anomalyDetected": false,
  "severity": null,
  "deviationPercent": -32.41,
  "recommendation": "Consumo dentro del patron esperado; continuar monitoreo regular.",
  "estimatedCostImpact": 0.00,
  "estimatedCo2Impact": 0.00
}
```

### Lectura anomalica

Request:

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

Respuesta validada:

```json
{
  "anomalyDetected": true,
  "severity": "HIGH",
  "deviationPercent": 88.21,
  "recommendation": "Inspeccionar equipos de alto consumo, climatizacion e iluminacion del area.",
  "estimatedCostImpact": 183.55,
  "estimatedCo2Impact": 53.62
}
```

### Analytics con explicacion dinamica

Request:

```http
GET /api/analytics/anomalies
Authorization: Bearer <token>
```

Respuesta validada:

```json
{
  "severity": "HIGH",
  "deviationPercent": 88.2100,
  "explanation": "Se uso baseline dinamico calculado con 7 lecturas historicas. Lectura=260.00 kWh, esperado=138.14 kWh, desviacion=88.21%, zScore=2.44, severidad=HIGH.",
  "recommendation": "Inspeccionar equipos de alto consumo, climatizacion e iluminacion del area.",
  "estimatedCostImpact": 183.55,
  "estimatedCo2Impact": 53.62
}
```

## Validacion tecnica

Comando:

```bash
mvn clean test
```

Resultado:

```text
BUILD SUCCESS
```

Smoke test HTTP:

- `POST /api/auth/login`: `200`
- `POST /api/energyops/analyze-reading` normal: `201`
- `POST /api/energyops/analyze-reading` anomalica: `201`
- `GET /api/analytics/anomalies`: `200`

## Como defenderlo ante el jurado

La solucion no afirma tener un modelo complejo. La defensa correcta es:

> Es una IA hibrida explicable para MVP universitario: aprende un patron energetico simple desde historial reciente mediante estadistica descriptiva, aplica reglas de negocio energeticas para clasificar la anomalia y genera una explicacion trazable con los valores usados en el calculo.

Puntos fuertes:

- Tiene separacion enterprise real en `ai-domain`, `ai-application` y `ai-infrastructure`.
- EnergyOps consume IA por puerto/adaptador.
- Analytics evidencia la explicacion dinamica.
- La decision es reproducible.
- El sistema es resiliente por fallback.
- No rompe contratos existentes.

## Evidencia final post-cleanup

Fecha de validacion: `2026-06-02`.

Despues de la limpieza segura del proyecto, la IA hibrida explicable fue validada nuevamente sin modificar codigo funcional.

### Build y runtime

```bash
mvn clean test
mvn install -DskipTests
cd backend
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=0
```

Resultado:

- Maven: `BUILD SUCCESS`.
- Spring Boot: arranque correcto.
- SQL Server: conexion correcta.
- Frontend Vite: HTTP `200`.

### Evidencia funcional

Login:

- `POST /api/auth/login`: HTTP `200`.

EnergyOps normal:

- `POST /api/energyops/analyze-reading`: HTTP `201`.
- Resultado: `anomalyDetected=false`.
- Recomendacion: `Consumo dentro del patron esperado; continuar monitoreo regular.`

EnergyOps anomalico:

- `POST /api/energyops/analyze-reading`: HTTP `201`.
- Resultado: `anomalyDetected=true`.
- Severidad: `HIGH`.
- Desviacion: `82.79%`.
- Recomendacion: `Inspeccionar equipos de alto consumo, climatizacion e iluminacion del area.`

Analytics:

- `GET /api/analytics/anomalies`: HTTP `200`.
- Explicacion dinamica observada:

```text
Se uso baseline dinamico calculado con 7 lecturas historicas. Lectura=270.00 kWh, esperado=147.71 kWh, desviacion=82.79%, zScore=1.79, severidad=HIGH.
```

### Defensa academica final

La evidencia muestra que la IA no es una etiqueta cosmetica: EnergyOps usa `ai-service` mediante arquitectura hexagonal, calcula baseline dinamico con historial, mide desviacion y `zScore`, aplica reglas de negocio para severidad e impacto, y persiste una explicacion trazable que luego Analytics expone.

La solucion sigue siendo adecuada para MVP porque no usa LLMs ni modelos opacos, no cambia contratos externos, mantiene fallback deterministico y permite explicar cada decision con valores calculados.

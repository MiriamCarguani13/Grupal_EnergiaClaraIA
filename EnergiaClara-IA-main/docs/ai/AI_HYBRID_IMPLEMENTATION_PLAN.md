# Plan de implementacion IA hibrida explicable

Este documento propone una IA hibrida explicable para EnergiaClara AI sin romper la aplicacion actual.

Restricciones:

- No cambiar endpoints actuales.
- No cambiar JSON externo todavia.
- No cambiar frontend.
- No cambiar base de datos salvo que sea indispensable.
- No mover IAM.
- No convertir `ai-service` en microservicio real.
- Reutilizar la logica actual de baseline, anomalias, severidad, costo, CO2 y recomendaciones.

## 1. Que significa IA hibrida en este proyecto

En EnergiaClara AI, IA hibrida significa combinar:

- Estadistica sobre historial de lecturas energeticas.
- Reglas de negocio deterministicas.
- Explicaciones generadas con plantillas basadas en datos calculados.

No significa usar LLMs ni modelos complejos. Para el MVP universitario, la "IA" es un motor explicable de inferencia energetica que aprende del historial reciente y aplica reglas claras.

La arquitectura propuesta:

```text
EnergyOps actual
  -> puerto de IA
  -> ai-application
  -> ai-domain
  -> puerto de historial
  -> adapter de lecturas existentes
  -> SQL Server actual
```

## 2. Parte estadistica

La parte estadistica calcula un contexto dinamico para cada nueva lectura:

- Historial reciente del medidor.
- Promedio movil.
- Desviacion estandar.
- Z-Score.
- Consumo esperado.
- Desviacion porcentual frente al consumo esperado.

Ejemplo conceptual:

```text
lectura nueva = 165 kWh
historial reciente = [98, 101, 95, 104, 100, 97, 103]
promedio movil = 100.0
desviacion estandar = 2.6
z-score = (165 - 100) / 2.6 = 25.0
```

## 3. Parte de reglas de negocio

La parte de reglas conserva y mejora lo que ya existe:

- Severidad `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`.
- Impacto energetico en kWh excedidos.
- Costo estimado.
- CO2 estimado.
- Recomendacion.
- Estado de anomalia.

La diferencia es que las reglas ya no dependerian solo de un baseline fijo, sino de un resultado estadistico dinamico cuando exista historial suficiente.

## 4. Algoritmos a usar

### Promedio movil

Promedio de las ultimas `N` lecturas del mismo medidor.

Formula:

```text
movingAverage = sum(kwh historicos) / cantidad
```

Parametro sugerido:

- `N = 7` para MVP.
- Si hay mas datos, permitir `N = 14` o `N = 30`.

### Desviacion estandar

Mide dispersion del consumo historico.

Formula:

```text
stdDev = sqrt(sum((kwh_i - promedio)^2) / n)
```

Para MVP se recomienda poblacional (`/ n`) por simplicidad.

### Z-Score

Mide cuantas desviaciones estandar se aleja la lectura actual del promedio historico.

Formula:

```text
zScore = (kwhActual - movingAverage) / stdDev
```

Fallback si `stdDev = 0`:

- Si `kwhActual <= movingAverage`, `zScore = 0`.
- Si `kwhActual > movingAverage`, usar desviacion porcentual como criterio principal.

### Consumo esperado

Prioridad propuesta:

1. Usar promedio movil si hay historial suficiente.
2. Usar baseline activo de BD si no hay suficiente historial.
3. Usar fallback demo actual si no hay baseline.

### Reglas de severidad

Combinar desviacion porcentual y Z-Score:

```text
CRITICAL: deviationPercent >= 100 o zScore >= 4.0
HIGH:     deviationPercent >= 50  o zScore >= 3.0
MEDIUM:   deviationPercent >= 25  o zScore >= 2.0
LOW:      deviationPercent > tolerancia o zScore >= 1.5
```

Para no romper comportamiento actual, conservar la tolerancia del baseline como umbral minimo de anomalia:

```text
anomalyDetected = deviationPercent > tolerancePercent || zScore >= 2.0
```

## 5. Clases a crear en ai-domain

Ubicacion propuesta:

```text
ai-service/modules/ai-domain/src/main/java/com/energiaclara/ai/domain/
```

Clases:

- `EnergyReadingSample`
  - Representa una lectura historica simple: medidor, fecha, kWh.
  - Sin JPA, sin Spring.

- `HistoricalEnergyWindow`
  - Contiene lista de lecturas historicas.
  - Valida si hay muestras suficientes.

- `DynamicBaseline`
  - `expectedKwh`
  - `movingAverageKwh`
  - `standardDeviationKwh`
  - `sampleCount`
  - `source`: `HISTORY`, `STATIC_BASELINE`, `DEMO_FALLBACK`

- `EnergyAnomalyScore`
  - `deviationPercent`
  - `zScore`
  - `excessKwh`
  - `anomalyDetected`
  - `severity`

- `HybridAiAnalysis`
  - Resultado interno completo:
    - baseline dinamico
    - score
    - explicacion
    - recomendacion
    - impactos

- `HybridEnergyAnalysisPolicy`
  - Politica pura:
    - promedio movil
    - desviacion estandar
    - z-score
    - severidad
    - impacto

- `ExplanationTemplatePolicy`
  - Genera explicaciones dinamicas sin LLM.

- `RecommendationPolicy`
  - Genera recomendaciones dinamicas segun severidad, z-score, desviacion y tipo de patron.

Enums:

- `BaselineSource`
  - `HISTORY`
  - `STATIC_BASELINE`
  - `DEMO_FALLBACK`

- `ConsumptionPattern`
  - `NORMAL`
  - `MODERATE_DEVIATION`
  - `HIGH_DEVIATION`
  - `CRITICAL_DEVIATION`
  - `INSUFFICIENT_HISTORY`

## 6. Clases a crear en ai-application

Ubicacion propuesta:

```text
ai-service/modules/ai-application/src/main/java/com/energiaclara/ai/application/
```

Clases:

- `AnalyzeHybridEnergyUseCase`
  - Puerto de entrada para que EnergyOps invoque la IA.

- `HybridEnergyAnalysisService`
  - Orquesta:
    - obtener historial
    - resolver baseline dinamico
    - calcular score
    - calcular impacto
    - generar explicacion
    - generar recomendacion

- `HybridEnergyAnalysisCommand`
  - Datos de entrada:
    - tenantId
    - medidorId
    - facilityId
    - meterId
    - measuredAt
    - kwh
    - staticBaselineKwh
    - tolerancePercent
    - costPerKwh
    - co2KgPerKwh

- `HybridEnergyAnalysisResult`
  - Resultado interno:
    - expectedKwh
    - baselineSource
    - sampleCount
    - movingAverageKwh
    - standardDeviationKwh
    - zScore
    - deviationPercent
    - excessKwh
    - anomalyDetected
    - severity
    - explanation
    - recommendation
    - estimatedCostImpact
    - estimatedCo2Impact

## 7. Puertos para integrar EnergyOps con IA

### Puerto de entrada IA

```text
AnalyzeHybridEnergyUseCase
```

Responsabilidad:

- Recibir una lectura y contexto de baseline.
- Devolver un resultado de analisis hibrido.

EnergyOps llamaria este puerto desde `EnergyAnalysisService`.

### Puerto de salida para historial

```text
LoadEnergyReadingHistoryPort
```

Responsabilidad:

- Cargar las ultimas `N` lecturas de un medidor.
- Filtrar por tenant y medidor.
- Excluir la lectura actual si ya fue persistida.

Firma conceptual:

```text
List<EnergyReadingSample> loadRecentReadings(UUID tenantId, UUID medidorId, int limit)
```

### Puerto opcional de configuracion

```text
LoadHybridAiSettingsPort
```

No es necesario para la primera fase. Puede omitirse y usar properties.

## 8. Adapters a crear

### Adapter de historial desde EnergyOps

Ubicacion posible:

```text
ai-service/modules/ai-infrastructure
```

O, si aun no se integra `ai-infrastructure` al Maven:

```text
backend/src/main/java/com/energiaclara/infrastructure/persistence/adapter/
```

Nombre:

- `EnergyReadingHistoryAdapter`

Implementa:

- `LoadEnergyReadingHistoryPort`

Usa:

- `EnergyReadingRepository`
- `EnergyReadingEntity`

Consulta sugerida:

```text
findTopNByTenantIdAndMedidorIdOrderByMeasuredAtDesc
```

Si se evita cambiar repositorio en fase 1, se puede reutilizar `findTop20ByOrderByMeasuredAtDesc()` y filtrar en memoria, aunque no es ideal.

## 9. Como se obtendra el historial de lecturas

La fuente sera la tabla existente:

```text
consumo.lectura
```

No se requiere nueva tabla para MVP.

Flujo:

1. EnergyOps recibe lectura nueva.
2. Antes o despues de persistirla, solicita historial reciente.
3. El adapter consulta lecturas previas del medidor demo.
4. La IA calcula baseline dinamico.
5. EnergyOps persiste anomalia si corresponde.

Recomendacion tecnica:

- Obtener historial antes de guardar la lectura actual para no contaminar el promedio con el dato que se esta evaluando.
- Si por simplicidad se guarda primero, el adapter debe excluir `readingId` actual.

## 10. Fallback deterministico sin historial suficiente

Minimo sugerido:

```text
minimumSamples = 5
```

Reglas:

1. Si hay `>= 5` lecturas historicas:
   - usar promedio movil y desviacion estandar.
2. Si hay `< 5` lecturas pero existe baseline activo:
   - usar baseline de BD.
   - marcar `baselineSource = STATIC_BASELINE`.
3. Si no hay baseline activo:
   - usar fallback demo actual:
     - `app.energyops.demo-default-baseline-kwh`
     - `app.energyops.demo-default-tolerance-percent`
   - marcar `baselineSource = DEMO_FALLBACK`.

Esto mantiene comportamiento deterministico y evita que la app falle por falta de datos.

## 11. Explicaciones dinamicas sin LLM

La explicacion debe generarse con plantillas y valores calculados.

Ejemplos:

### Historial suficiente

```text
La lectura de 165.00 kWh supera el consumo esperado de 100.00 kWh calculado con 7 lecturas historicas.
La desviacion es 65.00% y el Z-Score es 3.20, lo que indica un consumo atipico frente al patron reciente del medidor.
```

### Sin historial suficiente

```text
No hay historial suficiente para calcular un baseline dinamico. Se uso el baseline activo de 100.00 kWh.
La lectura supera el umbral de tolerancia configurado de 15.00%.
```

### Sin anomalia

```text
La lectura esta dentro del rango esperado. La desviacion frente al consumo esperado es 8.00%, por debajo de la tolerancia de 15.00%.
```

La explicacion debe incluir:

- kWh actual.
- consumo esperado.
- fuente del baseline.
- cantidad de muestras.
- desviacion porcentual.
- z-score, si aplica.
- razon de severidad.

## 12. Recomendaciones dinamicas

Las recomendaciones pueden generarse por reglas:

### LOW

```text
Monitorear el medidor durante las proximas lecturas y verificar si el incremento se repite.
```

### MEDIUM

```text
Revisar horarios de uso y equipos activos fuera de horario. Comparar con actividades programadas del area.
```

### HIGH

```text
Inspeccionar equipos de alto consumo, climatizacion e iluminacion. Priorizar revision operativa del area.
```

### CRITICAL

```text
Escalar a mantenimiento y auditoria energetica. Validar posible falla de medidor, fuga energetica o equipo encendido permanentemente.
```

Reglas adicionales:

- Si `zScore` alto pero desviacion porcentual moderada:
  - recomendar revisar cambio abrupto de patron.
- Si desviacion alta pero z-score no disponible:
  - explicar que la decision se basa en baseline fijo por falta de historial.
- Si `powerFactor` bajo:
  - recomendar revisar factor de potencia.
- Si `voltage` anormal:
  - recomendar revisar estabilidad electrica.

## 13. Reflejo en Analytics

Sin cambiar JSON externo en primera fase:

- Analytics seguira leyendo anomalias y KPIs actuales.
- `explanation` y `recommendation` ya existen en la respuesta de anomalias.
- Esos campos pueden empezar a contener textos dinamicos.
- `estimatedCostImpact` y `estimatedCo2Impact` seguiran igual.
- `deviationPercent` y `severity` seguiran igual en nombre y tipo.

Pendiente para una fase posterior:

- Agregar campos internos o externos:
  - `baselineSource`
  - `zScore`
  - `movingAverageKwh`
  - `standardDeviationKwh`
  - `sampleCount`

No agregar estos campos todavia si se quiere conservar JSON exacto.

## 14. Cambios minimos requeridos en EnergyOps

En `EnergyAnalysisService`:

- Reemplazar el calculo directo de `EnergyAnalysisPolicy` por llamada a `AnalyzeHybridEnergyUseCase`.
- Mantener guardado de lectura y anomalia como hoy.
- Mantener `AnalyzeEnergyReadingResult` igual.
- Mapear resultado IA a campos existentes:
  - `anomalyDetected`
  - `severity`
  - `deviationPercent`
  - `recommendation`
  - `estimatedCostImpact`
  - `estimatedCo2Impact`
  - `explanation` en `EnergyAnomalyRecord`

No cambiar:

- `EnergyOpsController`
- `AnalyzeReadingRequest`
- `AnalyzeReadingResponse`
- ruta `/api/energyops/analyze-reading`

## 15. Cambios que NO deben hacerse todavia

- No mover IAM.
- No modificar login/JWT.
- No cambiar endpoints.
- No cambiar JSON externo.
- No agregar frontend nuevo.
- No crear microservicio real para IA.
- No introducir LLMs.
- No introducir redes neuronales.
- No crear tablas nuevas.
- No modificar seeds salvo que se necesiten mas lecturas demo para pruebas.
- No mover entidades JPA a `ai-service` en la primera fase.
- No mover Analytics completo.

## 16. Riesgos de implementacion

- Calcular el promedio incluyendo la lectura actual y suavizar la anomalia.
- Historial insuficiente que genere falsos positivos.
- Desviacion estandar cero cuando todas las lecturas historicas son iguales.
- Duplicar reglas entre `EnergyAnalysisPolicy` y `HybridEnergyAnalysisPolicy`.
- Cambiar accidentalmente el JSON externo.
- Crear dependencia circular entre backend y ai-service.
- Hacer que `ai-domain` dependa de Spring/JPA.
- Persistir campos nuevos sin columnas existentes.
- Que recomendaciones dinamicas sean demasiado genericas.

Mitigaciones:

- Mantener fallback deterministico.
- Crear pruebas unitarias para calculos.
- Mantener contrato REST congelado.
- Mantener los campos nuevos como internos hasta fase posterior.
- Validar con Postman despues de cada fase.

## 17. Orden de implementacion por fases

### Fase 1: Dominio IA puro

Crear en `ai-domain`:

- `EnergyReadingSample`
- `HistoricalEnergyWindow`
- `DynamicBaseline`
- `EnergyAnomalyScore`
- `HybridAiAnalysis`
- `HybridEnergyAnalysisPolicy`
- `ExplanationTemplatePolicy`
- `RecommendationPolicy`
- enums de soporte

Validar con unit tests puros.

### Fase 2: Aplicacion IA

Crear en `ai-application`:

- `AnalyzeHybridEnergyUseCase`
- `HybridEnergyAnalysisService`
- command/result
- `LoadEnergyReadingHistoryPort`

Validar con tests usando historial fake.

### Fase 3: Adapter de historial

Crear adapter usando lecturas existentes.

Opciones:

- En backend primero, para minimizar integracion Maven.
- En `ai-infrastructure` si ya se conecta el modulo al reactor.

Validar que no se cambie schema.

### Fase 4: Integracion EnergyOps

Modificar solo `EnergyAnalysisService`:

- Invocar IA hibrida.
- Persistir lectura y anomalia igual que ahora.
- Mantener respuesta REST igual.

### Fase 5: Analytics

Sin cambiar JSON:

- Verificar que anomalias muestran explicacion y recomendacion dinamicas.

Fase posterior opcional:

- Exponer `zScore`, `baselineSource`, `sampleCount`.

## 18. Pruebas con Postman/Thunder Client

### 1. Login

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

Validar:

- HTTP 200.
- Respuesta contiene `token`, `userId`, `tenantId`, `roles`.

### 2. Crear lecturas normales para historial

Ejecutar varias veces:

```http
POST http://localhost:8080/api/energyops/analyze-reading
Content-Type: application/json
```

```json
{
  "facilityId": "Sede Central - Bloque B - Aula 3B",
  "meterId": "MED-DEMO-001",
  "measuredAt": "2026-06-01T08:00:00Z",
  "kwh": 100,
  "voltage": 220,
  "powerFactor": 0.95
}
```

Repetir con `kwh`: `98`, `101`, `99`, `102`, `100`.

Validar:

- Respuestas sin anomalia o severidad baja.

### 3. Crear lectura anomala

```json
{
  "facilityId": "Sede Central - Bloque B - Aula 3B",
  "meterId": "MED-DEMO-001",
  "measuredAt": "2026-06-02T12:00:00Z",
  "kwh": 165,
  "voltage": 220,
  "powerFactor": 0.95
}
```

Validar:

- `anomalyDetected = true`.
- `severity = HIGH` o `CRITICAL` segun z-score y desviacion.
- `recommendation` dinamica.
- No cambia estructura JSON.

### 4. Consultar anomalias

```http
GET http://localhost:8080/api/analytics/anomalies
```

Validar:

- HTTP 200.
- La anomalia incluye `explanation`.
- La recomendacion coincide con severidad y datos calculados.

### 5. Fallback sin historial

Usar un medidor sin historial o limpiar datos en ambiente controlado.

Validar:

- La app no falla.
- Usa baseline fijo o demo.
- Explicacion indica historial insuficiente.

## 19. Defensa academica ante jurado

Argumento principal:

> La solucion implementa una IA hibrida explicable porque combina aprendizaje estadistico simple del historial energetico con reglas de negocio energeticas y explicaciones trazables. No usa una caja negra: cada decision puede justificarse con promedio movil, desviacion estandar, Z-Score, baseline, tolerancia y reglas de severidad.

Puntos defendibles:

- Es adecuada para MVP porque no requiere grandes datasets.
- Es explicable porque cada resultado muestra la razon de la anomalia.
- Es deterministica y repetible.
- Es auditable porque no depende de respuestas generativas.
- Es compatible con SQL Server y la arquitectura actual.
- Mejora el baseline fijo actual con baseline dinamico cuando hay historial.
- Mantiene fallback para escenarios sin datos.
- Preserva endpoints y frontend.
- Respeta arquitectura hexagonal usando puertos y adaptadores.

Frase corta para presentacion:

> EnergiaClara AI no predice con una caja negra; calcula un patron normal por medidor, mide cuanto se aleja la lectura con Z-Score y desviacion porcentual, aplica reglas de severidad energetica y genera una explicacion entendible para el usuario.

## Decision recomendada

Implementar primero `ai-domain` y `ai-application` como modulos reales, con tests unitarios. Despues integrar EnergyOps mediante un puerto interno. En la primera version no exponer campos nuevos en JSON; solo mejorar internamente `severity`, `explanation` y `recommendation` manteniendo contratos actuales.

## Fase 1 implementada: ai-domain puro

Se implemento el nucleo puro de IA hibrida explicable en:

```text
ai-service/modules/ai-domain
```

El modulo fue conectado al reactor Maven raiz como `com.energiaclara:ai-domain`, sin conectarlo todavia con EnergyOps, Analytics, frontend, JWT ni base de datos.

Clases creadas:

- `EnergyAiInput`
- `EnergyAiHistoricalReading`
- `EnergyAiResult`
- `DynamicBaseline`
- `StatisticalAnomalyScore`
- `HybridSeverity`
- `ExplainableRecommendation`
- `HybridEnergyAiEngine`

Capacidades implementadas:

- baseline dinamico con promedio movil
- desviacion estandar
- Z-Score
- desviacion porcentual
- exceso kWh
- score de anomalia
- severidad `NORMAL`, `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
- confianza
- fallback deterministico si no hay historial suficiente
- explicacion dinamica sin LLM
- recomendacion dinamica
- `modelVersion` con valor `hybrid-stat-rules-v1.0`

Restricciones cumplidas:

- Sin imports de Spring.
- Sin imports de JPA.
- Sin dependencias del backend.
- Sin dependencias de infraestructura.
- Sin cambios en endpoints.
- Sin cambios en JSON externo.
- Sin cambios en frontend.
- Sin cambios en base de datos.
- Sin cambios en login/JWT.
- Sin cambios en Analytics.

Tests agregados:

- baseline dinamico con historial suficiente
- fallback a baseline fijo con historial insuficiente
- consumo normal dentro del rango esperado

Validacion:

```bash
mvn clean test
```

Resultado: build exitoso desde la raiz del proyecto.

Pendiente para Fase 2:

- Crear `ai-application`.
- Crear puerto de entrada para invocar la IA.
- Crear puerto de salida para cargar historial.
- Integrar con EnergyOps sin cambiar contrato REST.

## Fase 2 implementada: ai-application hexagonal

Se implemento la capa de aplicacion en:

```text
ai-service/modules/ai-application
```

El modulo fue conectado al reactor Maven raiz como `com.energiaclara:ai-application` y depende solo de `ai-domain`.

Clases creadas:

- `AnalyzeEnergyWithAiUseCase`
- `AnalyzeEnergyWithAiService`
- `EnergyAiAnalysisCommand`
- `EnergyAiAnalysisResponse`
- `EnergyHistoryProviderPort`
- `EnergyAiEnginePort`

Responsabilidad de la capa:

- Orquestar la carga de historial mediante puerto.
- Construir `EnergyAiInput`.
- Invocar el motor de IA mediante puerto.
- Convertir `EnergyAiResult` en una respuesta de aplicacion.
- Mantener el fallback definido por el dominio.

Arquitectura resultante:

```text
ai-application
  -> port.out.EnergyHistoryProviderPort
  -> port.out.EnergyAiEnginePort
  -> ai-domain
```

Restricciones cumplidas:

- Sin imports de Spring.
- Sin imports de JPA.
- Sin dependencias del backend.
- Sin acceso a SQL Server.
- Sin cambios en endpoints.
- Sin cambios en frontend.
- Sin cambios en base de datos.
- Sin cambios en JWT/IAM.
- Sin cambios en Analytics.

Tests agregados:

- Orquestacion de historial y motor de dominio.
- Respeto del fallback cuando el historial es insuficiente.
- Uso de un `EnergyAiEnginePort` fake para demostrar inversion de dependencias.

Validacion:

```bash
mvn clean test
```

Resultado: build exitoso desde la raiz del proyecto.

Pendiente para Fase 3:

- Crear adapter de historial en `ai-infrastructure` o en backend mientras no se integre infraestructura IA.
- Integrar `AnalyzeEnergyWithAiUseCase` con EnergyOps sin cambiar el contrato REST.

## Fase 3 implementada: ai-infrastructure demo

Se implemento la capa de adapters en:

```text
ai-service/modules/ai-infrastructure
```

El modulo fue conectado al reactor Maven raiz como `com.energiaclara:ai-infrastructure`.

Clases creadas:

- `InMemoryEnergyHistoryProviderAdapter`
- `DefaultEnergyAiEngineAdapter`
- `ManualAiServiceFactory`

Responsabilidad de la capa:

- Implementar `EnergyHistoryProviderPort` con historial demo en memoria.
- Implementar `EnergyAiEnginePort` delegando al `HybridEnergyAiEngine`.
- Componer manualmente un caso de uso sin Spring.
- Validar el flujo hexagonal completo sin tocar EnergyOps real.

Arquitectura resultante:

```text
ai-infrastructure
  -> ai-application
      -> ai-domain
  -> ai-domain
```

Restricciones cumplidas:

- Sin Spring.
- Sin JPA.
- Sin SQL Server.
- Sin dependencias del backend principal.
- Sin endpoints nuevos.
- Sin cambios en frontend.
- Sin cambios en base de datos.
- Sin cambios en JWT/IAM.
- Sin cambios en Analytics.

Tests agregados:

- Adapter en memoria filtra por tenant/medidor y ordena lecturas recientes.
- Adapter de motor delega al dominio.
- Factory manual compone un caso de uso demo sin Spring.
- Fallback con historial vacio.

Validacion:

```bash
mvn clean test
```

Resultado: build exitoso desde la raiz del proyecto.

Pendiente para Fase 4:

- Integrar EnergyOps real con `AnalyzeEnergyWithAiUseCase`.
- Crear adapter real de historial contra las lecturas existentes.
- Mantener endpoint y JSON externo sin cambios.

## Fase 4 implementada: integracion controlada con EnergyOps

Se integro EnergyOps real con el `ai-service` mediante puerto/adaptador, sin crear microservicio externo y sin cambiar el contrato HTTP.

Elementos implementados en backend:

- `EnergyAiAnalysisPort`
- `EnergyAiAnalysisRequest`
- `EnergyAiAnalysisOutcome`
- `HybridEnergyAiAnalysisAdapter`

Cambios de integracion:

- `EnergyAnalysisService` invoca el puerto `EnergyAiAnalysisPort`.
- El adapter obtiene historial real con `EnergyReadingRepository`.
- El adapter convierte lecturas persistidas a `EnergyAiHistoricalReading`.
- El adapter llama `AnalyzeEnergyWithAiUseCase`.
- La respuesta de IA se mapea a severidad, desviacion, explicacion, recomendacion, costo y CO2.
- Si la IA devuelve fallback o falla, se mantienen los calculos deterministas anteriores.

Decision de contrato externo:

- `POST /api/energyops/analyze-reading` conserva la misma ruta.
- El JSON de entrada se mantiene igual.
- El JSON de respuesta se mantiene igual.
- No se agregaron `aiUsed`, `confidence`, `modelVersion` ni explicacion extendida al response para no arriesgar el frontend.
- La explicacion dinamica y la recomendacion dinamica quedan persistidas en la anomalia y son visibles por Analytics cuando existe anomalia.

Comportamiento actual:

- Con historial suficiente, EnergyOps usa IA hibrida explicable.
- Con historial insuficiente, EnergyOps conserva el fallback deterministico actual.
- `iaUtilizada` se persiste en la anomalia cuando el modelo existente lo permite.
- No se modifico SQL Server ni la estructura de tablas.

Tests agregados:

- Lectura normal con IA.
- Lectura anomalica con IA.
- Fallback cuando la IA reporta historial insuficiente.
- Adapter IA con historial suficiente.
- Adapter IA con historial insuficiente.

Validacion:

```bash
mvn clean test
```

Resultado: build exitoso desde la raiz del proyecto.

Smoke test ejecutado con Spring Boot:

- `POST /api/auth/login`: 200
- `POST /api/energyops/analyze-reading` lectura normal: 201
- `POST /api/energyops/analyze-reading` lectura anomalica: 201
- `GET /api/analytics/anomalies`: 200

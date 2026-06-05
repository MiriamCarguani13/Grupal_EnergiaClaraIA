# Arquitectura interna de ai-service

`ai-service` esta siendo construido por fases como modulo enterprise, sin convertirlo todavia en microservicio real y sin tocar los endpoints actuales.

Estado actual:

```text
ai-service/
└── modules/
    ├── ai-domain
    ├── ai-application
    └── ai-infrastructure
```

Todavia no se integran:

- `ai-api`
- `ai-bootstrap`
- EnergyOps real
- Analytics real
- SQL Server productivo

## ai-domain

Ubicacion:

```text
ai-service/modules/ai-domain
```

Responsabilidad:

- Contener el nucleo puro de IA hibrida explicable.
- Calcular baseline dinamico, promedio movil, desviacion estandar, Z-Score, desviacion porcentual, score, severidad, confianza, explicacion, recomendacion y version del modelo.

Clases principales:

- `EnergyAiInput`
- `EnergyAiHistoricalReading`
- `EnergyAiResult`
- `DynamicBaseline`
- `StatisticalAnomalyScore`
- `HybridSeverity`
- `ExplainableRecommendation`
- `HybridEnergyAiEngine`

Reglas:

- No usa Spring.
- No usa JPA.
- No conoce backend.
- No conoce base de datos.
- No usa LLMs.

## ai-application

Ubicacion:

```text
ai-service/modules/ai-application
```

Responsabilidad:

- Orquestar el caso de uso de analisis energetico con IA.
- Exponer puertos de entrada y salida.
- Convertir command de aplicacion a input de dominio.
- Convertir resultado de dominio a response de aplicacion.

Clases principales:

- `AnalyzeEnergyWithAiUseCase`
- `AnalyzeEnergyWithAiService`
- `EnergyAiAnalysisCommand`
- `EnergyAiAnalysisResponse`
- `EnergyHistoryProviderPort`
- `EnergyAiEnginePort`

Dependencias:

```text
ai-application -> ai-domain
```

Reglas:

- No usa Spring.
- No usa JPA.
- No conoce SQL Server.
- No depende del backend principal.

## ai-infrastructure

Ubicacion:

```text
ai-service/modules/ai-infrastructure
```

Responsabilidad actual:

- Proveer adapters demo para probar la arquitectura hexagonal sin tocar EnergyOps ni SQL Server.

Clases principales:

- `InMemoryEnergyHistoryProviderAdapter`
- `DefaultEnergyAiEngineAdapter`
- `ManualAiServiceFactory`

Dependencias:

```text
ai-infrastructure -> ai-application -> ai-domain
ai-infrastructure -> ai-domain
```

Reglas actuales:

- No usa Spring.
- No usa JPA.
- No accede a SQL Server.
- No expone endpoints.
- No toca backend.

## Flujo interno de IA

Flujo demo actual:

```text
ManualAiServiceFactory
  -> AnalyzeEnergyWithAiService
      -> EnergyHistoryProviderPort
          -> InMemoryEnergyHistoryProviderAdapter
      -> EnergyAiEnginePort
          -> DefaultEnergyAiEngineAdapter
              -> HybridEnergyAiEngine
                  -> EnergyAiResult
      -> EnergyAiAnalysisResponse
```

El flujo ya demuestra:

- Caso de uso.
- Puerto de entrada.
- Puertos de salida.
- Adapter de historial.
- Adapter de motor.
- Dominio puro.
- Tests de integracion ligera.

## Integracion actual con EnergyOps

EnergyOps ya se conecta con `ai-service` mediante un puerto de salida del backend y un adapter Spring ubicado en infraestructura del backend.

Flujo real actual:

```text
POST /api/energyops/analyze-reading
  -> EnergyOpsController
  -> EnergyAnalysisService
  -> EnergyAiAnalysisPort
  -> HybridEnergyAiAnalysisAdapter
      -> EnergyReadingRepository
      -> AnalyzeEnergyWithAiUseCase
          -> AnalyzeEnergyWithAiService
              -> EnergyHistoryProviderPort
              -> EnergyAiEnginePort
                  -> DefaultEnergyAiEngineAdapter
                      -> HybridEnergyAiEngine
```

La decision de ubicar `HybridEnergyAiAnalysisAdapter` en backend/infrastructure es intencional:

- El adapter necesita Spring y repositories JPA reales.
- `ai-domain`, `ai-application` y `ai-infrastructure` siguen sin depender del backend.
- La direccion de dependencia queda controlada: backend depende de ai-service, ai-service no depende del backend.
- No se creo microservicio externo.

Contrato publico conservado:

- No cambio la ruta `POST /api/energyops/analyze-reading`.
- No cambio el JSON de entrada.
- No cambio el JSON de respuesta.
- No cambio frontend.
- No cambio SQL Server.
- No cambio JWT/IAM.

El resultado de IA mejora internamente:

- `severity`
- `deviationPercent`
- `recommendation`
- `estimatedCostImpact`
- `estimatedCo2Impact`
- `explanation` persistida para Analytics cuando hay anomalia.

No se exponen todavia:

- `aiUsed`
- `confidence`
- `modelVersion`

Estos campos quedan disponibles internamente para una fase posterior si se decide ampliar el contrato de manera versionada.

## Estado academico defendible

La arquitectura ya muestra IA hibrida explicable con separacion enterprise:

- `ai-domain`: calculo e inferencia explicable.
- `ai-application`: caso de uso y puertos.
- `ai-infrastructure`: adapters.

La integracion con EnergyOps ya esta activa de forma controlada. La aplicacion sigue siendo un monolito modular: EnergyOps consume el caso de uso de IA por puerto/adaptador, sin exponer un servicio externo ni romper contratos existentes.

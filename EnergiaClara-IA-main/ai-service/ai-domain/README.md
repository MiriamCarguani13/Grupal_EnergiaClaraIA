# ai-domain

## Propósito
Dominio puro del bounded context AI. Aggregates + value objects + domain events + invariantes.

## Reglas
- ❌ Sin Spring, JPA, Jackson, servlet
- ❌ Sin `@Component`, `@Entity`, `@Inject`
- ✅ Solo Java estándar + `core-platform` (TenantId, KwhValue, etc.)

## Estructura sugerida
```
com.energiaclara.ai.domain
├── aggregates           AnomalyDetection, BaselineCalculation, PredictionResult
├── valueobjects         AnomalyScore, ConfidenceLevel, ModelVersion
├── events               AnomalyPredicted, BaselineRecomputed
└── services             AnomalyDetectionService (lógica que cruza aggregates)
```

## Tests
- Unit tests del dominio sin Spring (milisegundos)
- ArchUnit test recomendado: no imports framework

## TODO equipo
- [ ] Definir `AnomalyDetection` aggregate
- [ ] Value object `AnomalyScore` con rango 0-100 e invariante
- [ ] `AnomalyDetectionService.detectarAnomalia(consumo, baseline) → AnomalyDetection`
- [ ] Tests invariantes (Fase 2 §10)

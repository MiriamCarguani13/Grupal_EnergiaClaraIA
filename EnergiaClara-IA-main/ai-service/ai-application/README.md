# ai-application

## Propósito
Use cases AI + ports (in/out) + commands. Orquesta dominio sin acoplarse a framework.

## Reglas
- ❌ Sin Spring (excepto `@Transactional` si justificado)
- ❌ Sin JPA
- ✅ Define puertos como interfaces
- ✅ Use cases como interfaces (in port) + implementación POJO

## Estructura sugerida
```
com.energiaclara.ai.application
├── port.in              DetectAnomalyUseCase, RecomputeBaselineUseCase, ExplainAnomalyUseCase
├── port.out             LlmPort, VectorStorePort, BaselineRepositoryPort, AnomalyResultRepositoryPort
├── usecase              DetectAnomalyCommand, ExplainAnomalyCommand (records)
├── dto                  AnomalyResult, PredictionExplanation
└── service              AiApplicationService (implementa los UseCase, depende solo de ports)
```

## TODO equipo
- [ ] `DetectAnomalyUseCase` interface + comando
- [ ] `LlmPort.predict(prompt, context) → response`
- [ ] `BaselineRepositoryPort.findActiveBy(tenantId, meterId)`
- [ ] `AiApplicationService` con constructor injection de puertos out
- [ ] Tests unitarios con mocks de puertos (sin Spring)

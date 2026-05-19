# ai-infrastructure

## Propósito
Adapters concretos: implementaciones de puertos out + integraciones externas.

## Estructura sugerida
```
com.energiaclara.ai.infrastructure
├── adapter
│   ├── SpringAiLlmAdapter           implements LlmPort
│   ├── PgVectorAdapter              implements VectorStorePort
│   ├── BaselineJpaAdapter           implements BaselineRepositoryPort
│   └── AnomalyResultJpaAdapter      implements AnomalyResultRepositoryPort
├── persistence
│   ├── entity                       BaselineEntity, AnomalyResultEntity (JPA)
│   └── repository                   BaselineJpaRepository, etc.
└── config
    └── SpringAiConfig               beans Spring AI, ChatClient, EmbeddingModel
```

## Reglas
- ✅ Spring, JPA, Spring AI permitidos aquí
- ❌ No depende de `ai-api` (controllers no se llaman desde adapters)
- ❌ No depende de `ai-bootstrap`

## TODO equipo
- [ ] Añadir deps Spring AI cuando se elija provider (OpenAI, Anthropic, local)
- [ ] `SpringAiLlmAdapter` implementando `LlmPort`
- [ ] Si usa pgvector: setup PostgreSQL con extension `vector`
- [ ] Tests integración con `@SpringBootTest(classes = AiInfrastructureTestConfig.class)`

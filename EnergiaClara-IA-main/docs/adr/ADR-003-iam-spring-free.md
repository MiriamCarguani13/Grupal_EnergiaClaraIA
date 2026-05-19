# ADR-003: iam-service Spring-free, wiring en infrastructure

## Estado
Aceptado · 2026-05-18

## Contexto

`IamApplicationService` implementa `LoginUseCase` y `RegisterUserUseCase`. Necesita ser invocable desde `AuthController`. Dos opciones para que Spring lo encuentre como bean:

**Opción A:** Anotar `IamApplicationService` con `@Service`. Requiere añadir `spring-context` como dependencia de `iam-service`.

**Opción B:** Dejar `IamApplicationService` como POJO. Wirearlo manualmente desde un `@Configuration` en `energiaclara-infrastructure`.

## Decisión

**Opción B. iam-service permanece 100% Spring-free.**

- `IamApplicationService` = POJO con constructor injection
- `IamWiringConfig` en infrastructure expone `IamApplicationService`, `LoginUseCase`, `RegisterUserUseCase` como beans Spring
- ArchUnit valida `iam-service` sin imports `org.springframework.*` (ver ADR-008)

## Alternativas consideradas

**Opción A (anotar `@Service`):**
- ❌ Contamina módulo con dep Spring
- ❌ Imposible reusar iam-service en aplicación non-Spring (CLI, microservicio Quarkus, etc.)
- ❌ Tests del use case requieren contexto Spring (lento)

**Opción C (CDI / `@Inject` neutral):**
- ❌ Más dependencias, jakarta.inject obliga runtime
- ❌ Compleja para beneficio marginal

## Consecuencias

**Beneficios:**
- iam-service portable a cualquier framework (Quarkus, Micronaut, CLI, batch)
- Tests del use case = `new IamApplicationService(mockRepo, mockHasher, mockToken)` directo
- Compilation enforcement: imposible importar Spring accidentalmente

**Cedemos:**
- Boilerplate ~10 líneas en `IamWiringConfig` por bean
- Si añadimos nuevo use case, hay que recordar wirearlo

**Riesgo mitigado:** ArchUnit `IamArchitectureTest.iam_module_does_not_depend_on_spring()` rompe build si alguien añade `@Service`.

## Referencias
- Fase 3 §4 (Separación Application vs Domain)
- Fase 3 §10 (Anti-patrones — dominio dependiente de frameworks)
- ADR-008 (ArchUnit)

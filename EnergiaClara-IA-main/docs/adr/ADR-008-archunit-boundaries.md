# ADR-008: ArchUnit para enforcing Dependency Rule

## Estado
Aceptado · 2026-05-19

## Contexto

Fase 3 §1 establece Dependency Rule: "Las dependencias siempre apuntan hacia el interior. Las capas externas dependen de las internas, pero el núcleo nunca conoce lo que hay afuera."

Sin enforcement automático, esta regla degrada por:
- Junior dev importa `@Entity` en aggregate de domain
- "Solo por esta vez" → permanente
- Code review ocasionalmente cacha violaciones
- Refactor accidental rompe arquitectura

Solveria (ProyectoAI) usa ArchUnit con este propósito.

## Decisión

**ArchUnit 1.2.1 como test obligatorio en CI.** Reglas codificadas:

**`core-platform`:**
- ❌ No depende de `org.springframework.*`
- ❌ No depende de `jakarta.persistence.*`, `javax.persistence.*`, `org.hibernate.*`
- ❌ No depende de `com.fasterxml.jackson.*`
- ❌ No depende de `jakarta.servlet.*`, `javax.servlet.*`

**`iam-service`:**
- ❌ No depende de `org.springframework.*` (ADR-003)
- ❌ No depende de JPA
- ❌ `domain` no depende de `application`

**`energiaclara-application`:**
- ❌ No depende de `org.springframework.*`
- ❌ No depende de JPA

Tests corren con `mvn test` en cada módulo. Fail en CI rompe build.

## Alternativas consideradas

- **Code review manual**: rechazado. No escala, humano olvida.
- **SonarQube custom rules**: rechazado. Setup pesado, requiere infra.
- **Maven enforcer plugin con `banned-dependencies`**: usable para deps externas pero no para reglas de paquete internas. Complementario, no sustituto.
- **jMolecules / Spring Modulith**: deferido. Más estructurado pero introduce dependency runtime.

## Consecuencias

**Beneficios:**
- Dependency Rule física, no convencional
- Violaciones detectadas en build, no en review
- Documentación ejecutable (tests dicen qué está prohibido)
- Onboarding mejorado: nuevo dev ve los tests = entiende reglas

**Cedemos:**
- Mantenimiento de tests cuando aparecen nuevas dependencias legítimas
- Falsos positivos requieren `@ArchIgnore` o ajuste de regla

**Riesgo:**
- Devs frustrados podrían desactivar tests. Mitigar: bloquear PR si test arch falla, no permitir skip.

## Reglas futuras (Hito 2/3)

- `infrastructure.adapter.*` debe implementar al menos un puerto de `application.port.out.*`
- Controllers en `api.rest.*` deben tener `@RestController`
- Aggregates en `domain.*` no deben tener setters públicos (modelo no anémico)

## Referencias
- Fase 3 §1 (Dependency Rule)
- Fase 3 §10 (Anti-patrones)
- Solveria/ProyectoAI `CoreArchitectureTest.java`
- `core-platform/src/test/java/com/energiaclara/core/architecture/CoreArchitectureTest.java`

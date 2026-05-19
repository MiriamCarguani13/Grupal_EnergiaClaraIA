# ADR-005: Flyway deshabilitado, schema DBA-owned

## Estado
Aceptado · 2026-05-18

## Contexto

Repo contiene dos fuentes de schema SQL Server:

1. **`database/script.sql`** — dump completo SSMS generado por DBA del equipo. UTF-16 LE. T-SQL puro con `GO`. 32 tablas en 8 schemas (`iam`, `core`, `consumo`, `energiaops`, `audit`, `analitica`, `educacion`, `mantenimiento`).
2. **`energiaclara-infrastructure/src/main/resources/db/migration/V1__create_schemas.sql`, `V2__create_tables_and_constraints.sql`, `V3__insert_demo_data.sql`** — migraciones Flyway. Tienen errores de sintaxis (`uq_kpi_diario_alcance]` con `]` sin abrir corchete, `UUID` no es tipo nativo SQL Server, etc.).

Si habilitamos Flyway, intenta correr migraciones y falla. Si DBA gestiona el schema manualmente, Flyway sobra.

## Decisión

**Flyway deshabilitado vía `spring.flyway.enabled: false`. Schema canónico = `database/script.sql` + `database/seeds.sql` ejecutados manualmente por DBA.**

`spring.jpa.hibernate.ddl-auto: none` complementa: Hibernate no valida ni modifica schema en arranque.

Migraciones V1/V2/V3 permanecen en `db/migration/` como referencia histórica pero NO se ejecutan.

## Alternativas consideradas

- **Habilitar Flyway con migraciones existentes**: rechazado. V2 contiene errores de sintaxis. Riesgo alto.
- **Arreglar V1/V2/V3 + habilitar Flyway**: deferido. Costo alto (3 migraciones ~500 líneas T-SQL), beneficio bajo (DBA ya tiene script.sql funcionando).
- **Eliminar database/script.sql y vivir solo con Flyway**: rechazado. DBA del equipo lo mantiene desde SSMS; cambio de workflow.

## Consecuencias

**Beneficios:**
- Schema canónico = fuente de verdad clara (DBA + SSMS)
- Sin riesgo de migraciones rotas en arranque
- DBA mantiene control total del DDL

**Cedemos:**
- Sin replicabilidad automática del schema en CI/CD
- Setup nuevo dev requiere correr scripts manualmente (`script.sql` + `seeds.sql` + roles base)
- Sin histórico de cambios de schema versionado

**Mitigación:**
- README documenta setup manual paso a paso
- Posible v2: regenerar migraciones Flyway desde `script.sql` actual + habilitar para CI/CD

## Referencias
- `database/script.sql` (canónico)
- `application.yml` `spring.flyway.enabled: false`
- README §1 (Base de datos)

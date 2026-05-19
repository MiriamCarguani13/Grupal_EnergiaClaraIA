# ADR-004: Role enum alineado con nombres de DB

## Estado
Aceptado · 2026-05-18

## Contexto

Estado inicial del repo tenía mismatch entre `Role.java` enum y `[iam].[rol].nombre`:

- `Role.java`: `ADMIN_PLATAFORMA, ADMIN_INSTITUCION, OPERADOR_MANTENIMIENTO, DOCENTE, ESTUDIANTE, AUDITOR`
- DB / seeds / README: `ADMIN_INSTITUCION, DIRECTOR, DOCENTE, TECNICO, ESTUDIANTE, AUDITOR`

`ADMIN_PLATAFORMA` y `OPERADOR_MANTENIMIENTO` solo existían en código. `DIRECTOR` y `TECNICO` solo existían en DB y docs.

Doc Fase 1 §1.3 "Segmentación Estratégica (Tabla de Actores)" lista explícitamente: Admin Institución, Director, Docente, Estudiante, Técnico, Auditor.

## Decisión

**Alinear enum con DB + docs.**

```java
public enum Role {
    ADMIN_INSTITUCION,
    DIRECTOR,
    DOCENTE,
    TECNICO,
    ESTUDIANTE,
    AUDITOR
}
```

`ADMIN_PLATAFORMA` y `OPERADOR_MANTENIMIENTO` eliminados.

## Alternativas consideradas

- **Mantener enum + agregar mapeo entre nombres**: rechazado. Indirección sin valor; confunde al equipo.
- **Cambiar DB para que use nombres del enum original**: rechazado. Docs Fase 1 son fuente de verdad de negocio; cambiar DB rompe seeds + README.
- **Agregar `ADMIN_GLOBAL` para super-admin cross-tenant**: deferido. No hay caso de uso v1.

## Consecuencias

**Beneficios:**
- `IamUserRepositoryAdapter.toDomain()` mapea `rol.nombre` → `Role.valueOf()` sin transformación
- JWT claim `roles: ["ADMIN_INSTITUCION"]` directo del enum
- `@PreAuthorize("hasRole('ADMIN_INSTITUCION')")` coincide con seeds

**Cedemos:**
- Cambio breaking si alguien ya estaba usando enum viejo (nadie hasta este punto)

**Riesgo:**
- Si DB recibe rol nuevo no presente en enum, `Role.valueOf()` lanzaría. `IamUserRepositoryAdapter` captura `IllegalArgumentException` y skipea silenciosamente (TODO: log warn).

## Referencias
- Fase 1 §1.3 (Tabla de Actores)
- README Roles disponibles
- `database/seeds.sql`

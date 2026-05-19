# ADR-001: Monorepo multi-módulo Maven (vs multi-repo microservicios)

## Estado
Aceptado · 2026-05-18

## Contexto

EnergíaClara AI requiere separación clara de capas (hexagonal) y bounded contexts (DDD) para cumplir Fase 3 y Fase 4 documentadas. Existen dos patrones enterprise comunes:

1. **Multi-repo + microservicios** (estilo Solveria/ProyectoAI): cada servicio en su repo Git, cada uno corre como aplicación Spring Boot independiente, comunicación HTTP/gRPC, despliegue separado.
2. **Monorepo + multi-módulo Maven**: un solo repo, módulos Maven separados, build atómico, despliegue como JAR único.

El proyecto es académico, equipo pequeño (6 integrantes), un solo dominio de negocio, presupuesto de ops ~0. Sin embargo, los docs piden mentalidad enterprise (Fase 4 §11 proyección escalabilidad).

## Decisión

**Monorepo multi-módulo Maven.** Estructura:

```
energiaclara-platform/  (pom raíz)
├── core-platform                  shared kernel (domain)
├── iam-service                    bounded context auth
├── energiaclara-application       use cases EnergiaClara
├── energiaclara-infrastructure    bootstrap + adapters + REST
└── ai-service                     bounded context IA (skeleton)
```

## Alternativas consideradas

- **Multi-repo microservicios** (Solveria): rechazado por overhead operativo (3+ repos, build orchestration, network calls, latencia, deploy coordinado). No justificado a escala actual.
- **Monolito sin módulos**: rechazado. Imposible enforcing Dependency Rule del dominio. No alineado con Fase 3.
- **Layers tradicionales (`controllers/`, `services/`, `repositories/`)**: rechazado. Esconde bounded contexts, dificulta extracción futura.

## Consecuencias

**Beneficios:**
- Refactor cross-módulo en 1 PR atómico
- Build verifica todo junto
- Onboarding rápido (pom.xml raíz = mapa mental)
- Sin overhead red entre módulos
- Camino limpio a microservicios cuando crezca (módulos ya aislados)
- Dependency Rule física, forzada por compilador

**Cedemos:**
- No escala despliegue independiente (todo se redeploya junto)
- Un proceso, un fallo afecta todo
- Build completo cuando módulo pequeño cambia

**Mitigación futura:** cada bounded context (`iam-service`, `ai-service`) ya está estructurado para extraerse a repo propio con `mvn package` cuando justifique microservicio.

## Referencias
- Fase 3 §11 (Estructura de paquetes recomendada)
- Fase 4 §11 (Escalabilidad y particionamiento)
- ADR-002 (Hexagonal)

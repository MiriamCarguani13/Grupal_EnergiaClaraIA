# ai-bootstrap

## Propósito
Bootstrap del **microservicio AI standalone** (opcional). Espejo de `ai-bootstrap` en Solveria.

⚠️ **Solo se usa si AI corre como microservicio separado en puerto distinto a EnergíaClara (ej. 8091).** Si AI corre embebido en `energiaclara-infrastructure`, este módulo no se completa — el bootstrap principal de EnergíaClara declara dep a `ai-application` + `ai-infrastructure` directamente.

## Estructura sugerida (si standalone)
```
com.energiaclara.ai.bootstrap
├── AiServiceApplication             @SpringBootApplication, main()
└── config
    ├── AiWiringConfig               beans manuales para mantener Spring-free ai-application
    └── AiSecurityConfig             JWT compartido con EnergíaClara (mismo secret)

src/main/resources
├── application.yml                  port 8091, deps Spring AI, DB própia o compartida
├── application-dev.yml              stubs LLM, sin OpenAI key
└── application-prod.yml             real LLM, secrets via env
```

## Decisión pendiente equipo
**¿AI embebido o standalone?**

| Opción | Ventajas | Desventajas |
|---|---|---|
| Embebido en EnergíaClara | 1 proceso, 1 deploy, llamadas in-process, sin auth duplicada | Acopla deploy, mismo JVM heap, ai pesado afecta principal |
| Standalone microservicio | Scaling independiente, fault isolation, posible stack diferente (Python para ML) | Network calls, JWT compartido, ops duplicado |

**Recomendación v1**: **embebido**. Sigue ADR-001 (monorepo modular). Migrar a standalone cuando crezca + justifique.

Si embebido: ignorar `ai-bootstrap` o dejar `<skip>true</skip>` en spring-boot-maven-plugin (ya configurado así).

## TODO equipo (solo si standalone)
- [ ] `AiServiceApplication` con `@SpringBootApplication`
- [ ] `application.yml` con port 8091 + datasource AI
- [ ] Configurar JWT con MISMO secret que EnergíaClara (servicios federados)
- [ ] Docker compose entry: `ai-service:8091`
- [ ] Health endpoint `/actuator/health`

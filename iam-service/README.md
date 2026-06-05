# iam-service

Estructura preparada para responsabilidades de identidad y acceso.

Estado actual:

- Es un modulo Maven interno real.
- Esta conectado al reactor Maven raiz.
- Contiene dominio IAM.
- Contiene capa de aplicacion IAM.
- Depende de `core-platform`.
- No depende de `backend`.
- Los endpoints actuales de autenticacion siguen en `backend/`.

Pendiente:

- Agregar tests unitarios especificos del modulo.
- Migrar adapters de password/token en una etapa posterior.
- Migrar persistence IAM en una etapa posterior.
- Evaluar `AuthController`, `SecurityConfig` y `JwtAuthFilter` solo cuando el flujo IAM este estable.

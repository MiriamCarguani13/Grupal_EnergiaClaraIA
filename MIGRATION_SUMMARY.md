# Resumen de migraciones enterprise

Fecha: 2026-06-02

## Estado final

El proyecto avanzo desde una estructura backend monolitica hacia un monolito modular enterprise alineado parcialmente al ejemplo del docente.

Resultado:

```text
CUMPLE PARCIALMENTE
```

Cumple parcialmente porque ya hay modulos reales y funcionales, pero se conservan adaptadores web/security/JPA en backend para no romper Spring Boot, frontend, SQL Server ni contratos HTTP.

## Migraciones reales realizadas

### core-platform

Modulo:

```text
core-plataform/core-platform
```

Responsabilidades:

- `TenantId`
- `UserId`
- `AuthenticatedUser`
- `TenantContextHolder`
- `AuditEvent`

Valor:

- Centraliza responsabilidades transversales.
- Evita duplicacion entre IAM, audit, security y backend.
- No depende de Spring/JPA/backend.

### iam-service

Modulo:

```text
iam-service
```

Responsabilidades migradas:

- Dominio IAM: `User`, `Role`, `Email`.
- Application IAM: login, registro, comandos, resultados y puertos.

Valor:

- IAM deja de estar totalmente incrustado en backend.
- Backend consume casos de uso y puertos desde un modulo separado.
- Se mantienen endpoints y JSON sin cambios.

Queda temporalmente en backend:

- `AuthController`.
- DTOs HTTP.
- `SecurityConfig`.
- `JwtAuthFilter`.
- adapters JWT/password.
- JPA IAM.

Motivo:

- Son adaptadores web/security/JPA necesarios para mantener Spring Boot y SQL Server estables.

### ai-service

Modulos:

```text
ai-service/modules/ai-domain
ai-service/modules/ai-application
ai-service/modules/ai-infrastructure
```

Responsabilidades:

- IA hibrida explicable.
- Baseline dinamico.
- Promedio movil.
- Desviacion estandar.
- Z-Score.
- Anomaly score.
- Confianza.
- Explicacion dinamica.
- Recomendacion dinamica.
- Caso de uso hexagonal.
- Puertos y adapters.

Valor:

- IA deja de ser reglas fijas solamente.
- EnergyOps usa ai-service por puerto/adaptador.
- Analytics evidencia explicacion dinamica.
- No se usan LLMs ni modelos opacos.

## Dependencias actuales

```text
backend
  -> core-platform
  -> iam-service
  -> ai-application
  -> ai-infrastructure

iam-service
  -> core-platform

ai-infrastructure
  -> ai-application
  -> ai-domain

ai-application
  -> ai-domain
```

Reglas cumplidas:

- `iam-service` no depende de backend.
- `ai-service` no depende de backend.
- `ai-domain` no depende de Spring/JPA.
- `core-platform` no depende de backend.

## Funcionalidad preservada

No se cambio:

- frontend;
- endpoints;
- JSON publico;
- SQL Server;
- login JWT;
- EnergyOps;
- Analytics.

Endpoints validados:

- `POST /api/auth/login`: HTTP `200`.
- `POST /api/energyops/analyze-reading` con Bearer: HTTP `201`.
- `GET /api/analytics/anomalies` con Bearer: HTTP `200`.

## Validacion final

Comandos:

```bash
mvn clean test
mvn install -DskipTests
cd backend
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=0
```

Resultado:

- Maven reactor: `BUILD SUCCESS`.
- Spring Boot: arranque correcto.
- SQL Server: conexion correcta.
- Puerto runtime validado: `65000`.
- Backend detenido correctamente al finalizar.

## Riesgos pendientes

- `iam-service` application todavia usa `@Service` y `@Transactional` para integrarse con Spring Boot sin configuracion adicional.
- JPA IAM sigue en backend.
- `JwtAuthFilter` y `SecurityConfig` siguen en backend.
- `ai-api` y `ai-bootstrap` siguen como estructura preparada.

Estos riesgos son aceptables para MVP porque preservan funcionalidad y evitan una migracion disruptiva.

## Siguiente fase recomendada

1. Agregar tests unitarios propios de `iam-service`.
2. Evaluar mover `BCryptPasswordHasherAdapter`.
3. Evaluar mover `JwtTokenAdapter` sin cambiar claims.
4. Preparar entity/repository scan para mover JPA IAM.
5. Mantener `SecurityConfig` y `JwtAuthFilter` en backend hasta decidir un modulo security interno.
6. No crear microservicios externos todavia.

## Validacion final post-cleanup

Fecha de validacion: `2026-06-02`.

Despues de la limpieza segura, se confirmo que no se elimino logica funcional: solo se habian retirado directorios fuente vacios heredados. No se tocaron endpoints, frontend, base de datos, IA, EnergyOps, Analytics, IAM, enums conceptuales ni `package-info.java` arquitectonicos.

Comandos ejecutados desde la raiz:

```bash
mvn clean test
mvn install -DskipTests
```

Resultado:

- `mvn clean test`: `BUILD SUCCESS`.
- `mvn install -DskipTests`: `BUILD SUCCESS`.
- Modulos validados: `core-platform`, `iam-service`, `ai-domain`, `ai-application`, `ai-infrastructure` y backend.

Backend validado:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=0
```

Evidencia HTTP:

- `POST /api/auth/login`: HTTP `200`.
- `POST /api/energyops/analyze-reading` normal: HTTP `201`, sin anomalia.
- `POST /api/energyops/analyze-reading` anomalica: HTTP `201`, severidad `HIGH`.
- `GET /api/analytics/anomalies`: HTTP `200`, con explicacion dinamica de IA.

Explicacion dinamica observada en Analytics:

```text
Se uso baseline dinamico calculado con 7 lecturas historicas. Lectura=270.00 kWh, esperado=147.71 kWh, desviacion=82.79%, zScore=1.79, severidad=HIGH.
```

Frontend validado:

```bash
cd frontend
npm run dev -- --host 127.0.0.1
```

Resultado:

- Vite dev server: HTTP `200` en `http://127.0.0.1:5173/`.
- Proxy frontend hacia backend en `8080`: `POST /api/auth/login` HTTP `200`.

Conclusion: la arquitectura queda limpia, funcional y alineada al ejemplo enterprise del docente para un MVP universitario, con migraciones reales y sin romper contratos externos.

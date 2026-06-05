# ai-application

Modulo de aplicacion para la IA hibrida explicable.

Contiene:

- caso de uso `AnalyzeEnergyWithAiUseCase`
- servicio orquestador `AnalyzeEnergyWithAiService`
- command/response de aplicacion
- puerto de historial `EnergyHistoryProviderPort`
- puerto de motor `EnergyAiEnginePort`

Restricciones:

- Depende de `ai-domain`.
- No depende de Spring.
- No depende de JPA.
- No depende del backend principal.
- No accede a SQL Server.

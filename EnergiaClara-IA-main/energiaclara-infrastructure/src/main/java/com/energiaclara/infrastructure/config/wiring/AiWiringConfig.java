package com.energiaclara.infrastructure.config.wiring;

import com.energiaclara.ai.application.AnalyzeEnergyWithAiService;
import com.energiaclara.ai.application.port.in.AnalyzeEnergyWithAiUseCase;
import com.energiaclara.ai.application.port.out.EnergyAiEnginePort;
import com.energiaclara.ai.application.port.out.EnergyHistoryProviderPort;
import com.energiaclara.ai.infrastructure.DefaultEnergyAiEngineAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cableado manual del bounded context AI (hexagonal, Spring-free en sus módulos).
 * El historial real lo provee {@code JpaEnergyHistoryProviderAdapter} (bean @Component).
 */
@Configuration
public class AiWiringConfig {

    @Bean
    public EnergyAiEnginePort energyAiEnginePort() {
        return new DefaultEnergyAiEngineAdapter();
    }

    @Bean
    public AnalyzeEnergyWithAiUseCase analyzeEnergyWithAiUseCase(EnergyHistoryProviderPort historyProvider,
                                                                 EnergyAiEnginePort aiEnginePort) {
        return new AnalyzeEnergyWithAiService(historyProvider, aiEnginePort);
    }
}

package com.energiaclara.ai.infrastructure;

import com.energiaclara.ai.application.AnalyzeEnergyWithAiService;
import com.energiaclara.ai.application.port.in.AnalyzeEnergyWithAiUseCase;
import com.energiaclara.ai.domain.EnergyAiHistoricalReading;

import java.util.List;

public final class ManualAiServiceFactory {

    private ManualAiServiceFactory() {
    }

    public static AnalyzeEnergyWithAiUseCase demoUseCase(List<EnergyAiHistoricalReading> demoHistory) {
        return new AnalyzeEnergyWithAiService(
                InMemoryEnergyHistoryProviderAdapter.demo(demoHistory),
                new DefaultEnergyAiEngineAdapter()
        );
    }

    public static AnalyzeEnergyWithAiUseCase emptyHistoryUseCase() {
        return new AnalyzeEnergyWithAiService(
                InMemoryEnergyHistoryProviderAdapter.empty(),
                new DefaultEnergyAiEngineAdapter()
        );
    }
}


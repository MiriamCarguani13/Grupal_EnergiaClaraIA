package com.energiaclara.ai.application.port.in;

import com.energiaclara.ai.application.EnergyAiAnalysisCommand;
import com.energiaclara.ai.application.EnergyAiAnalysisResponse;

public interface AnalyzeEnergyWithAiUseCase {
    EnergyAiAnalysisResponse analyze(EnergyAiAnalysisCommand command);
}


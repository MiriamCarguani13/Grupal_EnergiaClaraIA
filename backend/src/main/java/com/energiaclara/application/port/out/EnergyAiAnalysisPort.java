package com.energiaclara.application.port.out;

import com.energiaclara.application.energyops.dto.EnergyAiAnalysisOutcome;
import com.energiaclara.application.energyops.dto.EnergyAiAnalysisRequest;

public interface EnergyAiAnalysisPort {
    EnergyAiAnalysisOutcome analyze(EnergyAiAnalysisRequest request);
}


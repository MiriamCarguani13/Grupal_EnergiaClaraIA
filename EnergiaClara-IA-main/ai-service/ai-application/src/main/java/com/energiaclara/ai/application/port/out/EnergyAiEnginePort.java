package com.energiaclara.ai.application.port.out;

import com.energiaclara.ai.domain.EnergyAiHistoricalReading;
import com.energiaclara.ai.domain.EnergyAiInput;
import com.energiaclara.ai.domain.EnergyAiResult;

import java.util.List;

public interface EnergyAiEnginePort {
    EnergyAiResult analyze(EnergyAiInput input, List<EnergyAiHistoricalReading> history);
}


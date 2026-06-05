package com.energiaclara.ai.infrastructure;

import com.energiaclara.ai.application.port.out.EnergyAiEnginePort;
import com.energiaclara.ai.domain.EnergyAiHistoricalReading;
import com.energiaclara.ai.domain.EnergyAiInput;
import com.energiaclara.ai.domain.EnergyAiResult;
import com.energiaclara.ai.domain.HybridEnergyAiEngine;

import java.util.List;

public class DefaultEnergyAiEngineAdapter implements EnergyAiEnginePort {

    @Override
    public EnergyAiResult analyze(EnergyAiInput input, List<EnergyAiHistoricalReading> history) {
        return HybridEnergyAiEngine.analyze(input, history);
    }
}


package com.energiaclara.application.port.out;

import com.energiaclara.application.energyops.dto.EnergyAiPredictionInput;
import com.energiaclara.application.energyops.dto.EnergyAiPredictionResult;

import java.util.Optional;

public interface EnergyAiPredictionPort {
    Optional<EnergyAiPredictionResult> predict(EnergyAiPredictionInput input);
}

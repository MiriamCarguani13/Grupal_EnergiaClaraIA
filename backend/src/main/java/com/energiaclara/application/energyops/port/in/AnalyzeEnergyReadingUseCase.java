package com.energiaclara.application.energyops.port.in;

import com.energiaclara.application.energyops.dto.AnalyzeEnergyReadingCommand;
import com.energiaclara.application.energyops.dto.AnalyzeEnergyReadingResult;

public interface AnalyzeEnergyReadingUseCase {
    AnalyzeEnergyReadingResult analyze(AnalyzeEnergyReadingCommand command);
}

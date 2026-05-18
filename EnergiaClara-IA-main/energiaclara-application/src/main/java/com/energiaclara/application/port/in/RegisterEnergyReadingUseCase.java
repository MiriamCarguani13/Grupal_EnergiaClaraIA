package com.energiaclara.application.port.in;

import com.energiaclara.application.usecase.RegisterEnergyReadingCommand;
import com.energiaclara.core.domain.energy.EnergyReadingId;

public interface RegisterEnergyReadingUseCase {
    EnergyReadingId register(RegisterEnergyReadingCommand command);
}

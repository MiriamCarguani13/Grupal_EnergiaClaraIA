package com.energiaclara.application.energyops.port.out;

import com.energiaclara.application.energyops.dto.EnergyReadingRecord;

public interface SaveEnergyReadingPort {
    EnergyReadingRecord save(EnergyReadingRecord reading);
}

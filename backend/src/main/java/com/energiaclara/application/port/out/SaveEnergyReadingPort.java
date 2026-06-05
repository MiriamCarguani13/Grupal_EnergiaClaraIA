package com.energiaclara.application.port.out;

import com.energiaclara.application.energyops.dto.EnergyReadingRecord;

public interface SaveEnergyReadingPort {
    EnergyReadingRecord save(EnergyReadingRecord reading);
}

package com.energiaclara.application.energyops.port.out;

import com.energiaclara.application.energyops.dto.EnergyAnomalyRecord;

public interface SaveEnergyAnomalyPort {
    EnergyAnomalyRecord save(EnergyAnomalyRecord anomaly);
}

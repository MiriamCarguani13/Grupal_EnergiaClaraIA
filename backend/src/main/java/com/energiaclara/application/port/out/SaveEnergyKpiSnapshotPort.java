package com.energiaclara.application.port.out;

import com.energiaclara.application.energyops.dto.EnergyKpiSnapshotRecord;

public interface SaveEnergyKpiSnapshotPort {
    void save(EnergyKpiSnapshotRecord snapshot);
}

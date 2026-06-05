package com.energiaclara.infrastructure.persistence.adapter;

import com.energiaclara.application.energyops.dto.EnergyKpiSnapshotRecord;
import com.energiaclara.application.port.out.SaveEnergyKpiSnapshotPort;
import org.springframework.stereotype.Component;

@Component
public class EnergyKpiSnapshotPersistenceAdapter implements SaveEnergyKpiSnapshotPort {

    @Override
    public void save(EnergyKpiSnapshotRecord snapshot) {
        // MVP: KPI snapshots are calculated on-the-fly from readings, anomalies and baseline.
        // The canonical SQL Server schema does not include a persisted KPI snapshot table.
    }
}

package com.energiaclara.application.port.out;

import com.energiaclara.application.energyops.dto.EnergyReadingRecord;

import java.util.List;

public interface LoadKpiSnapshotsPort {
    List<EnergyReadingRecord> loadRecentReadings();
}

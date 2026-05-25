package com.energiaclara.application.analytics.port.out;

import com.energiaclara.application.energyops.dto.EnergyReadingRecord;

import java.util.List;

public interface LoadKpiSnapshotsPort {
    List<EnergyReadingRecord> loadRecentReadings();
}

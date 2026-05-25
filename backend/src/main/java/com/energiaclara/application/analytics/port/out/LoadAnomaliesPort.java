package com.energiaclara.application.analytics.port.out;

import com.energiaclara.application.energyops.dto.EnergyAnomalyRecord;

import java.util.List;

public interface LoadAnomaliesPort {
    List<EnergyAnomalyRecord> loadRecentAnomalies();
}

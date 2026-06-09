package com.energiaclara.ai.application.port.out;

import com.energiaclara.ai.domain.EnergyAiHistoricalReading;

import java.util.List;
import java.util.UUID;

public interface EnergyHistoryProviderPort {
    List<EnergyAiHistoricalReading> loadRecentReadings(UUID tenantId, UUID meterId, int limit);
}


package com.energiaclara.application.port.out;

import com.energiaclara.application.energyops.dto.EnergyAiPredictionResult;

import java.util.UUID;

public interface SaveAiPredictionLogPort {
    void save(UUID tenantId, UUID medidorId, EnergyAiPredictionResult result);
}

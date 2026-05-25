package com.energiaclara.application.energyops.port.out;

import com.energiaclara.application.energyops.dto.EnergyBaselineRecord;

import java.util.Optional;
import java.util.UUID;

public interface FindEnergyBaselinePort {
    Optional<EnergyBaselineRecord> findActiveByMedidorId(UUID medidorId);
}

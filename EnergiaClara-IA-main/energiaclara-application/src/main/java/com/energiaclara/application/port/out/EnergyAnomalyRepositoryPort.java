package com.energiaclara.application.port.out;

import com.energiaclara.core.domain.energy.EnergyAnomaly;

public interface EnergyAnomalyRepositoryPort {
    EnergyAnomaly save(EnergyAnomaly anomaly);
}

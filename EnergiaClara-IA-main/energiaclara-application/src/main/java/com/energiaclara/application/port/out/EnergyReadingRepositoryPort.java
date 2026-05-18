package com.energiaclara.application.port.out;

import com.energiaclara.core.domain.energy.EnergyReading;
import com.energiaclara.core.domain.energy.EnergyReadingId;

import java.util.Optional;

public interface EnergyReadingRepositoryPort {
    EnergyReading save(EnergyReading reading);

    Optional<EnergyReading> findById(EnergyReadingId readingId);
}

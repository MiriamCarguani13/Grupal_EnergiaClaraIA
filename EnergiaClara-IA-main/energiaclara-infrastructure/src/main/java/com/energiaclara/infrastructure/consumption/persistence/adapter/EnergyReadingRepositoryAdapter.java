package com.energiaclara.infrastructure.consumption.persistence.adapter;

import com.energiaclara.application.port.out.EnergyReadingRepositoryPort;
import com.energiaclara.core.domain.energy.EnergyReading;
import com.energiaclara.core.domain.energy.EnergyReadingId;
import com.energiaclara.infrastructure.consumption.persistence.repository.LecturaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * SKELETON. Implementa {@link EnergyReadingRepositoryPort} contra {@code consumo.lectura}.
 * TODO equipo Consumption: completar save() + findById() con mapper bidireccional.
 */
@Component
public class EnergyReadingRepositoryAdapter implements EnergyReadingRepositoryPort {

    private final LecturaRepository lecturaRepository;

    public EnergyReadingRepositoryAdapter(LecturaRepository lecturaRepository) {
        this.lecturaRepository = lecturaRepository;
    }

    @Override
    public EnergyReading save(EnergyReading reading) {
        // TODO equipo Consumption: mapear EnergyReading → LecturaEntity, lecturaRepository.save(), retornar reading
        throw new UnsupportedOperationException("SKELETON: implementar save() en EnergyReadingRepositoryAdapter");
    }

    @Override
    public Optional<EnergyReading> findById(EnergyReadingId readingId) {
        // TODO equipo Consumption: lecturaRepository.findById(readingId.value()).map(this::toDomain)
        throw new UnsupportedOperationException("SKELETON: implementar findById() en EnergyReadingRepositoryAdapter");
    }
}

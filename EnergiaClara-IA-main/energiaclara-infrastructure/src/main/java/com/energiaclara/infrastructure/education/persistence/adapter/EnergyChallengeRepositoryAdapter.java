package com.energiaclara.infrastructure.education.persistence.adapter;

import com.energiaclara.application.port.out.EnergyChallengeRepositoryPort;
import com.energiaclara.core.domain.challenge.EnergyChallenge;
import com.energiaclara.infrastructure.education.persistence.repository.RetoRepository;
import org.springframework.stereotype.Component;

/**
 * SKELETON. Implementa {@link EnergyChallengeRepositoryPort}.
 * TODO equipo Education: mapper bidireccional RetoEntity ↔ EnergyChallenge.
 */
@Component
public class EnergyChallengeRepositoryAdapter implements EnergyChallengeRepositoryPort {

    private final RetoRepository retoRepository;

    public EnergyChallengeRepositoryAdapter(RetoRepository retoRepository) {
        this.retoRepository = retoRepository;
    }

    @Override
    public EnergyChallenge save(EnergyChallenge challenge) {
        // TODO equipo: ChallengeJpaMapper.toEntity(challenge), retoRepository.save(entity), retornar challenge
        throw new UnsupportedOperationException("SKELETON: implementar save() en EnergyChallengeRepositoryAdapter");
    }
}

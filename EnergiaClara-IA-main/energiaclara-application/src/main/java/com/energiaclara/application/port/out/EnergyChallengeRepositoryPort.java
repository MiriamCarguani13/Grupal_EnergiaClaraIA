package com.energiaclara.application.port.out;

import com.energiaclara.core.domain.challenge.EnergyChallenge;

public interface EnergyChallengeRepositoryPort {
    EnergyChallenge save(EnergyChallenge challenge);
}

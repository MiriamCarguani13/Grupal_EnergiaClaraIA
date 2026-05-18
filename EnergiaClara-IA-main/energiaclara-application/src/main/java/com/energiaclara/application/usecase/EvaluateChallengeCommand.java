package com.energiaclara.application.usecase;

import com.energiaclara.core.domain.challenge.EnergyChallenge;
import com.energiaclara.core.domain.energy.KwhValue;
import com.energiaclara.iam.domain.AuthenticatedPrincipal;

import java.time.Instant;

public record EvaluateChallengeCommand(AuthenticatedPrincipal actor, EnergyChallenge challenge, KwhValue baselineConsumption, KwhValue actualConsumption, Instant evaluatedAt) {
}

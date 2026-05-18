package com.energiaclara.application.usecase;

import com.energiaclara.core.domain.energy.KwhValue;
import com.energiaclara.core.domain.shared.FacilityId;
import com.energiaclara.iam.domain.AuthenticatedPrincipal;

import java.time.Instant;

public record RegisterEnergyReadingCommand(AuthenticatedPrincipal actor, FacilityId facilityId, KwhValue kwhValue, Instant timestamp) {
}

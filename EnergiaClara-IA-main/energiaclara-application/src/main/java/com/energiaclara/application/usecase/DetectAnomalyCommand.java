package com.energiaclara.application.usecase;

import com.energiaclara.core.domain.energy.AnomalyType;
import com.energiaclara.core.domain.energy.EnergyReadingId;
import com.energiaclara.iam.domain.AuthenticatedPrincipal;

public record DetectAnomalyCommand(AuthenticatedPrincipal actor, EnergyReadingId readingId, AnomalyType type) {
}

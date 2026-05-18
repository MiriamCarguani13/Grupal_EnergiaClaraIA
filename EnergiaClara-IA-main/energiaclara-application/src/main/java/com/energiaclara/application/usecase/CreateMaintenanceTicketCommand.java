package com.energiaclara.application.usecase;

import com.energiaclara.core.domain.energy.AnomalyId;
import com.energiaclara.core.domain.energy.AnomalySeverity;
import com.energiaclara.core.domain.energy.KwhValue;
import com.energiaclara.core.domain.shared.FacilityId;
import com.energiaclara.iam.domain.AuthenticatedPrincipal;

public record CreateMaintenanceTicketCommand(AuthenticatedPrincipal actor, FacilityId facilityId, AnomalyId anomalyId, AnomalySeverity severity, KwhValue estimatedWaste, String description) {
}

package com.energiaclara.api.rest.maintenance;

import java.time.Instant;
import java.util.UUID;

public record MaintenanceTicketDto(
        UUID id,
        UUID anomalyId,
        String title,
        String description,
        String priority,
        String status,
        UUID assignedTo,
        Instant slaDueAt,
        boolean slaBreached,
        String technicalDiagnosis,
        String appliedSolution,
        String usedMaterials,
        String technicalNotes,
        String technicalStatus,
        Instant attendedAt,
        UUID responsibleTechnicianId,
        String evidenceImageUrl
) {}

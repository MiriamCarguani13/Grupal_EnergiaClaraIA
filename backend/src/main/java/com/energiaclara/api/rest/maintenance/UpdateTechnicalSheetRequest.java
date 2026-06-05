package com.energiaclara.api.rest.maintenance;

import jakarta.validation.constraints.Size;

public record UpdateTechnicalSheetRequest(
        @Size(max = 4000) String technicalDiagnosis,
        @Size(max = 4000) String appliedSolution,
        @Size(max = 4000) String usedMaterials,
        @Size(max = 4000) String technicalNotes,
        @Size(max = 30) String technicalStatus,
        @Size(max = 7_000_000) String evidenceImageDataUrl
) {}

package com.energiaclara.api.rest.maintenance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateMaintenanceTicketRequest(
        @NotNull UUID anomalyId,
        @NotBlank @Size(max = 300) String title,
        @Size(max = 4000) String description,
        @NotBlank @Size(max = 10) String priority,
        UUID assignedTo
) {}


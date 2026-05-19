package com.energiaclara.infrastructure.api.rest.dto;

import java.util.List;

public record DashboardResponseDto(
        long totalReadings,
        long totalAnomalies,
        long ticketsOpen,
        List<KpiItemDto> kpis,
        List<AnomalyItemDto> anomalies
) {
}

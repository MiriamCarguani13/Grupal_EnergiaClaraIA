package com.energiaclara.application.energyops.dto;

import com.energiaclara.domain.energyops.AnomalySeverity;
import com.energiaclara.domain.energyops.AnomalyType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EnergyAnomalyRecord(
        UUID id,
        UUID tenantId,
        UUID medidorId,
        UUID readingId,
        String facilityId,
        String meterId,
        Instant measuredAt,
        AnomalyType type,
        AnomalySeverity severity,
        BigDecimal puntajeScore,
        BigDecimal deviationPercent,
        String explanation,
        String recommendation,
        BigDecimal estimatedCostImpact,
        BigDecimal estimatedCo2Impact,
        boolean iaUtilizada,
        String estado
) {
}

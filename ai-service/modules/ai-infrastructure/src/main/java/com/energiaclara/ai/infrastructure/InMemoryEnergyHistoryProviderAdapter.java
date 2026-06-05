package com.energiaclara.ai.infrastructure;

import com.energiaclara.ai.application.port.out.EnergyHistoryProviderPort;
import com.energiaclara.ai.domain.EnergyAiHistoricalReading;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class InMemoryEnergyHistoryProviderAdapter implements EnergyHistoryProviderPort {

    private final List<EnergyAiHistoricalReading> readings;

    public InMemoryEnergyHistoryProviderAdapter(List<EnergyAiHistoricalReading> readings) {
        this.readings = List.copyOf(Objects.requireNonNull(readings, "readings cannot be null"));
    }

    public static InMemoryEnergyHistoryProviderAdapter empty() {
        return new InMemoryEnergyHistoryProviderAdapter(List.of());
    }

    public static InMemoryEnergyHistoryProviderAdapter demo(List<EnergyAiHistoricalReading> readings) {
        return new InMemoryEnergyHistoryProviderAdapter(readings);
    }

    @Override
    public List<EnergyAiHistoricalReading> loadRecentReadings(UUID tenantId, UUID meterId, int limit) {
        if (limit <= 0) {
            return List.of();
        }
        return readings.stream()
                .filter(reading -> matches(reading, tenantId, meterId))
                .sorted(Comparator.comparing(EnergyAiHistoricalReading::measuredAt).reversed())
                .limit(limit)
                .toList();
    }

    public List<EnergyAiHistoricalReading> allReadings() {
        return new ArrayList<>(readings);
    }

    private static boolean matches(EnergyAiHistoricalReading reading, UUID tenantId, UUID meterId) {
        boolean tenantMatches = tenantId == null || reading.tenantId() == null || tenantId.equals(reading.tenantId());
        boolean meterMatches = meterId == null || reading.meterId() == null || meterId.equals(reading.meterId());
        return tenantMatches && meterMatches;
    }
}


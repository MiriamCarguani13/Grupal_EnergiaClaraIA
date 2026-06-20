package com.energiaclara.application.port.out;

import com.energiaclara.application.energyops.dto.EnergyAnomalyRecord;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface LoadAnomaliesPort {
    List<EnergyAnomalyRecord> loadRecentAnomalies();

    default List<EnergyAnomalyRecord> loadAnomaliesByReadingIds(Collection<UUID> readingIds) {
        if (readingIds == null || readingIds.isEmpty()) {
            return List.of();
        }
        return loadRecentAnomalies().stream()
                .filter(anomaly -> anomaly.readingId() != null && readingIds.contains(anomaly.readingId()))
                .toList();
    }
}

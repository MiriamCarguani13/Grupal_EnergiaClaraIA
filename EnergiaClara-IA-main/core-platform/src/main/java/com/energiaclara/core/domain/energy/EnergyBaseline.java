package com.energiaclara.core.domain.energy;

import com.energiaclara.core.domain.shared.FacilityId;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public class EnergyBaseline {
    private final Map<FacilityId, KwhValue> expectedByFacility;
    private final KwhValue toleranceThreshold;

    public EnergyBaseline(Map<FacilityId, KwhValue> expectedByFacility, KwhValue toleranceThreshold) {
        this.expectedByFacility = Map.copyOf(Objects.requireNonNull(expectedByFacility, "La línea base no puede ser nula"));
        this.toleranceThreshold = Objects.requireNonNull(toleranceThreshold, "La tolerancia no puede ser nula");
    }

    public KwhValue getExpectedFor(FacilityId facilityId, Instant timestamp) {
        Objects.requireNonNull(facilityId, "FacilityId no puede ser nulo");
        Objects.requireNonNull(timestamp, "Timestamp no puede ser nulo");
        return expectedByFacility.getOrDefault(facilityId, new KwhValue(0));
    }

    public KwhValue getToleranceThreshold() {
        return toleranceThreshold;
    }
}

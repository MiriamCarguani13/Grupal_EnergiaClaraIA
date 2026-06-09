package com.energiaclara.ai.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record DynamicBaseline(
        BigDecimal expectedKwh,
        BigDecimal movingAverageKwh,
        BigDecimal standardDeviationKwh,
        int sampleCount,
        Source source,
        boolean fallback
) {
    public enum Source {
        HISTORY,
        STATIC_BASELINE,
        INPUT_AS_EXPECTED
    }

    public DynamicBaseline {
        Objects.requireNonNull(expectedKwh, "expectedKwh cannot be null");
        Objects.requireNonNull(movingAverageKwh, "movingAverageKwh cannot be null");
        Objects.requireNonNull(standardDeviationKwh, "standardDeviationKwh cannot be null");
        Objects.requireNonNull(source, "source cannot be null");
        if (expectedKwh.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("expectedKwh must be greater than zero");
        }
        if (sampleCount < 0) {
            throw new IllegalArgumentException("sampleCount cannot be negative");
        }
    }
}


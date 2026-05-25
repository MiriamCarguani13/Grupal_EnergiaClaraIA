package com.energiaclara.application.analytics.port.in;

import com.energiaclara.application.analytics.dto.AnomalyResult;

import java.util.List;

public interface GetAnomaliesUseCase {
    List<AnomalyResult> anomalies();
}

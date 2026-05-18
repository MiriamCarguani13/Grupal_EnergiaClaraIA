package com.energiaclara.application.port.in;

import com.energiaclara.application.usecase.DetectAnomalyCommand;
import com.energiaclara.core.domain.energy.AnomalyId;

import java.util.Optional;

public interface DetectAnomalyUseCase {
    Optional<AnomalyId> detect(DetectAnomalyCommand command);
}

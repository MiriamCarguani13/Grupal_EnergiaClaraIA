package com.energiaclara.application.port.in;

import com.energiaclara.application.energyops.dto.EnergyReadingHistoryResult;
import com.energiaclara.application.energyops.dto.EnergyReadingTrendResult;

import java.util.List;

public interface GetEnergyReadingsUseCase {
    List<EnergyReadingHistoryResult> readings();

    List<EnergyReadingTrendResult> trends();
}

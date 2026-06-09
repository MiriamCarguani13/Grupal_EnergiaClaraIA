package com.energiaclara.ai.application;

import com.energiaclara.ai.application.port.in.AnalyzeEnergyWithAiUseCase;
import com.energiaclara.ai.application.port.out.EnergyAiEnginePort;
import com.energiaclara.ai.application.port.out.EnergyHistoryProviderPort;
import com.energiaclara.ai.domain.EnergyAiHistoricalReading;
import com.energiaclara.ai.domain.EnergyAiInput;
import com.energiaclara.ai.domain.EnergyAiResult;

import java.util.List;
import java.util.Objects;

public class AnalyzeEnergyWithAiService implements AnalyzeEnergyWithAiUseCase {

    public static final int DEFAULT_HISTORY_LIMIT = 7;

    private final EnergyHistoryProviderPort historyProviderPort;
    private final EnergyAiEnginePort aiEnginePort;
    private final int historyLimit;

    public AnalyzeEnergyWithAiService(
            EnergyHistoryProviderPort historyProviderPort,
            EnergyAiEnginePort aiEnginePort
    ) {
        this(historyProviderPort, aiEnginePort, DEFAULT_HISTORY_LIMIT);
    }

    public AnalyzeEnergyWithAiService(
            EnergyHistoryProviderPort historyProviderPort,
            EnergyAiEnginePort aiEnginePort,
            int historyLimit
    ) {
        this.historyProviderPort = Objects.requireNonNull(historyProviderPort, "historyProviderPort cannot be null");
        this.aiEnginePort = Objects.requireNonNull(aiEnginePort, "aiEnginePort cannot be null");
        if (historyLimit <= 0) {
            throw new IllegalArgumentException("historyLimit must be greater than zero");
        }
        this.historyLimit = historyLimit;
    }

    @Override
    public EnergyAiAnalysisResponse analyze(EnergyAiAnalysisCommand command) {
        Objects.requireNonNull(command, "command cannot be null");

        List<EnergyAiHistoricalReading> history = historyProviderPort.loadRecentReadings(
                command.tenantId(),
                command.meterId(),
                historyLimit
        );

        EnergyAiInput input = new EnergyAiInput(
                command.tenantId(),
                command.meterId(),
                command.facilityLabel(),
                command.meterLabel(),
                command.measuredAt(),
                command.kwh(),
                command.staticBaselineKwh(),
                command.tolerancePercent(),
                command.costPerKwh(),
                command.co2KgPerKwh(),
                command.voltage(),
                command.powerFactor()
        );

        EnergyAiResult result = aiEnginePort.analyze(input, history);
        return EnergyAiAnalysisResponse.from(result);
    }
}


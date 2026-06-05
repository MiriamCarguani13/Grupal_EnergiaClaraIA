package com.energiaclara.ai.infrastructure;

import com.energiaclara.ai.application.EnergyAiAnalysisCommand;
import com.energiaclara.ai.application.EnergyAiAnalysisResponse;
import com.energiaclara.ai.application.port.in.AnalyzeEnergyWithAiUseCase;
import com.energiaclara.ai.domain.DynamicBaseline;
import com.energiaclara.ai.domain.EnergyAiHistoricalReading;
import com.energiaclara.ai.domain.HybridSeverity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiInfrastructureAdapterTest {

    private static final UUID TENANT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_TENANT_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");
    private static final UUID METER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID OTHER_METER_ID = UUID.fromString("88888888-8888-8888-8888-888888888888");

    @Test
    void inMemoryHistoryProviderFiltersAndOrdersReadings() {
        InMemoryEnergyHistoryProviderAdapter adapter = InMemoryEnergyHistoryProviderAdapter.demo(List.of(
                historical(TENANT_ID, METER_ID, "2026-06-01T08:00:00Z", "101"),
                historical(TENANT_ID, METER_ID, "2026-06-01T10:00:00Z", "102"),
                historical(TENANT_ID, OTHER_METER_ID, "2026-06-01T11:00:00Z", "999"),
                historical(OTHER_TENANT_ID, METER_ID, "2026-06-01T12:00:00Z", "888"),
                historical(TENANT_ID, METER_ID, "2026-06-01T09:00:00Z", "99")
        ));

        List<EnergyAiHistoricalReading> result = adapter.loadRecentReadings(TENANT_ID, METER_ID, 2);

        assertEquals(2, result.size());
        assertEquals(new BigDecimal("102"), result.get(0).kwh());
        assertEquals(new BigDecimal("99"), result.get(1).kwh());
    }

    @Test
    void defaultEngineAdapterDelegatesToDomainEngine() {
        DefaultEnergyAiEngineAdapter adapter = new DefaultEnergyAiEngineAdapter();

        var result = adapter.analyze(input("165"), normalHistory());

        assertFalse(result.fallback());
        assertTrue(result.anomalyScore().anomalyDetected());
        assertEquals(HybridSeverity.CRITICAL, result.anomalyScore().severity());
    }

    @Test
    void manualFactoryBuildsDemoUseCaseWithoutSpring() {
        AnalyzeEnergyWithAiUseCase useCase = ManualAiServiceFactory.demoUseCase(normalHistory());

        EnergyAiAnalysisResponse response = useCase.analyze(command("165"));

        assertFalse(response.fallback());
        assertEquals(DynamicBaseline.Source.HISTORY, response.baselineSource());
        assertTrue(response.anomalyDetected());
        assertEquals(HybridSeverity.CRITICAL, response.severity());
    }

    @Test
    void manualFactorySupportsEmptyHistoryFallback() {
        AnalyzeEnergyWithAiUseCase useCase = ManualAiServiceFactory.emptyHistoryUseCase();

        EnergyAiAnalysisResponse response = useCase.analyze(command("120"));

        assertTrue(response.fallback());
        assertEquals(DynamicBaseline.Source.STATIC_BASELINE, response.baselineSource());
        assertEquals(HybridSeverity.LOW, response.severity());
    }

    private static List<EnergyAiHistoricalReading> normalHistory() {
        return List.of(
                historical(TENANT_ID, METER_ID, "2026-06-01T07:00:00Z", "98"),
                historical(TENANT_ID, METER_ID, "2026-06-01T08:00:00Z", "101"),
                historical(TENANT_ID, METER_ID, "2026-06-01T09:00:00Z", "99"),
                historical(TENANT_ID, METER_ID, "2026-06-01T10:00:00Z", "102"),
                historical(TENANT_ID, METER_ID, "2026-06-01T11:00:00Z", "100"),
                historical(TENANT_ID, METER_ID, "2026-06-01T12:00:00Z", "97"),
                historical(TENANT_ID, METER_ID, "2026-06-01T13:00:00Z", "103")
        );
    }

    private static EnergyAiAnalysisCommand command(String kwh) {
        return new EnergyAiAnalysisCommand(
                TENANT_ID,
                METER_ID,
                "Sede Central",
                "MED-DEMO-001",
                Instant.parse("2026-06-02T12:00:00Z"),
                new BigDecimal(kwh),
                new BigDecimal("100"),
                new BigDecimal("15"),
                new BigDecimal("1.50625"),
                new BigDecimal("0.44"),
                new BigDecimal("220"),
                new BigDecimal("0.95")
        );
    }

    private static com.energiaclara.ai.domain.EnergyAiInput input(String kwh) {
        return new com.energiaclara.ai.domain.EnergyAiInput(
                TENANT_ID,
                METER_ID,
                "Sede Central",
                "MED-DEMO-001",
                Instant.parse("2026-06-02T12:00:00Z"),
                new BigDecimal(kwh),
                new BigDecimal("100"),
                new BigDecimal("15"),
                new BigDecimal("1.50625"),
                new BigDecimal("0.44"),
                new BigDecimal("220"),
                new BigDecimal("0.95")
        );
    }

    private static EnergyAiHistoricalReading historical(UUID tenantId, UUID meterId, String measuredAt, String kwh) {
        return new EnergyAiHistoricalReading(tenantId, meterId, Instant.parse(measuredAt), new BigDecimal(kwh));
    }
}


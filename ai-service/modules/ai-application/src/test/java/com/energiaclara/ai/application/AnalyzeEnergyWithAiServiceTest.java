package com.energiaclara.ai.application;

import com.energiaclara.ai.application.port.out.EnergyAiEnginePort;
import com.energiaclara.ai.application.port.out.EnergyHistoryProviderPort;
import com.energiaclara.ai.domain.DynamicBaseline;
import com.energiaclara.ai.domain.EnergyAiHistoricalReading;
import com.energiaclara.ai.domain.EnergyAiInput;
import com.energiaclara.ai.domain.EnergyAiResult;
import com.energiaclara.ai.domain.ExplainableRecommendation;
import com.energiaclara.ai.domain.HybridEnergyAiEngine;
import com.energiaclara.ai.domain.HybridSeverity;
import com.energiaclara.ai.domain.StatisticalAnomalyScore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnalyzeEnergyWithAiServiceTest {

    private static final UUID TENANT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID METER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Test
    void orchestratesHistoryAndDomainEngine() {
        EnergyHistoryProviderPort historyProvider = (tenantId, meterId, limit) -> List.of(
                historical("2026-06-01T07:00:00Z", "98"),
                historical("2026-06-01T08:00:00Z", "101"),
                historical("2026-06-01T09:00:00Z", "99"),
                historical("2026-06-01T10:00:00Z", "102"),
                historical("2026-06-01T11:00:00Z", "100"),
                historical("2026-06-01T12:00:00Z", "97"),
                historical("2026-06-01T13:00:00Z", "103")
        );
        EnergyAiEnginePort engine = HybridEnergyAiEngine::analyze;
        AnalyzeEnergyWithAiService service = new AnalyzeEnergyWithAiService(historyProvider, engine);

        EnergyAiAnalysisResponse response = service.analyze(command("165"));

        assertFalse(response.fallback());
        assertTrue(response.anomalyDetected());
        assertEquals(DynamicBaseline.Source.HISTORY, response.baselineSource());
        assertEquals(HybridSeverity.CRITICAL, response.severity());
        assertEquals(HybridEnergyAiEngine.MODEL_VERSION, response.modelVersion());
    }

    @Test
    void respectsDomainFallbackWhenHistoryIsInsufficient() {
        EnergyHistoryProviderPort historyProvider = (tenantId, meterId, limit) -> List.of(
                historical("2026-06-01T07:00:00Z", "98")
        );
        AnalyzeEnergyWithAiService service = new AnalyzeEnergyWithAiService(historyProvider, HybridEnergyAiEngine::analyze);

        EnergyAiAnalysisResponse response = service.analyze(command("120"));

        assertTrue(response.fallback());
        assertEquals(DynamicBaseline.Source.STATIC_BASELINE, response.baselineSource());
        assertEquals(new BigDecimal("100.00"), response.expectedKwh());
        assertEquals(HybridSeverity.LOW, response.severity());
        assertTrue(response.explanation().contains("No hay historial suficiente"));
    }

    @Test
    void canUseCustomEnginePortInTests() {
        EnergyHistoryProviderPort historyProvider = (tenantId, meterId, limit) -> List.of();
        EnergyAiEnginePort fakeEngine = (input, history) -> new EnergyAiResult(
                new DynamicBaseline(
                        new BigDecimal("100.00"),
                        new BigDecimal("100.00"),
                        BigDecimal.ZERO.setScale(2),
                        history.size(),
                        DynamicBaseline.Source.STATIC_BASELINE,
                        true
                ),
                new StatisticalAnomalyScore(
                        BigDecimal.ZERO.setScale(2),
                        BigDecimal.ZERO.setScale(2),
                        BigDecimal.ZERO.setScale(2),
                        BigDecimal.ZERO.setScale(2),
                        HybridSeverity.NORMAL,
                        new BigDecimal("0.50"),
                        false,
                        BigDecimal.ZERO.setScale(2),
                        BigDecimal.ZERO.setScale(2)
                ),
                new ExplainableRecommendation("fake explanation", "fake recommendation"),
                true,
                "fake-model"
        );
        AnalyzeEnergyWithAiService service = new AnalyzeEnergyWithAiService(historyProvider, fakeEngine);

        EnergyAiAnalysisResponse response = service.analyze(command("100"));

        assertEquals("fake-model", response.modelVersion());
        assertEquals("fake explanation", response.explanation());
        assertEquals("fake recommendation", response.recommendation());
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

    private static EnergyAiHistoricalReading historical(String measuredAt, String kwh) {
        return new EnergyAiHistoricalReading(TENANT_ID, METER_ID, Instant.parse(measuredAt), new BigDecimal(kwh));
    }
}


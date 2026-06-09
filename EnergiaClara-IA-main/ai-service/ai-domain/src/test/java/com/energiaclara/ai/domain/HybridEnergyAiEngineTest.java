package com.energiaclara.ai.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HybridEnergyAiEngineTest {

    private static final UUID TENANT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID METER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Test
    void usesDynamicBaselineWhenHistoryIsEnough() {
        EnergyAiInput input = input("165");
        List<EnergyAiHistoricalReading> history = List.of(
                historical("2026-06-01T07:00:00Z", "98"),
                historical("2026-06-01T08:00:00Z", "101"),
                historical("2026-06-01T09:00:00Z", "99"),
                historical("2026-06-01T10:00:00Z", "102"),
                historical("2026-06-01T11:00:00Z", "100"),
                historical("2026-06-01T12:00:00Z", "97"),
                historical("2026-06-01T13:00:00Z", "103")
        );

        EnergyAiResult result = HybridEnergyAiEngine.analyze(input, history);

        assertFalse(result.fallback());
        assertEquals(DynamicBaseline.Source.HISTORY, result.baseline().source());
        assertEquals(new BigDecimal("100.00"), result.baseline().expectedKwh());
        assertTrue(result.anomalyScore().anomalyDetected());
        assertEquals(HybridSeverity.CRITICAL, result.anomalyScore().severity());
        assertTrue(result.explainableRecommendation().explanation().contains("baseline dinamico"));
        assertEquals(HybridEnergyAiEngine.MODEL_VERSION, result.modelVersion());
    }

    @Test
    void fallsBackToStaticBaselineWhenHistoryIsInsufficient() {
        EnergyAiInput input = input("120");
        List<EnergyAiHistoricalReading> history = List.of(
                historical("2026-06-01T07:00:00Z", "98"),
                historical("2026-06-01T08:00:00Z", "101")
        );

        EnergyAiResult result = HybridEnergyAiEngine.analyze(input, history);

        assertTrue(result.fallback());
        assertEquals(DynamicBaseline.Source.STATIC_BASELINE, result.baseline().source());
        assertEquals(new BigDecimal("100.00"), result.baseline().expectedKwh());
        assertEquals(new BigDecimal("20.00"), result.anomalyScore().deviationPercent());
        assertEquals(HybridSeverity.LOW, result.anomalyScore().severity());
        assertTrue(result.explainableRecommendation().explanation().contains("No hay historial suficiente"));
    }

    @Test
    void returnsNormalWhenConsumptionIsWithinTolerance() {
        EnergyAiInput input = input("101");
        List<EnergyAiHistoricalReading> history = List.of(
                historical("2026-06-01T07:00:00Z", "100"),
                historical("2026-06-01T08:00:00Z", "101"),
                historical("2026-06-01T09:00:00Z", "99"),
                historical("2026-06-01T10:00:00Z", "102"),
                historical("2026-06-01T11:00:00Z", "100")
        );

        EnergyAiResult result = HybridEnergyAiEngine.analyze(input, history);

        assertFalse(result.fallback());
        assertFalse(result.anomalyScore().anomalyDetected());
        assertEquals(HybridSeverity.NORMAL, result.anomalyScore().severity());
        assertTrue(result.explainableRecommendation().recommendation().contains("monitoreo regular"));
    }

    private static EnergyAiInput input(String kwh) {
        return new EnergyAiInput(
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

package com.energiaclara.application.energyops.service;

import com.energiaclara.application.energyops.dto.AnalyzeEnergyReadingCommand;
import com.energiaclara.application.energyops.dto.AnalyzeEnergyReadingResult;
import com.energiaclara.application.energyops.dto.EnergyAiAnalysisOutcome;
import com.energiaclara.application.energyops.dto.EnergyAnomalyRecord;
import com.energiaclara.application.energyops.dto.EnergyBaselineRecord;
import com.energiaclara.application.energyops.dto.EnergyReadingRecord;
import com.energiaclara.application.port.out.EnergyAiAnalysisPort;
import com.energiaclara.application.port.out.FindEnergyBaselinePort;
import com.energiaclara.application.port.out.SaveEnergyAnomalyPort;
import com.energiaclara.application.port.out.SaveEnergyReadingPort;
import com.energiaclara.domain.energyops.AnomalySeverity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class EnergyAnalysisServiceTest {

    private static final UUID TENANT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID METER_UUID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID USER_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Test
    void usesHybridAiWhenHistoryIsEnoughAndAnomalyIsDetected() {
        AtomicReference<EnergyAnomalyRecord> savedAnomaly = new AtomicReference<>();
        EnergyAnalysisService service = serviceWithAi(
                request -> new EnergyAiAnalysisOutcome(
                        true,
                        false,
                        true,
                        AnomalySeverity.HIGH,
                        new BigDecimal("65.00"),
                        new BigDecimal("0.90"),
                        new BigDecimal("0.87"),
                        new BigDecimal("3.10"),
                        new BigDecimal("100.00"),
                        7,
                        "HISTORY",
                        new BigDecimal("97.91"),
                        new BigDecimal("28.60"),
                        "Explicacion IA dinamica con zScore.",
                        "Recomendacion IA dinamica.",
                        "hybrid-stat-rules-v1.0"
                ),
                savedAnomaly
        );

        AnalyzeEnergyReadingResult result = service.analyze(command(new BigDecimal("165")));

        assertThat(result.anomalyDetected()).isTrue();
        assertThat(result.severity()).isEqualTo(AnomalySeverity.HIGH);
        assertThat(result.recommendation()).isEqualTo("Recomendacion IA dinamica.");
        assertThat(savedAnomaly.get().iaUtilizada()).isTrue();
        assertThat(savedAnomaly.get().explanation()).contains("IA dinamica");
    }

    @Test
    void keepsDeterministicFallbackWhenAiReportsFallback() {
        AtomicReference<EnergyAnomalyRecord> savedAnomaly = new AtomicReference<>();
        EnergyAnalysisService service = serviceWithAi(
                request -> new EnergyAiAnalysisOutcome(
                        false,
                        true,
                        true,
                        AnomalySeverity.CRITICAL,
                        new BigDecimal("999.00"),
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        new BigDecimal("100.00"),
                        1,
                        "STATIC_BASELINE",
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        "No hay historial suficiente.",
                        "No usar esta recomendacion IA.",
                        "hybrid-stat-rules-v1.0"
                ),
                savedAnomaly
        );

        AnalyzeEnergyReadingResult result = service.analyze(command(new BigDecimal("120")));

        assertThat(result.anomalyDetected()).isTrue();
        assertThat(result.severity()).isEqualTo(AnomalySeverity.LOW);
        assertThat(result.deviationPercent()).isEqualByComparingTo("20.00");
        assertThat(result.recommendation()).isEqualTo("Revisar equipos activos fuera de horario o consumo superior al baseline.");
        assertThat(savedAnomaly.get().iaUtilizada()).isFalse();
        assertThat(savedAnomaly.get().explanation()).contains("historial suficiente");
    }

    @Test
    void returnsNormalReadingWithoutAnomalyWhenAiFindsNormalPattern() {
        AtomicReference<EnergyAnomalyRecord> savedAnomaly = new AtomicReference<>();
        EnergyAnalysisService service = serviceWithAi(
                request -> new EnergyAiAnalysisOutcome(
                        true,
                        false,
                        false,
                        null,
                        new BigDecimal("5.00"),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        new BigDecimal("100.00"),
                        7,
                        "HISTORY",
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        "Consumo normal segun historial.",
                        "Continuar monitoreo regular.",
                        "hybrid-stat-rules-v1.0"
                ),
                savedAnomaly
        );

        AnalyzeEnergyReadingResult result = service.analyze(command(new BigDecimal("105")));

        assertThat(result.anomalyDetected()).isFalse();
        assertThat(result.severity()).isNull();
        assertThat(result.recommendation()).isEqualTo("Continuar monitoreo regular.");
        assertThat(savedAnomaly.get()).isNull();
    }

    private static EnergyAnalysisService serviceWithAi(
            EnergyAiAnalysisPort aiPort,
            AtomicReference<EnergyAnomalyRecord> savedAnomaly
    ) {
        SaveEnergyReadingPort saveReading = reading -> new EnergyReadingRecord(
                UUID.randomUUID(),
                reading.tenantId(),
                reading.medidorId(),
                reading.registradaPor(),
                reading.facilityId(),
                reading.meterId(),
                reading.measuredAt(),
                reading.periodoInicio(),
                reading.kwh(),
                reading.voltage(),
                reading.powerFactor()
        );
        FindEnergyBaselinePort baseline = medidorId -> Optional.of(new EnergyBaselineRecord(
                UUID.randomUUID(),
                medidorId,
                new BigDecimal("100"),
                new BigDecimal("15")
        ));
        SaveEnergyAnomalyPort saveAnomaly = anomaly -> {
            EnergyAnomalyRecord saved = new EnergyAnomalyRecord(
                    UUID.randomUUID(),
                    anomaly.tenantId(),
                    anomaly.medidorId(),
                    anomaly.readingId(),
                    anomaly.facilityId(),
                    anomaly.meterId(),
                    anomaly.measuredAt(),
                    anomaly.type(),
                    anomaly.severity(),
                    anomaly.puntajeScore(),
                    anomaly.deviationPercent(),
                    anomaly.explanation(),
                    anomaly.recommendation(),
                    anomaly.estimatedCostImpact(),
                    anomaly.estimatedCo2Impact(),
                    anomaly.iaUtilizada(),
                    anomaly.modelVersion(),
                    anomaly.estado(),
                    anomaly.ticketId(),
                    anomaly.responsibleTechnicianId(),
                    anomaly.responsibleTechnicianName(),
                    anomaly.resolvedAt()
            );
            savedAnomaly.set(saved);
            return saved;
        };
        return new EnergyAnalysisService(
                saveReading,
                baseline,
                saveAnomaly,
                aiPort,
                TENANT_ID,
                METER_UUID,
                USER_ID,
                new BigDecimal("100"),
                new BigDecimal("15"),
                new BigDecimal("1.50625"),
                new BigDecimal("0.44")
        );
    }

    private static AnalyzeEnergyReadingCommand command(BigDecimal kwh) {
        return new AnalyzeEnergyReadingCommand(
                "Sede Central",
                "MED-DEMO-001",
                Instant.parse("2026-06-02T12:00:00Z"),
                kwh,
                new BigDecimal("220"),
                new BigDecimal("0.95")
        );
    }
}

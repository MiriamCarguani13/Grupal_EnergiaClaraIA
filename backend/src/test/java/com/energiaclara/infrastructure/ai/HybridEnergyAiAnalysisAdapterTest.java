package com.energiaclara.infrastructure.ai;

import com.energiaclara.application.energyops.dto.EnergyAiAnalysisOutcome;
import com.energiaclara.application.energyops.dto.EnergyAiAnalysisRequest;
import com.energiaclara.domain.energyops.AnomalySeverity;
import com.energiaclara.infrastructure.persistence.entity.EnergyReadingEntity;
import com.energiaclara.infrastructure.persistence.repository.EnergyReadingRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HybridEnergyAiAnalysisAdapterTest {

    private static final UUID TENANT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID METER_UUID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Test
    void adaptsRealHistoryToAiService() {
        EnergyReadingRepository repository = mock(EnergyReadingRepository.class);
        when(repository.findTop7ByTenantIdAndMedidorIdOrderByMeasuredAtDesc(TENANT_ID, METER_UUID))
                .thenReturn(List.of(
                        reading("2026-06-01T07:00:00Z", "98"),
                        reading("2026-06-01T08:00:00Z", "101"),
                        reading("2026-06-01T09:00:00Z", "99"),
                        reading("2026-06-01T10:00:00Z", "102"),
                        reading("2026-06-01T11:00:00Z", "100"),
                        reading("2026-06-01T12:00:00Z", "97"),
                        reading("2026-06-01T13:00:00Z", "103")
                ));

        HybridEnergyAiAnalysisAdapter adapter = new HybridEnergyAiAnalysisAdapter(repository);

        EnergyAiAnalysisOutcome outcome = adapter.analyze(request(new BigDecimal("165")));

        assertThat(outcome.aiUsed()).isTrue();
        assertThat(outcome.fallback()).isFalse();
        assertThat(outcome.anomalyDetected()).isTrue();
        assertThat(outcome.severity()).isEqualTo(AnomalySeverity.CRITICAL);
        assertThat(outcome.explanation()).contains("baseline dinamico");
    }

    @Test
    void reportsFallbackWhenRepositoryHistoryIsInsufficient() {
        EnergyReadingRepository repository = mock(EnergyReadingRepository.class);
        when(repository.findTop7ByTenantIdAndMedidorIdOrderByMeasuredAtDesc(TENANT_ID, METER_UUID))
                .thenReturn(List.of(reading("2026-06-01T07:00:00Z", "98")));

        HybridEnergyAiAnalysisAdapter adapter = new HybridEnergyAiAnalysisAdapter(repository);

        EnergyAiAnalysisOutcome outcome = adapter.analyze(request(new BigDecimal("120")));

        assertThat(outcome.aiUsed()).isFalse();
        assertThat(outcome.fallback()).isTrue();
        assertThat(outcome.explanation()).contains("No hay historial suficiente");
    }

    private static EnergyAiAnalysisRequest request(BigDecimal kwh) {
        return new EnergyAiAnalysisRequest(
                TENANT_ID,
                METER_UUID,
                "Sede Central",
                "MED-DEMO-001",
                Instant.parse("2026-06-02T12:00:00Z"),
                kwh,
                new BigDecimal("100"),
                new BigDecimal("15"),
                new BigDecimal("1.50625"),
                new BigDecimal("0.44"),
                new BigDecimal("220"),
                new BigDecimal("0.95")
        );
    }

    private static EnergyReadingEntity reading(String measuredAt, String kwh) {
        EnergyReadingEntity entity = new EnergyReadingEntity();
        entity.setTenantId(TENANT_ID);
        entity.setMedidorId(METER_UUID);
        entity.setMeasuredAt(Instant.parse(measuredAt));
        entity.setKwh(new BigDecimal(kwh));
        return entity;
    }
}


package com.energiaclara.application;

import com.energiaclara.application.analytics.dto.AnalyticsDashboardMetricsResult;
import com.energiaclara.application.port.in.GetAnalyticsDashboardUseCase;
import com.energiaclara.application.port.out.LoadAnalyticsDashboardPort;
import com.energiaclara.application.port.out.LoadAnomaliesPort;
import com.energiaclara.application.port.out.LoadKpiSnapshotsPort;
import com.energiaclara.application.analytics.service.AnalyticsQueryService;
import com.energiaclara.application.energyops.dto.EnergyAnomalyRecord;
import com.energiaclara.application.energyops.dto.EnergyBaselineRecord;
import com.energiaclara.application.energyops.dto.EnergyReadingRecord;
import com.energiaclara.application.port.in.AnalyzeEnergyReadingUseCase;
import com.energiaclara.application.port.out.FindEnergyBaselinePort;
import com.energiaclara.application.port.out.SaveEnergyAnomalyPort;
import com.energiaclara.application.port.out.SaveEnergyReadingPort;
import com.energiaclara.application.energyops.service.EnergyAnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationServicesSmokeTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues(
                    "app.energyops.demo-tenant-id=11111111-1111-1111-1111-111111111111",
                    "app.energyops.demo-medidor-id=33333333-3333-3333-3333-333333333333",
                    "app.energyops.demo-user-id=44444444-4444-4444-4444-444444444444",
                    "app.energyops.demo-default-baseline-kwh=100",
                    "app.energyops.demo-default-tolerance-percent=15",
                    "app.energyops.cost-per-kwh=1.50625",
                    "app.energyops.co2-kg-per-kwh=0.44"
            )
            .withBean(SaveEnergyReadingPort.class, () -> reading -> new EnergyReadingRecord(
                    reading.id() != null ? reading.id() : UUID.randomUUID(),
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
            ))
            .withBean(FindEnergyBaselinePort.class, () -> medidorId -> Optional.of(new EnergyBaselineRecord(
                    UUID.randomUUID(),
                    medidorId,
                    BigDecimal.valueOf(100),
                    BigDecimal.valueOf(15)
            )))
            .withBean(SaveEnergyAnomalyPort.class, () -> anomaly -> new EnergyAnomalyRecord(
                    anomaly.id() != null ? anomaly.id() : UUID.randomUUID(),
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
                    anomaly.estado()
            ))
            .withBean(LoadAnalyticsDashboardPort.class, () -> () -> new AnalyticsDashboardMetricsResult(0, 0))
            .withBean(LoadKpiSnapshotsPort.class, () -> List::of)
            .withBean(LoadAnomaliesPort.class, () -> List::of)
            .withBean(EnergyAnalysisService.class)
            .withBean(AnalyticsQueryService.class);

    @Test
    void applicationUseCasesAreWired() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AnalyzeEnergyReadingUseCase.class);
            assertThat(context).hasSingleBean(GetAnalyticsDashboardUseCase.class);
        });
    }
}

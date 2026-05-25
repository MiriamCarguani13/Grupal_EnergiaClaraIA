package com.energiaclara.infrastructure.persistence.adapter;

import com.energiaclara.application.analytics.dto.AnalyticsDashboardMetricsResult;
import com.energiaclara.application.port.out.LoadAnalyticsDashboardPort;
import com.energiaclara.infrastructure.persistence.repository.EnergyAnomalyRepository;
import com.energiaclara.infrastructure.persistence.repository.EnergyReadingRepository;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsDashboardPersistenceAdapter implements LoadAnalyticsDashboardPort {

    private final EnergyReadingRepository readingRepository;
    private final EnergyAnomalyRepository anomalyRepository;

    public AnalyticsDashboardPersistenceAdapter(EnergyReadingRepository readingRepository,
                                                EnergyAnomalyRepository anomalyRepository) {
        this.readingRepository = readingRepository;
        this.anomalyRepository = anomalyRepository;
    }

    @Override
    public AnalyticsDashboardMetricsResult loadDashboardMetrics() {
        return new AnalyticsDashboardMetricsResult(
                readingRepository.count(),
                anomalyRepository.count()
        );
    }
}

package com.energiaclara.infrastructure.ai;

import com.energiaclara.ai.application.port.out.EnergyHistoryProviderPort;
import com.energiaclara.ai.domain.EnergyAiHistoricalReading;
import com.energiaclara.infrastructure.consumption.persistence.entity.LecturaEntity;
import com.energiaclara.infrastructure.consumption.persistence.repository.LecturaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Adapter de salida AI: alimenta el motor híbrido con el historial real de lecturas
 * (tabla {@code consumo.lectura}) para el cálculo del baseline dinámico.
 *
 * <p>Vive en {@code infrastructure.ai} (no en un bounded context) para no acoplar
 * EnergyOps con Consumption: el cruce de datos se hace en la capa AI neutral (ADR-009).</p>
 */
@Component
public class JpaEnergyHistoryProviderAdapter implements EnergyHistoryProviderPort {

    private final LecturaRepository lecturaRepository;

    public JpaEnergyHistoryProviderAdapter(LecturaRepository lecturaRepository) {
        this.lecturaRepository = lecturaRepository;
    }

    @Override
    public List<EnergyAiHistoricalReading> loadRecentReadings(UUID tenantId, UUID meterId, int limit) {
        if (tenantId == null || meterId == null || limit <= 0) {
            return List.of();
        }
        return lecturaRepository
                .findByInquilinoIdAndMedidorIdOrderByPeriodoFinDesc(tenantId, meterId, PageRequest.of(0, limit))
                .stream()
                .map(JpaEnergyHistoryProviderAdapter::toHistoricalReading)
                .toList();
    }

    private static EnergyAiHistoricalReading toHistoricalReading(LecturaEntity entity) {
        return new EnergyAiHistoricalReading(
                entity.getInquilinoId(),
                entity.getMedidorId(),
                entity.getPeriodoFin(),
                entity.getValor()
        );
    }
}

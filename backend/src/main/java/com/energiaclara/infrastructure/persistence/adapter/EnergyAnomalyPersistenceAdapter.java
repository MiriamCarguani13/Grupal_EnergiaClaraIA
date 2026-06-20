package com.energiaclara.infrastructure.persistence.adapter;

import com.energiaclara.application.port.out.LoadAnomaliesPort;
import com.energiaclara.application.energyops.dto.EnergyAnomalyRecord;
import com.energiaclara.application.port.out.SaveEnergyAnomalyPort;
import com.energiaclara.domain.energyops.AnomalySeverity;
import com.energiaclara.domain.energyops.AnomalyType;
import com.energiaclara.infrastructure.persistence.entity.EnergyAnomalyEntity;
import com.energiaclara.infrastructure.persistence.repository.EnergyAnomalyRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Collection;
import java.util.UUID;

@Component
public class EnergyAnomalyPersistenceAdapter implements SaveEnergyAnomalyPort, LoadAnomaliesPort {

    private static final List<String> VISIBLE_STATUSES = List.of("DETECTADA", "DERIVADA", "EN_ATENCION", "RESUELTA");

    private final EnergyAnomalyRepository anomalyRepository;

    public EnergyAnomalyPersistenceAdapter(EnergyAnomalyRepository anomalyRepository) {
        this.anomalyRepository = anomalyRepository;
    }

    @Override
    public EnergyAnomalyRecord save(EnergyAnomalyRecord anomaly) {
        return toRecord(anomalyRepository.save(toEntity(anomaly)));
    }

    @Override
    public List<EnergyAnomalyRecord> loadRecentAnomalies() {
        return anomalyRepository.findTop20ByEstadoInOrderByMeasuredAtDesc(VISIBLE_STATUSES).stream()
                .map(this::toRecord)
                .toList();
    }

    @Override
    public List<EnergyAnomalyRecord> loadAnomaliesByReadingIds(Collection<UUID> readingIds) {
        if (readingIds == null || readingIds.isEmpty()) {
            return List.of();
        }
        return anomalyRepository.findByReadingIdIn(readingIds).stream()
                .map(this::toRecord)
                .toList();
    }

    private EnergyAnomalyEntity toEntity(EnergyAnomalyRecord record) {
        EnergyAnomalyEntity entity = new EnergyAnomalyEntity();
        entity.setId(record.id());
        entity.setTenantId(record.tenantId());
        entity.setMedidorId(record.medidorId());
        entity.setReadingId(record.readingId());
        entity.setFacilityId(record.facilityId());
        entity.setMeterId(record.meterId());
        entity.setMeasuredAt(record.measuredAt());
        entity.setType(toSqlType(record.type()));
        entity.setSeverity(toSqlSeverity(record.severity()));
        entity.setPuntajeScore(record.puntajeScore());
        entity.setDeviationPercent(record.deviationPercent());
        entity.setExplanation(record.explanation());
        entity.setRecommendation(record.recommendation());
        entity.setEstimatedCostImpact(record.estimatedCostImpact());
        entity.setEstimatedCo2Impact(record.estimatedCo2Impact());
        entity.setIaUtilizada(record.iaUtilizada());
        entity.setVersionModeloIa(record.modelVersion());
        entity.setEstado(toSqlStatus(record.estado()));
        entity.setTicketId(record.ticketId());
        entity.setResponsibleTechnicianId(record.responsibleTechnicianId());
        entity.setResponsibleTechnicianName(record.responsibleTechnicianName());
        entity.setResolvedAt(record.resolvedAt());
        return entity;
    }

    private EnergyAnomalyRecord toRecord(EnergyAnomalyEntity entity) {
        return new EnergyAnomalyRecord(
                entity.getId(),
                entity.getTenantId(),
                entity.getMedidorId(),
                entity.getReadingId(),
                entity.getFacilityId(),
                entity.getMeterId(),
                entity.getMeasuredAt(),
                fromSqlType(entity.getType()),
                fromSqlSeverity(entity.getSeverity()),
                entity.getPuntajeScore(),
                entity.getDeviationPercent(),
                entity.getExplanation(),
                entity.getRecommendation(),
                entity.getEstimatedCostImpact(),
                entity.getEstimatedCo2Impact(),
                entity.isIaUtilizada(),
                entity.getVersionModeloIa(),
                entity.getEstado(),
                entity.getTicketId(),
                entity.getResponsibleTechnicianId(),
                entity.getResponsibleTechnicianName(),
                entity.getResolvedAt()
        );
    }

    private String toSqlType(AnomalyType type) {
        if (type == null) {
            return "OTRO";
        }
        return switch (type) {
            case EXCESS_CONSUMPTION -> "PICO";
            case LOW_POWER_FACTOR, VOLTAGE_OUT_OF_RANGE -> "OTRO";
        };
    }

    private AnomalyType fromSqlType(String type) {
        if ("PICO".equals(type)) {
            return AnomalyType.EXCESS_CONSUMPTION;
        }
        return AnomalyType.EXCESS_CONSUMPTION;
    }

    private String toSqlSeverity(AnomalySeverity severity) {
        if (severity == null) {
            return "BAJA";
        }
        return switch (severity) {
            case LOW -> "BAJA";
            case MEDIUM -> "MEDIA";
            case HIGH -> "ALTA";
            case CRITICAL -> "CRITICA";
        };
    }

    private AnomalySeverity fromSqlSeverity(String severity) {
        return switch (severity) {
            case "CRITICA" -> AnomalySeverity.CRITICAL;
            case "ALTA" -> AnomalySeverity.HIGH;
            case "MEDIA" -> AnomalySeverity.MEDIUM;
            case "BAJA" -> AnomalySeverity.LOW;
            default -> AnomalySeverity.LOW;
        };
    }

    private String toSqlStatus(String status) {
        if ("ABIERTA".equals(status) || "OPEN".equals(status) || status == null) {
            return "DETECTADA";
        }
        return status;
    }
}

package com.energiaclara.infrastructure.energyops.persistence.adapter;

import com.energiaclara.ai.application.EnergyAiAnalysisResponse;
import com.energiaclara.ai.domain.HybridSeverity;
import com.energiaclara.infrastructure.energyops.persistence.entity.AnomaliaEntity;
import com.energiaclara.infrastructure.energyops.persistence.repository.AnomaliaRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Persiste en {@code energiaops.anomalia} el resultado del motor AI híbrido cuando
 * se detecta una anomalía. Mapea {@link EnergyAiAnalysisResponse} → {@link AnomaliaEntity}
 * conservando score, desviación, explicación, recomendación e impacto cost/CO2.
 *
 * <p>Vive en el contexto EnergyOps (no cruza a otros contextos infra) → ADR-009.</p>
 */
@Component
public class EnergyOpsAnomalyWriter {

    private final AnomaliaRepository anomaliaRepository;

    public EnergyOpsAnomalyWriter(AnomaliaRepository anomaliaRepository) {
        this.anomaliaRepository = anomaliaRepository;
    }

    /** Inserta la anomalía detectada y devuelve su id generado. */
    public UUID persistDetected(UUID tenantId, UUID meterId, EnergyAiAnalysisResponse r) {
        AnomaliaEntity e = new AnomaliaEntity();
        e.setAnomaliaId(UUID.randomUUID());
        e.setInquilinoId(tenantId);
        e.setMedidorId(meterId);
        e.setLecturaId(null);                       // análisis directo: sin lectura persistida
        e.setTipoAnomalia("PICO");
        e.setSeveridad(mapSeveridad(r.severity()));
        e.setPuntajeScore(r.anomalyScore());
        e.setPorcentajeDesviacion(r.deviationPercent());
        e.setExplicacion(truncate(r.explanation(), 1000));
        e.setIaUtilizada(true);
        e.setVersionModeloIa(r.modelVersion());
        e.setEstado("DETECTADA");
        e.setDetectadaEl(Instant.now());
        e.setRecomendacion(truncate(r.recommendation(), 700));
        e.setCostoEstimado(r.estimatedCostImpact());
        e.setCo2Estimado(r.estimatedCo2Impact());
        anomaliaRepository.save(e);
        return e.getAnomaliaId();
    }

    private static String mapSeveridad(HybridSeverity s) {
        return switch (s) {
            case CRITICAL -> "CRITICA";
            case HIGH -> "ALTA";
            case MEDIUM -> "MEDIA";
            case LOW, NORMAL -> "BAJA";
        };
    }

    private static String truncate(String v, int max) {
        if (v == null) {
            return null;
        }
        return v.length() <= max ? v : v.substring(0, max);
    }
}

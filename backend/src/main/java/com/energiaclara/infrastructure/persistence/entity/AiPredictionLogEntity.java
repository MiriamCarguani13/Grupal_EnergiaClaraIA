package com.energiaclara.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "log_prediccion_ia", schema = "energiaops")
public class AiPredictionLogEntity {

    @Id
    @Column(name = "log_id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(name = "inquilino_id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID tenantId;

    @Column(name = "medidor_id", nullable = false, columnDefinition = "uniqueidentifier")
    private UUID medidorId;

    @Column(name = "version_modelo", nullable = false, length = 50)
    private String modelVersion;

    @Column(name = "hash_entrada", nullable = false, length = 64)
    private String inputHash;

    @Column(name = "hash_salida", nullable = false, length = 64)
    private String outputHash;

    @Column(name = "valor_predicho", precision = 18, scale = 4)
    private BigDecimal predictedKwh;

    @Column(name = "confianza", precision = 5, scale = 4)
    private BigDecimal confidence;

    @Column(name = "anomalia_detectada", nullable = false)
    private boolean anomalyDetected;

    @Column(name = "latencia_ms")
    private Integer latencyMs;

    @Column(name = "llamada_el", nullable = false)
    private Instant calledAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (calledAt == null) {
            calledAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getMedidorId() {
        return medidorId;
    }

    public void setMedidorId(UUID medidorId) {
        this.medidorId = medidorId;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getInputHash() {
        return inputHash;
    }

    public void setInputHash(String inputHash) {
        this.inputHash = inputHash;
    }

    public String getOutputHash() {
        return outputHash;
    }

    public void setOutputHash(String outputHash) {
        this.outputHash = outputHash;
    }

    public BigDecimal getPredictedKwh() {
        return predictedKwh;
    }

    public void setPredictedKwh(BigDecimal predictedKwh) {
        this.predictedKwh = predictedKwh;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
    }

    public boolean isAnomalyDetected() {
        return anomalyDetected;
    }

    public void setAnomalyDetected(boolean anomalyDetected) {
        this.anomalyDetected = anomalyDetected;
    }

    public Integer getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Integer latencyMs) {
        this.latencyMs = latencyMs;
    }

    public Instant getCalledAt() {
        return calledAt;
    }

    public void setCalledAt(Instant calledAt) {
        this.calledAt = calledAt;
    }
}

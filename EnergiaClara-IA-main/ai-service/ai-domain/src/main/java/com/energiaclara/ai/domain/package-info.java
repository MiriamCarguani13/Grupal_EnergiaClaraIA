/**
 * Dominio puro del bounded context AI.
 *
 * Aggregates, value objects, domain events, invariantes.
 * Framework-agnostic. Sin Spring, sin JPA, sin Jackson.
 *
 * Estructura sugerida:
 * <ul>
 *   <li>{@code aggregates} — AnomalyDetection, BaselineCalculation, PredictionResult</li>
 *   <li>{@code valueobjects} — AnomalyScore, ConfidenceLevel, ModelVersion</li>
 *   <li>{@code events} — AnomalyPredicted, BaselineRecomputed</li>
 *   <li>{@code services} — AnomalyDetectionService (lógica cross-aggregate)</li>
 * </ul>
 *
 * @see com.energiaclara.core.domain.shared Tipos compartidos (TenantId, KwhValue, FacilityId)
 */
package com.energiaclara.ai.domain;

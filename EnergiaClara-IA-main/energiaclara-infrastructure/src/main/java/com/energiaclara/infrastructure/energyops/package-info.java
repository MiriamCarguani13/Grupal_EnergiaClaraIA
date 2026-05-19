/**
 * Bounded context <b>EnergyOps</b> — detección de anomalías + baselines (core domain).
 *
 * <p>Aggregate Fase 2 §2.2.3: <code>Anomaly</code>.</p>
 *
 * <p>Estados: DETECTED → NOTIFIED → IN_ACTION → RESOLVED|IGNORED.</p>
 *
 * <p>Tablas DB: <code>energiaops.anomalia</code>, <code>energiaops.snapshot_linea_base</code>,
 * <code>energiaops.log_prediccion_ia</code>.</p>
 *
 * <p>Skeleton para equipo. Ver README.md.</p>
 */
package com.energiaclara.infrastructure.energyops;

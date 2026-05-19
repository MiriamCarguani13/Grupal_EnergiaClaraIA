/**
 * Bounded context <b>Education</b> — retos energéticos + rankings + medallas.
 *
 * <p>Aggregate Fase 2 §2.2.5: <code>Challenge</code>.</p>
 *
 * <p>Estados: CREATED → ASSIGNED → IN_PROGRESS → COMPLETED|EXPIRED.</p>
 *
 * <p>Tablas DB: <code>educacion.reto</code>, <code>educacion.progreso_reto</code>,
 * <code>educacion.medalla</code>, <code>educacion.medalla_usuario</code>, <code>educacion.snapshot_ranking</code>.</p>
 */
package com.energiaclara.infrastructure.education;

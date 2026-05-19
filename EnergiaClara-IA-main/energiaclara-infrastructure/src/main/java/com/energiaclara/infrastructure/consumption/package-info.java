/**
 * Bounded context <b>Consumption</b> — punto de medición y registro de lecturas energéticas.
 *
 * <p>Aggregates Fase 2 §2.2.1-2: <code>Meter</code>, <code>Reading</code>.</p>
 *
 * <p>Endpoints REST:
 * <ul>
 *   <li>POST /api/energyops/readings — registrar lectura</li>
 *   <li>GET /api/energyops/readings — listar lecturas (filtrado tenant)</li>
 * </ul>
 *
 * <p>Tablas DB: <code>consumo.lectura</code>, <code>consumo.importacion_lectura</code>,
 * <code>core.medidor</code>, <code>core.edificio</code>.</p>
 *
 * <p>Skeleton para equipo. Ver README.md.</p>
 */
package com.energiaclara.infrastructure.consumption;

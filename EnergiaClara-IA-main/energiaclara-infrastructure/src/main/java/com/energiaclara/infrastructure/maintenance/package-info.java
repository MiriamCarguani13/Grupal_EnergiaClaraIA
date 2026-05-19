/**
 * Bounded context <b>Maintenance</b> — tickets de mantenimiento + evidencias.
 *
 * <p>Aggregate Fase 2 §2.2.4: <code>Ticket</code>.</p>
 *
 * <p>Estados: BORRADOR → ABIERTO → ASIGNADO → EN_PROCESO → CERRADO|REABIERTO.</p>
 *
 * <p>Tablas DB: <code>mantenimiento.ticket</code>, <code>mantenimiento.evidencia_ticket</code>,
 * <code>mantenimiento.historial_asignacion_ticket</code>, <code>mantenimiento.politica_sla</code>.</p>
 */
package com.energiaclara.infrastructure.maintenance;

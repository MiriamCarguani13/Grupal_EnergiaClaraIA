import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import AppLayout from '../components/AppLayout'
import { fetchDashboard } from '../services/analyticsService'
import { getMaintenanceTickets } from '../services/maintenanceService'

const PERIODS = [
  { id: 'today', label: 'Hoy' },
  { id: 'week', label: 'Semana' },
  { id: 'month', label: 'Mes' },
  { id: 'year', label: 'Año' },
]

function formatNumber(n) {
  if (n === null || n === undefined || n === '') return 'Sin datos'
  const value = typeof n === 'string' ? parseFloat(n) : n
  if (Number.isNaN(value)) return 'Sin datos'
  return value.toLocaleString('es-BO', { maximumFractionDigits: 2 })
}

function parseNumber(n) {
  const value = typeof n === 'string' ? parseFloat(n) : n
  return Number.isFinite(value) ? value : 0
}

function severityBadge(severity) {
  const map = {
    CRITICAL: 'critica',
    HIGH: 'alta',
    MEDIUM: 'media',
    LOW: 'baja',
  }
  return map[severity] || 'media'
}

function severityLabel(severity) {
  const map = { CRITICAL: 'Crítica', HIGH: 'Alta', MEDIUM: 'Media', LOW: 'Baja' }
  return map[severity] || severity || 'Sin severidad'
}

function normalizeTicketStatus(status) {
  return (status || '').toUpperCase()
}

function isTicketOpen(ticket) {
  const status = normalizeTicketStatus(ticket.status || ticket.estado)
  return !['CERRADO', 'REPARADO', 'RESUELTO', 'CLOSED'].includes(status)
}

function getPeriodStart(period) {
  const now = new Date()
  const start = new Date(now)
  start.setHours(0, 0, 0, 0)
  if (period === 'week') start.setDate(now.getDate() - 7)
  if (period === 'month') start.setMonth(now.getMonth() - 1)
  if (period === 'year') start.setFullYear(now.getFullYear() - 1)
  return start
}

function getItemDate(item, fields) {
  const value = fields.map((field) => item?.[field]).find(Boolean)
  if (!value) return null
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? null : date
}

function filterByPeriod(items, period, fields) {
  const start = getPeriodStart(period)
  return (items || []).filter((item) => {
    const date = getItemDate(item, fields)
    return !date || date >= start
  })
}

function KpiCard({ tone, label, value, unit, detail }) {
  return (
    <article className={`executive-kpi-card ${tone || ''}`}>
      <div className="kpi-label">{label}</div>
      <div className="kpi-value">
        {value}
        {unit && <span className="kpi-unit">{unit}</span>}
      </div>
      <div className="kpi-sub">{detail}</div>
    </article>
  )
}

function PeriodSelector({ value, onChange }) {
  return (
    <div className="period-selector" aria-label="Selector de período">
      {PERIODS.map((period) => (
        <button
          key={period.id}
          type="button"
          className={value === period.id ? 'active' : ''}
          onClick={() => onChange(period.id)}
        >
          {period.label}
        </button>
      ))}
    </div>
  )
}

function ConsumptionHistoryChart({ readings }) {
  const series = readings
    .filter((item) => item?.measuredAt)
    .slice(0, 8)
    .reverse()
  const hasEnoughData = series.length >= 2
  const maxValue = Math.max(
    1,
    ...series.flatMap((item) => [
      parseNumber(item.kwh),
      parseNumber(item.baselineKwh ?? item.expectedKwh ?? item.baseline),
    ])
  )

  return (
    <section className="control-card consumption-history-card">
      <div className="control-card-header">
        <div>
          <span className="section-kicker">Serie temporal</span>
          <h2>Historial de Consumo Energético</h2>
        </div>
        <span className="data-source-pill">Consumo real vs baseline esperado</span>
      </div>

      {!hasEnoughData ? (
        <div className="empty-state consumption-empty">
          El historial aparecerá cuando existan lecturas suficientes.
        </div>
      ) : (
        <>
          <div className="consumption-chart">
            {series.map((item, idx) => {
              const baseline = item.baselineKwh ?? item.expectedKwh ?? item.baseline
              const consumptionPct = Math.max(8, (parseNumber(item.kwh) / maxValue) * 100)
              const baselinePct = Math.max(8, (parseNumber(baseline) / maxValue) * 100)
              return (
                <div className="consumption-point" key={item.id || idx}>
                  <div className="consumption-bars">
                    <span
                      className="consumption-bar real"
                      style={{ height: `${consumptionPct}%` }}
                      title={`Consumo real: ${formatNumber(item.kwh)} kWh`}
                    />
                    <span
                      className="consumption-bar baseline"
                      style={{ height: `${baselinePct}%` }}
                      title={`Baseline esperado: ${formatNumber(baseline)} kWh`}
                    />
                  </div>
                  <span className="consumption-label">
                    {new Date(item.measuredAt).toLocaleDateString('es-BO', { day: '2-digit', month: 'short' })}
                  </span>
                </div>
              )
            })}
          </div>

          <div className="chart-legend">
            <span><i className="legend-dot real" /> Consumo real</span>
            <span><i className="legend-dot baseline" /> Baseline esperado</span>
          </div>
        </>
      )}
    </section>
  )
}

function RecentActivity({ readings, anomalies, tickets }) {
  const events = useMemo(() => {
    const readingEvents = readings.slice(0, 3).map((reading) => ({
      id: `reading-${reading.id || reading.measuredAt}`,
      type: 'Lectura registrada',
      title: `${formatNumber(reading.kwh)} kWh`,
      detail: reading.facilityId || reading.meterId || 'Consumo energético actualizado',
      date: reading.measuredAt,
    }))
    const anomalyEvents = anomalies.slice(0, 3).map((anomaly) => ({
      id: `anomaly-${anomaly.id}`,
      type: 'Anomalía detectada',
      title: `${severityLabel(anomaly.severity)} · ${formatNumber(anomaly.deviationPercent)}%`,
      detail: anomaly.meterId || anomaly.facilityId || 'Desviación energética',
      date: anomaly.measuredAt,
    }))
    const ticketEvents = (tickets || []).slice(0, 4).map((ticket) => {
      const status = normalizeTicketStatus(ticket.status || ticket.estado)
      const resolved = ['CERRADO', 'REPARADO', 'RESUELTO', 'CLOSED'].includes(status)
      return {
        id: `ticket-${ticket.id}`,
        type: resolved ? 'Ticket resuelto' : 'Ticket generado',
        title: ticket.title || ticket.titulo || 'Ticket de mantenimiento',
        detail: ticket.status || ticket.estado || 'En seguimiento',
        date: ticket.attendedAt || ticket.resolvedAt || ticket.createdAt || ticket.slaDueAt,
      }
    })

    return [...readingEvents, ...anomalyEvents, ...ticketEvents]
      .sort((a, b) => new Date(b.date || 0) - new Date(a.date || 0))
      .slice(0, 6)
  }, [readings, anomalies, tickets])

  return (
    <section className="control-card">
      <div className="control-card-header">
        <div>
          <span className="section-kicker">Operación</span>
          <h2>Actividad reciente</h2>
        </div>
      </div>
      {events.length === 0 ? (
        <div className="empty-state">Sin actividad reciente disponible.</div>
      ) : (
        <div className="activity-list">
          {events.map((event) => (
            <div className="activity-item" key={event.id}>
              <span className="activity-dot" />
              <div>
                <strong>{event.type}</strong>
                <p>{event.title}</p>
                <small>{event.detail}</small>
              </div>
              <time>{event.date ? new Date(event.date).toLocaleDateString('es-BO') : 'Sin fecha'}</time>
            </div>
          ))}
        </div>
      )}
    </section>
  )
}

function LatestAnomalies({ anomalies }) {
  return (
    <section className="control-card">
      <div className="control-card-header">
        <div>
          <span className="section-kicker">IA híbrida</span>
          <h2>Últimas anomalías</h2>
        </div>
        <Link to="/anomalias" className="btn btn-secondary">Ver todas</Link>
      </div>

      {anomalies.length === 0 ? (
        <div className="empty-state">Sin anomalías recientes.</div>
      ) : (
        <div className="latest-anomaly-list">
          {anomalies.slice(0, 5).map((a) => (
            <article className="latest-anomaly-item" key={a.id}>
              <div>
                <span className="anomaly-card-label">Medidor</span>
                <strong>{a.meterId || 'Sin medidor'}</strong>
                <p>{a.facilityId || 'Área no registrada'}</p>
              </div>
              <span className={`badge ${severityBadge(a.severity)}`}>{severityLabel(a.severity)}</span>
              <span className="badge info">{a.status || 'DETECTADA'}</span>
              <strong>{formatNumber(a.deviationPercent)}%</strong>
              <Link to={`/anomalias/${a.id}`} className="btn btn-secondary">Ver detalle</Link>
            </article>
          ))}
        </div>
      )}
    </section>
  )
}

function SeverityDistribution({ anomalies }) {
  const severityCounts = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW'].map((severity) => ({
    severity,
    count: anomalies.filter((a) => a.severity === severity).length,
  }))
  const max = Math.max(1, ...severityCounts.map((item) => item.count))

  return (
    <article className="analytics-panel-card">
      <div>
        <span className="section-kicker">Riesgo</span>
        <h3>Anomalías por severidad</h3>
      </div>
      <div className="severity-bars">
        {severityCounts.map((item) => (
          <div className="severity-row" key={item.severity}>
            <span className={`badge ${severityBadge(item.severity)}`}>{severityLabel(item.severity)}</span>
            <div className="severity-track">
              <i style={{ width: `${Math.max(8, (item.count / max) * 100)}%` }} />
            </div>
            <strong>{item.count}</strong>
          </div>
        ))}
      </div>
    </article>
  )
}

function TopAnomalyMeters({ anomalies }) {
  const meters = useMemo(() => {
    const criticalAnomalies = anomalies.filter((anomaly) => ['CRITICAL', 'HIGH'].includes(anomaly.severity))
    const source = criticalAnomalies.length > 0 ? criticalAnomalies : anomalies
    const counts = source.reduce((acc, anomaly) => {
      const key = anomaly.meterId || 'Sin medidor'
      acc[key] = (acc[key] || 0) + 1
      return acc
    }, {})

    return Object.entries(counts)
      .map(([meter, count]) => ({ meter, count }))
      .sort((a, b) => b.count - a.count)
      .slice(0, 5)
  }, [anomalies])

  return (
    <article className="analytics-panel-card">
      <div>
        <span className="section-kicker">Priorización</span>
        <h3>Top medidores críticos</h3>
      </div>
      {meters.length === 0 ? (
        <div className="empty-state">Sin medidores con anomalías.</div>
      ) : (
        <div className="meter-ranking-list">
          {meters.map((item, index) => (
            <div className="meter-ranking-item" key={item.meter}>
              <span>{index + 1}</span>
              <strong>{item.meter}</strong>
              <em>{item.count}</em>
            </div>
          ))}
        </div>
      )}
    </article>
  )
}

function AnalyticalPanel({ anomalies, estimatedImpact, estimatedSavings, estimatedCo2 }) {

  return (
    <section className="control-card analytics-panel">
      <div className="control-card-header">
        <div>
          <span className="section-kicker">Inteligencia operativa</span>
          <h2>Panel Analítico</h2>
        </div>
      </div>

      <div className="analytics-panel-grid">
        <SeverityDistribution anomalies={anomalies} />
        <TopAnomalyMeters anomalies={anomalies} />
        <article className="analytics-panel-card impact">
          <div>
            <span className="section-kicker">Impacto</span>
            <h3>Impacto estimado</h3>
          </div>
          <strong className="analytics-big-number">
            {estimatedImpact > 0 ? `${formatNumber(estimatedImpact)} kWh` : 'Sin datos'}
          </strong>
          <p>{estimatedCo2 > 0 ? `${formatNumber(estimatedCo2)} kg CO2 estimados.` : 'Impacto energético consolidado desde anomalías.'}</p>
        </article>
        <article className="analytics-panel-card savings">
          <div>
            <span className="section-kicker">Ahorro</span>
            <h3>Ahorro estimado</h3>
          </div>
          <strong className="analytics-big-number">
            {anomalies.length > 0 ? `Bs. ${formatNumber(estimatedSavings)}` : 'Sin datos'}
          </strong>
          <p>Calculado desde el impacto económico reportado por las anomalías analizadas.</p>
        </article>
      </div>
    </section>
  )
}

export default function DashboardKpisPage() {
  const [data, setData] = useState(null)
  const [tickets, setTickets] = useState(null)
  const [period, setPeriod] = useState('month')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let mounted = true
    Promise.all([
      fetchDashboard(),
      getMaintenanceTickets().catch(() => null),
    ])
      .then(([dashboard, maintenanceTickets]) => {
        if (!mounted) return
        setData(dashboard)
        setTickets(maintenanceTickets)
      })
      .catch((err) => {
        if (mounted) setError(err.response?.data?.message || 'Error cargando dashboard')
      })
      .finally(() => {
        if (mounted) setLoading(false)
      })
    return () => {
      mounted = false
    }
  }, [])

  if (loading) return <AppLayout title="Centro de Control"><p>Cargando...</p></AppLayout>
  if (error) return <AppLayout title="Centro de Control"><div className="alert alert-danger">{error}</div></AppLayout>

  const allReadings = data?.kpis || []
  const allAnomalies = data?.anomalies || []
  const allTickets = Array.isArray(tickets) ? tickets : []
  const readings = filterByPeriod(allReadings, period, ['measuredAt', 'createdAt'])
  const anomalies = filterByPeriod(allAnomalies, period, ['measuredAt', 'createdAt', 'resolvedAt'])
  const filteredTickets = filterByPeriod(allTickets, period, ['attendedAt', 'resolvedAt', 'createdAt', 'slaDueAt'])
  const activeAnomalies = anomalies.filter((a) => (a.status || 'DETECTADA') !== 'RESUELTA')
  const totalKwh = readings.reduce((s, k) => s + parseNumber(k.kwh), 0)
  const estimatedSavings = anomalies.reduce((s, a) => s + parseNumber(a.estimatedCostImpact), 0)
  const estimatedImpact = anomalies.reduce((s, a) => s + parseNumber(a.estimatedEnergyImpact ?? a.energyImpactKwh), 0)
  const estimatedCo2 = anomalies.reduce((s, a) => s + parseNumber(a.estimatedCo2Impact), 0)
  const openTickets = tickets === null ? null : filteredTickets.filter(isTicketOpen).length

  return (
    <AppLayout title="Centro de Control">
      <section className="dashboard-hero control-hero">
        <div>
          <span className="section-kicker">Plataforma energética institucional</span>
          <h1>Centro de Control Energético</h1>
          <p>Supervisión inteligente del consumo energético institucional.</p>
        </div>
        <div className="hero-control-stack">
          <PeriodSelector value={period} onChange={setPeriod} />
          <div className="hero-status-card">
            <span className="live-dot"></span>
            <div>
              <strong>Operación en línea</strong>
              <p>Vista filtrada por período seleccionado.</p>
            </div>
          </div>
        </div>
      </section>

      <div className="executive-kpi-grid">
        <KpiCard
          tone="energy"
          label="Consumo total"
          value={readings.length > 0 ? formatNumber(totalKwh) : 'Sin datos'}
          unit={readings.length > 0 ? 'kWh' : null}
          detail={`${data?.totalReadings ?? 0} lecturas registradas`}
        />
        <KpiCard
          tone="alerting"
          label="Anomalías activas"
          value={activeAnomalies.length}
          detail={`${anomalies.length} anomalías recientes analizadas`}
        />
        <KpiCard
          tone="savings"
          label="Ahorro estimado"
          value={anomalies.length > 0 ? `Bs. ${formatNumber(estimatedSavings)}` : 'Sin datos'}
          detail="Calculado desde impacto económico de anomalías"
        />
        <KpiCard
          tone="tickets"
          label="Tickets abiertos"
          value={openTickets === null ? 'Sin datos' : openTickets}
          detail={openTickets === null ? 'Mantenimiento no disponible' : 'Tickets de mantenimiento activos'}
        />
      </div>

      <ConsumptionHistoryChart readings={readings} />

      <AnalyticalPanel
        anomalies={anomalies}
        estimatedImpact={estimatedImpact}
        estimatedSavings={estimatedSavings}
        estimatedCo2={estimatedCo2}
      />

      <LatestAnomalies anomalies={anomalies} />

      <RecentActivity readings={readings} anomalies={anomalies} tickets={filteredTickets} />
    </AppLayout>
  )
}

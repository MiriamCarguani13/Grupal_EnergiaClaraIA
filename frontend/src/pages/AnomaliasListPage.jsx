import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import AppLayout from '../components/AppLayout'
import { fetchAnomalies } from '../services/analyticsService'

const SEV_BADGE = { CRITICAL: 'critica', HIGH: 'alta', MEDIUM: 'media', LOW: 'baja' }
const SEV_LABEL = { CRITICAL: 'Crítica', HIGH: 'Alta', MEDIUM: 'Media', LOW: 'Baja' }
const STATUS_LABEL = {
  DETECTADA: 'Detectada',
  DERIVADA: 'Derivada',
  EN_ATENCION: 'En atención',
  RESUELTA: 'Resuelta',
}
const STATUS_OPTIONS = ['DETECTADA', 'DERIVADA', 'EN_ATENCION', 'RESUELTA']
const SEVERITY_OPTIONS = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']
const ACTIVE_STATUSES = ['DETECTADA', 'DERIVADA', 'EN_ATENCION']

function formatNumber(value, decimals = 2) {
  if (value === null || value === undefined || value === '') return 'No disponible'
  const numeric = Number(value)
  return Number.isFinite(numeric) ? numeric.toFixed(decimals) : value
}

function formatDate(value) {
  if (!value) return 'No disponible'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? 'No disponible' : date.toLocaleString('es-BO')
}

function estimatedConsumption(anomaly) {
  const baseline = Number(anomaly.expectedKwh)
  const deviation = Number(anomaly.deviationPercent)
  if (!Number.isFinite(baseline) || !Number.isFinite(deviation)) return null
  return baseline * (1 + deviation / 100)
}

function normalizeStatus(status) {
  return status || 'DETECTADA'
}

function shortRecommendation(value) {
  if (!value) return 'Sin recomendación disponible.'
  return value.length > 135 ? `${value.slice(0, 132)}...` : value
}

function SummaryCard({ label, value, tone }) {
  return (
    <article className={`anomaly-summary-card ${tone || ''}`}>
      <span>{label}</span>
      <strong>{value}</strong>
    </article>
  )
}

export default function AnomaliasListPage() {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [filters, setFilters] = useState({
    status: '',
    severity: '',
    meter: '',
    date: '',
  })

  useEffect(() => {
    fetchAnomalies()
      .then(setItems)
      .catch((err) => setError(err.response?.data?.message || 'Error cargando anomalías'))
      .finally(() => setLoading(false))
  }, [])

  const meters = useMemo(() => {
    return [...new Set(items.map((a) => a.meterId).filter(Boolean))].sort()
  }, [items])

  const filteredItems = useMemo(() => {
    return items.filter((a) => {
      const status = normalizeStatus(a.status)
      const measuredAt = a.measuredAt ? new Date(a.measuredAt).toISOString().slice(0, 10) : ''
      return (!filters.status || status === filters.status)
        && (!filters.severity || a.severity === filters.severity)
        && (!filters.meter || a.meterId === filters.meter)
        && (!filters.date || measuredAt === filters.date)
    })
  }, [filters, items])

  const summary = useMemo(() => {
    const highOrCritical = items.filter((a) => ['HIGH', 'CRITICAL'].includes(a.severity)).length
    const inAttention = items.filter((a) => ['DERIVADA', 'EN_ATENCION'].includes(normalizeStatus(a.status))).length
    const resolved = items.filter((a) => normalizeStatus(a.status) === 'RESUELTA').length
    return { total: items.length, highOrCritical, inAttention, resolved }
  }, [items])

  const handleFilterChange = (e) => {
    setFilters((current) => ({ ...current, [e.target.name]: e.target.value }))
  }

  const resetFilters = () => setFilters({ status: '', severity: '', meter: '', date: '' })

  return (
    <AppLayout title="Anomalías Detectadas">
      <section className="page-hero compact">
        <div>
          <span className="section-kicker">IA híbrida explicable</span>
          <h1>Anomalías Detectadas</h1>
          <p>Eventos de consumo energético fuera del comportamiento esperado, analizados mediante IA híbrida y reglas energéticas.</p>
        </div>
      </section>

      <section className="anomaly-summary-grid">
        <SummaryCard label="Total anomalías" value={summary.total} />
        <SummaryCard label="Críticas / Altas" value={summary.highOrCritical} tone="danger" />
        <SummaryCard label="En atención" value={summary.inAttention} tone="warning" />
        <SummaryCard label="Resueltas" value={summary.resolved} tone="success" />
      </section>

      <section className="control-card anomaly-filter-panel">
        <div className="control-card-header">
          <div>
            <span className="section-kicker">Filtros</span>
            <h2>Explorar anomalías</h2>
          </div>
          <button type="button" className="btn btn-secondary" onClick={resetFilters}>Limpiar</button>
        </div>

        <div className="anomaly-filter-grid">
          <div className="form-group">
            <label>Estado</label>
            <select name="status" value={filters.status} onChange={handleFilterChange}>
              <option value="">Todos</option>
              {STATUS_OPTIONS.map((status) => (
                <option key={status} value={status}>{STATUS_LABEL[status]}</option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label>Severidad</label>
            <select name="severity" value={filters.severity} onChange={handleFilterChange}>
              <option value="">Todas</option>
              {SEVERITY_OPTIONS.map((severity) => (
                <option key={severity} value={severity}>{SEV_LABEL[severity]}</option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label>Medidor</label>
            <select name="meter" value={filters.meter} onChange={handleFilterChange}>
              <option value="">Todos</option>
              {meters.map((meter) => <option key={meter} value={meter}>{meter}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label>Fecha</label>
            <input name="date" type="date" value={filters.date} onChange={handleFilterChange} />
          </div>
        </div>
      </section>

      {loading && <div className="control-card">Cargando anomalías...</div>}
      {error && <div className="alert alert-danger">{error}</div>}

      {!loading && !error && (
        <section className="anomaly-card-grid anomaly-card-grid-wide">
          {filteredItems.length === 0 ? (
            <div className="control-card empty-state">No hay anomalías con los filtros seleccionados.</div>
          ) : (
            filteredItems.map((anomaly) => <AnomalyCard key={anomaly.id} anomaly={anomaly} />)
          )}
        </section>
      )}
    </AppLayout>
  )
}

function AnomalyCard({ anomaly }) {
  const status = normalizeStatus(anomaly.status)
  const consumption = estimatedConsumption(anomaly)
  const canDerive = status === 'DETECTADA'

  return (
    <article className={`anomaly-card anomaly-card-premium severity-${(anomaly.severity || 'MEDIUM').toLowerCase()}`}>
      <div className="anomaly-card-top">
        <div>
          <span className="anomaly-card-label">Medidor</span>
          <h4>{anomaly.meterId || 'No disponible'}</h4>
          <p>{anomaly.facilityId || 'Área no registrada'}</p>
        </div>
        <div className="anomaly-card-badges">
          <span className={`badge ${SEV_BADGE[anomaly.severity] || 'media'}`}>{SEV_LABEL[anomaly.severity] || anomaly.severity || 'Media'}</span>
          <span className={`status-badge status-${status.toLowerCase()}`}>{STATUS_LABEL[status] || status}</span>
        </div>
      </div>

      <div className="anomaly-metrics">
        <div><span>Consumo registrado</span><strong>{formatNumber(consumption)} kWh</strong></div>
        <div><span>Baseline esperado</span><strong>{formatNumber(anomaly.expectedKwh)} kWh</strong></div>
        <div><span>Desviación</span><strong>{formatNumber(anomaly.deviationPercent)}%</strong></div>
        <div><span>Confidence IA</span><strong>{formatNumber(anomaly.confidence)}</strong></div>
      </div>

      <div className="anomaly-card-footer anomaly-card-footer-vertical">
        <span>Fecha: <strong>{formatDate(anomaly.measuredAt)}</strong></span>
        <p>{shortRecommendation(anomaly.recommendation)}</p>
      </div>

      <div className="anomaly-actions">
        <Link to={`/anomalias/${anomaly.id}`} className="btn btn-secondary">Ver detalle</Link>
        {canDerive && (
          <Link to={`/tickets/nuevo?anomalia=${anomaly.id}`} className="btn btn-primary">Derivar a mantenimiento</Link>
        )}
      </div>
    </article>
  )
}

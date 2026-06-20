import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import AppLayout from '../components/AppLayout'
import { analyzeReading, fetchEnergyReadingTrends, fetchEnergyReadings } from '../services/energyOpsService'

const TABS = [
  { id: 'registro', label: 'Registrar lectura' },
  { id: 'historial', label: 'Historial de lecturas' },
  { id: 'tendencias', label: 'Tendencias' },
]

function formatNumber(value) {
  if (value === null || value === undefined || value === '') return 'Sin datos'
  const numeric = Number(value)
  return Number.isFinite(numeric)
    ? numeric.toLocaleString('es-BO', { maximumFractionDigits: 2 })
    : 'Sin datos'
}

function formatDate(value) {
  if (!value) return 'Sin fecha'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return 'Sin fecha'
  return date.toLocaleString('es-BO', {
    day: '2-digit',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function formatShortDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  return date.toLocaleString('es-BO', {
    day: '2-digit',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function resultLabel(result) {
  if (!result || result === 'NORMAL') return 'Normal'
  return result
}

function resultBadgeClass(result) {
  if (!result || result === 'NORMAL') return 'success'
  if (result === 'LOW') return 'baja'
  if (result === 'MEDIUM') return 'media'
  return 'alta'
}

function isTrendAlert(item) {
  return item?.result !== 'NORMAL' || Number(item?.deviationPercentage) > 15
}

function estimateBaseline(result, registeredKwh) {
  const deviation = Number(result?.deviationPercent)
  const kwh = Number(registeredKwh)
  if (!Number.isFinite(deviation) || !Number.isFinite(kwh)) return null
  return kwh / (1 + deviation / 100)
}

function ReadingResultCard({ result, submittedReading }) {
  if (!result) {
    return (
      <div className="reading-result-empty">
        <span>Resultado</span>
        <strong>Esperando análisis</strong>
        <p>Al analizar una lectura verás aquí el estado, desviación y recomendación generada.</p>
      </div>
    )
  }

  const baseline = estimateBaseline(result, submittedReading?.kwh)
  const anomaly = result.anomalyDetected

  return (
    <section className={`reading-result-card ${anomaly ? 'anomaly' : 'normal'}`}>
      <div className="reading-result-header">
        <span className={`badge ${anomaly ? 'alta' : 'success'}`}>
          {anomaly ? 'Anomalía detectada' : 'Lectura normal'}
        </span>
        {anomaly && result.severity && <span className="badge info">{result.severity}</span>}
      </div>

      <div className="reading-result-grid">
        <div>
          <span>Consumo registrado</span>
          <strong>{formatNumber(submittedReading?.kwh)} kWh</strong>
        </div>
        <div>
          <span>Baseline esperado</span>
          <strong>{formatNumber(baseline)} kWh</strong>
        </div>
        <div>
          <span>Desviación</span>
          <strong>{formatNumber(result.deviationPercent)}%</strong>
        </div>
        {anomaly && (
          <div>
            <span>Confidence IA</span>
            <strong>{formatNumber(result.confidence)}</strong>
          </div>
        )}
      </div>

      <div className="reading-recommendation">
        <span>Recomendación</span>
        <p>{result.recommendation || 'Sin recomendación disponible.'}</p>
      </div>

      {anomaly && result.anomalyId && (
        <Link to={`/anomalias/${result.anomalyId}`} className="btn btn-primary">
          Ver anomalía generada
        </Link>
      )}
    </section>
  )
}

function ReadingHistoryTab({ readings, loading, error }) {
  return (
    <section className="control-card">
      <div className="control-card-header">
        <div>
          <span className="section-kicker">Lecturas</span>
          <h2>Historial de lecturas</h2>
        </div>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      <div className="reading-table-shell">
        <table>
          <thead>
            <tr>
              <th>Fecha</th>
              <th>Medidor</th>
              <th>Consumo</th>
              <th>Voltaje</th>
              <th>Factor de potencia</th>
              <th>Resultado</th>
            </tr>
          </thead>
          <tbody>
            {loading && (
              <tr>
                <td colSpan="6">
                  <div className="empty-state">Cargando historial energético...</div>
                </td>
              </tr>
            )}
            {!loading && readings.length === 0 && (
              <tr>
                <td colSpan="6">
                  <div className="empty-state">No hay historial disponible todavía.</div>
                </td>
              </tr>
            )}
            {!loading && readings.map((reading) => (
              <tr key={reading.id}>
                <td>{formatDate(reading.createdAt)}</td>
                <td>{reading.meterId || 'Sin medidor'}</td>
                <td>{formatNumber(reading.consumptionKwh)} kWh</td>
                <td>{formatNumber(reading.voltage)} V</td>
                <td>{formatNumber(reading.powerFactor)}</td>
                <td>
                  <span className={`badge ${resultBadgeClass(reading.result)}`}>
                    {resultLabel(reading.result)}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  )
}

function TrendsTab({ trends, loading, error }) {
  const hasEnoughData = trends.length >= 2
  const chartWidth = 720
  const chartHeight = 260
  const padding = { top: 24, right: 28, bottom: 42, left: 48 }
  const values = trends.flatMap((item) => [Number(item.consumptionKwh), Number(item.baselineKwh)])
    .filter(Number.isFinite)
  const minValue = values.length > 0 ? Math.min(...values) : 0
  const maxValue = values.length > 0 ? Math.max(...values) : 1
  const rangePadding = Math.max((maxValue - minValue) * 0.12, 8)
  const yMin = Math.max(0, minValue - rangePadding)
  const yMax = maxValue + rangePadding
  const yRange = Math.max(yMax - yMin, 1)
  const xStep = hasEnoughData ? (chartWidth - padding.left - padding.right) / (trends.length - 1) : 0
  const toX = (index) => padding.left + index * xStep
  const toY = (value) => padding.top + (1 - ((Number(value) || 0) - yMin) / yRange) * (chartHeight - padding.top - padding.bottom)
  const consumptionPoints = trends.map((item, index) => `${toX(index)},${toY(item.consumptionKwh)}`).join(' ')
  const baselinePoints = trends.map((item, index) => `${toX(index)},${toY(item.baselineKwh)}`).join(' ')
  const labelStep = Math.max(1, Math.ceil(trends.length / 5))
  const yTicks = [yMax, yMin + yRange / 2, yMin]

  return (
    <section className="control-card trends-empty-card">
      <div className="control-card-header">
        <div>
          <span className="section-kicker">Consumo vs baseline</span>
          <h2>Tendencias</h2>
        </div>
      </div>
      {error && <div className="alert alert-danger">{error}</div>}
      {loading && <div className="empty-state">Cargando tendencias energéticas...</div>}
      {!loading && !hasEnoughData && (
        <div className="trend-placeholder">
          <div className="trend-line trend-real" />
          <div className="trend-line trend-baseline" />
          <p>Las tendencias aparecerán cuando existan lecturas históricas suficientes.</p>
        </div>
      )}
      {!loading && hasEnoughData && (
        <div className="reading-trend-chart">
          <div className="trend-legend">
            <span><i className="legend-real" /> Consumo registrado</span>
            <span><i className="legend-baseline" /> Baseline esperado</span>
          </div>
          <div className="trend-line-chart-shell">
            <svg className="trend-line-chart" viewBox={`0 0 ${chartWidth} ${chartHeight}`} role="img" aria-label="Consumo registrado versus baseline esperado">
              {yTicks.map((tick, index) => {
                const y = toY(tick)
                return (
                  <g key={`y-${index}`}>
                    <line x1={padding.left} y1={y} x2={chartWidth - padding.right} y2={y} className="trend-grid-line" />
                    <text x={padding.left - 12} y={y + 4} className="trend-axis-label" textAnchor="end">
                      {formatNumber(tick)}
                    </text>
                  </g>
                )
              })}

              <polyline points={baselinePoints} className="trend-polyline baseline" />
              <polyline points={consumptionPoints} className="trend-polyline consumption" />

              {trends.map((item, index) => {
                const x = toX(index)
                const showLabel = index === 0 || index === trends.length - 1 || index % labelStep === 0
                return (
                  <g key={`${item.date}-${index}`}>
                    {showLabel && (
                      <text x={x} y={chartHeight - 12} className="trend-axis-label" textAnchor="middle">
                        {formatShortDate(item.date)}
                      </text>
                    )}
                    <circle cx={x} cy={toY(item.baselineKwh)} r="4" className="trend-point baseline" />
                    <circle
                      cx={x}
                      cy={toY(item.consumptionKwh)}
                      r={isTrendAlert(item) ? '7' : '5'}
                      className={`trend-point consumption ${isTrendAlert(item) ? 'alert' : ''}`}
                    >
                      <title>
                        {`${formatDate(item.date)} | Consumo ${formatNumber(item.consumptionKwh)} kWh | Baseline ${formatNumber(item.baselineKwh)} kWh | Desviación ${formatNumber(item.deviationPercentage)}%`}
                      </title>
                    </circle>
                  </g>
                )
              })}
            </svg>
          </div>
          <div className="trend-detail-strip">
            {trends.map((item, index) => (
              <article key={`detail-${item.date}-${index}`} className="trend-detail-card">
                <span>{formatShortDate(item.date)}</span>
                <strong>{formatNumber(item.consumptionKwh)} kWh</strong>
                <small>Baseline {formatNumber(item.baselineKwh)} kWh</small>
                <em className={isTrendAlert(item) ? 'alert' : ''}>{formatNumber(item.deviationPercentage)}% desv.</em>
              </article>
            ))}
          </div>
        </div>
      )}
    </section>
  )
}

export default function RegistroLecturaPage() {
  const [activeTab, setActiveTab] = useState('registro')
  const [form, setForm] = useState({
    facilityId: 'Sede Central - Bloque B - Aula 3B',
    meterId: 'MED-DEMO-001',
    measuredAt: new Date().toISOString().slice(0, 16),
    kwh: 165,
    voltage: 220,
    powerFactor: 0.95,
    observaciones: '',
  })
  const [result, setResult] = useState(null)
  const [submittedReading, setSubmittedReading] = useState(null)
  const [readings, setReadings] = useState([])
  const [trends, setTrends] = useState([])
  const [readingsLoading, setReadingsLoading] = useState(true)
  const [readingsError, setReadingsError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  const loadReadingData = async () => {
    setReadingsLoading(true)
    setReadingsError('')
    try {
      const [historyData, trendsData] = await Promise.all([
        fetchEnergyReadings(),
        fetchEnergyReadingTrends(),
      ])
      setReadings(Array.isArray(historyData) ? historyData : [])
      setTrends(Array.isArray(trendsData) ? trendsData : [])
    } catch (err) {
      setReadingsError(err.response?.data?.message || 'No se pudo cargar el historial de lecturas.')
    } finally {
      setReadingsLoading(false)
    }
  }

  useEffect(() => {
    loadReadingData()
  }, [])

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    setError('')
    setResult(null)
    try {
      const payload = {
        facilityId: form.facilityId,
        meterId: form.meterId,
        measuredAt: new Date(form.measuredAt).toISOString(),
        kwh: parseFloat(form.kwh),
        voltage: form.voltage ? parseFloat(form.voltage) : null,
        powerFactor: form.powerFactor ? parseFloat(form.powerFactor) : null,
      }
      const data = await analyzeReading(payload)
      setSubmittedReading(payload)
      setResult(data)
      await loadReadingData()
    } catch (err) {
      setError(err.response?.data?.message || 'Error registrando lectura')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AppLayout title="Lecturas Energéticas">
      <section className="page-hero compact">
        <div>
          <span className="section-kicker">EnergyOps</span>
          <h1>Lecturas Energéticas</h1>
          <p>Registra consumo, analiza desviaciones y prepara historial para tendencias energéticas.</p>
        </div>
      </section>

      <div className="energy-tabs">
        {TABS.map((tab) => (
          <button
            key={tab.id}
            type="button"
            className={activeTab === tab.id ? 'active' : ''}
            onClick={() => setActiveTab(tab.id)}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {activeTab === 'registro' && (
        <div className="reading-register-grid">
          <section className="control-card">
            <div className="control-card-header">
              <div>
                <span className="section-kicker">Nueva medición</span>
                <h2>Registrar lectura</h2>
              </div>
            </div>

            <form onSubmit={handleSubmit}>
              <div className="form-row">
                <div className="form-group">
                  <label>Medidor *</label>
                  <input name="meterId" value={form.meterId} onChange={handleChange} required />
                </div>
                <div className="form-group">
                  <label>Periodo *</label>
                  <input name="measuredAt" type="datetime-local" value={form.measuredAt} onChange={handleChange} required />
                </div>
              </div>

              <div className="form-group">
                <label>Sede / Área *</label>
                <input name="facilityId" value={form.facilityId} onChange={handleChange} required />
              </div>

              <div className="form-group">
                <label>Consumo kWh *</label>
                <input
                  name="kwh"
                  type="number"
                  step="0.01"
                  value={form.kwh}
                  onChange={handleChange}
                  required
                  className="reading-kwh-input"
                />
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Voltaje</label>
                  <input name="voltage" type="number" step="0.1" value={form.voltage} onChange={handleChange} />
                </div>
                <div className="form-group">
                  <label>Factor de potencia</label>
                  <input name="powerFactor" type="number" step="0.01" value={form.powerFactor} onChange={handleChange} />
                </div>
              </div>

              <div className="form-group">
                <label>Observaciones</label>
                <textarea name="observaciones" rows="3" value={form.observaciones} onChange={handleChange} style={{ resize: 'vertical' }} />
              </div>

              {error && <div className="alert alert-danger" style={{ marginBottom: '1rem' }}><span className="icon">⚠</span><div>{error}</div></div>}

              <div className="reading-actions">
                <button type="button" className="btn btn-secondary" onClick={() => { setResult(null); setError('') }}>
                  Limpiar
                </button>
                <button type="submit" disabled={submitting} className="btn btn-primary">
                  {submitting ? 'Analizando...' : 'Analizar lectura'}
                </button>
              </div>
            </form>
          </section>

          <aside className="reading-side-panel">
            <ReadingResultCard result={result} submittedReading={submittedReading} />
            <div className="control-card">
              <div className="control-card-header">
                <div>
                  <span className="section-kicker">Conexión</span>
                  <h2>Análisis EnergyOps</h2>
                </div>
              </div>
              <p style={{ color: 'var(--gray-700)', lineHeight: 1.5, fontSize: '0.9rem' }}>
                El sistema compara la lectura contra el baseline activo y genera una anomalía cuando la desviación supera la tolerancia configurada.
              </p>
            </div>
          </aside>
        </div>
      )}

      {activeTab === 'historial' && <ReadingHistoryTab readings={readings} loading={readingsLoading} error={readingsError} />}
      {activeTab === 'tendencias' && <TrendsTab trends={trends} loading={readingsLoading} error={readingsError} />}
    </AppLayout>
  )
}

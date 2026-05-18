import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import AppLayout from '../components/AppLayout'
import { fetchDashboard } from '../services/analyticsService'
import { mockReto, mockTickets } from '../services/mockService'

function formatNumber(n) {
  if (n === null || n === undefined) return '—'
  const value = typeof n === 'string' ? parseFloat(n) : n
  if (Number.isNaN(value)) return '—'
  return value.toLocaleString('es-BO', { maximumFractionDigits: 2 })
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
  return map[severity] || severity
}

export default function DashboardKpisPage() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    fetchDashboard()
      .then(setData)
      .catch((err) => setError(err.response?.data?.message || 'Error cargando dashboard'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <AppLayout title="Dashboard Ejecutivo"><p>Cargando...</p></AppLayout>
  if (error) return <AppLayout title="Dashboard Ejecutivo"><div className="alert alert-danger">{error}</div></AppLayout>

  const totalKwh = (data.kpis || []).reduce((s, k) => s + parseFloat(k.kwh || 0), 0)
  const totalCost = (data.kpis || []).reduce((s, k) => s + parseFloat(k.estimatedCostImpact || 0), 0)
  const totalCo2 = (data.kpis || []).reduce((s, k) => s + parseFloat(k.estimatedCo2Impact || 0), 0)
  const anomalies = data.anomalies || []
  const criticalCount = anomalies.filter((a) => a.severity === 'CRITICAL').length

  return (
    <AppLayout title="Dashboard Ejecutivo - Sede Central">
      <div className="kpi-grid">
        <div className="kpi-card">
          <div className="kpi-label">⚡ kWh Consumidos</div>
          <div className="kpi-value">{formatNumber(totalKwh)}</div>
          <div className="kpi-sub">Últimas 20 lecturas</div>
          <div className="kpi-trend down">Total lecturas: {data.totalReadings}</div>
        </div>
        <div className="kpi-card teal">
          <div className="kpi-label">💰 Costo evitado/impacto</div>
          <div className="kpi-value">Bs. {formatNumber(totalCost)}</div>
          <div className="kpi-sub">Acumulado anomalías</div>
        </div>
        <div className="kpi-card info">
          <div className="kpi-label">🌿 CO₂ impacto</div>
          <div className="kpi-value">{formatNumber(totalCo2)} kg</div>
          <div className="kpi-sub">Factor: 0.44 kg/kWh</div>
        </div>
        <div className="kpi-card danger">
          <div className="kpi-label"><span className="live-dot"></span>Anomalías totales</div>
          <div className="kpi-value">{data.totalAnomalies}</div>
          <div className="kpi-sub">{criticalCount} crítica(s)</div>
          {criticalCount > 0 && <div className="kpi-trend up">⚠ Requiere atención</div>}
        </div>
      </div>

      <div className="row-2">
        <div className="card">
          <div className="card-title">Últimas Lecturas (kWh)</div>
          {data.kpis.length === 0 ? (
            <p style={{ color: 'var(--gray-500)', fontSize: '0.9rem' }}>Sin lecturas todavía. Registra una desde "Lecturas".</p>
          ) : (
            <div className="chart-placeholder">
              {data.kpis.slice(0, 8).reverse().map((k, idx) => {
                const max = Math.max(...data.kpis.map((x) => parseFloat(x.kwh)))
                const pct = Math.max(20, (parseFloat(k.kwh) / max) * 100)
                return (
                  <div
                    key={k.id || idx}
                    className="chart-bar"
                    style={{ height: `${pct}%` }}
                    data-label={new Date(k.measuredAt).toLocaleDateString('es-BO', { day: '2-digit', month: 'short' })}
                    data-value={`${formatNumber(k.kwh)} kWh`}
                  />
                )
              })}
            </div>
          )}
        </div>

        <div className="card">
          <div className="card-title">Anomalías Activas <span className="live-dot"></span></div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            {anomalies.slice(0, 4).map((a) => (
              <Link key={a.id} to={`/anomalias/${a.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
                <div className={`alert ${a.severity === 'CRITICAL' ? 'alert-danger' : 'alert-warning'}`}>
                  <span className="icon">⚠</span>
                  <div>
                    <strong>{a.facilityId || 'Anomalía'} · {a.meterId}</strong>
                    <div style={{ fontSize: '0.75rem', marginTop: '0.25rem' }}>
                      <span className={`badge ${severityBadge(a.severity)}`}>{severityLabel(a.severity)}</span>{' '}
                      Desviación: {formatNumber(a.deviationPercent)}%
                    </div>
                  </div>
                </div>
              </Link>
            ))}
            {anomalies.length === 0 && (
              <p style={{ color: 'var(--gray-500)', fontSize: '0.85rem' }}>Sin anomalías activas. ✓</p>
            )}
          </div>
        </div>
      </div>

      <div className="row-2">
        <div className="card">
          <div className="card-title">
            Tickets Recientes <span className="badge info" style={{ marginLeft: 8 }}>MOCK</span>
            <Link to="/tickets/nuevo" className="btn btn-secondary" style={{ padding: '0.4rem 0.75rem', fontSize: '0.8rem' }}>+ Nuevo</Link>
          </div>
          <table>
            <thead>
              <tr><th>ID</th><th>Descripción</th><th>Técnico</th><th>Estado</th></tr>
            </thead>
            <tbody>
              {mockTickets.map((t) => (
                <tr key={t.id}>
                  <td><strong>{t.id}</strong></td>
                  <td>{t.titulo}</td>
                  <td>{t.tecnico}</td>
                  <td><span className={`badge ${t.estado === 'CERRADO' ? 'success' : t.estado === 'EN_PROCESO' ? 'process' : 'info'}`}>{t.estado.replace('_', ' ')}</span></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="card">
          <div className="card-title">🎯 Reto Activo <span className="badge info" style={{ marginLeft: 8 }}>MOCK</span></div>
          <h3 style={{ fontSize: '1rem', color: 'var(--green-dark)', marginBottom: '0.5rem' }}>{mockReto.nombre}</h3>
          <p style={{ fontSize: '0.85rem', color: 'var(--gray-500)', marginBottom: '1rem' }}>{mockReto.meta}</p>
          <div className="progress-bar"><div className="progress-fill" style={{ width: `${mockReto.progreso}%` }} /></div>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '0.5rem', fontSize: '0.8rem' }}>
            <span style={{ color: 'var(--gray-500)' }}>Progreso</span>
            <strong style={{ color: 'var(--green-dark)' }}>{mockReto.progreso}%</strong>
          </div>
          <div style={{ marginTop: '1rem', paddingTop: '1rem', borderTop: '1px solid var(--gray-200)', fontSize: '0.85rem' }}>
            <p>🏆 <strong>Líder:</strong> {mockReto.lider}</p>
            <p>👥 <strong>Participantes:</strong> {mockReto.participantes} cursos</p>
          </div>
        </div>
      </div>
    </AppLayout>
  )
}

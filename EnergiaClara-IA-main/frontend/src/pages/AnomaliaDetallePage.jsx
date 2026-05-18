import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import AppLayout from '../components/AppLayout'
import { fetchAnomalyById } from '../services/analyticsService'

const SEV_BADGE = { CRITICAL: 'critica', HIGH: 'alta', MEDIUM: 'media', LOW: 'baja' }
const SEV_LABEL = { CRITICAL: 'Crítica', HIGH: 'Alta', MEDIUM: 'Media', LOW: 'Baja' }
const SEV_SCORE = { CRITICAL: 0.95, HIGH: 0.75, MEDIUM: 0.5, LOW: 0.25 }

export default function AnomaliaDetallePage() {
  const { id } = useParams()
  const [anomaly, setAnomaly] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    fetchAnomalyById(id)
      .then((a) => {
        if (!a) setError('Anomalía no encontrada')
        else setAnomaly(a)
      })
      .catch((err) => setError(err.response?.data?.message || 'Error cargando anomalía'))
      .finally(() => setLoading(false))
  }, [id])

  if (loading) return <AppLayout title="Anomalía - Detalle"><p>Cargando...</p></AppLayout>
  if (error) return <AppLayout title="Anomalía - Detalle"><div className="alert alert-danger">{error}</div></AppLayout>

  const score = SEV_SCORE[anomaly.severity] || 0.5
  const scorePct = Math.round(score * 100)

  return (
    <AppLayout title={`Anomalía ${anomaly.id.slice(0, 8)} - Detalle`}>
      <div className={`alert ${anomaly.severity === 'CRITICAL' ? 'alert-danger' : 'alert-warning'}`} style={{ marginBottom: '1.25rem', padding: '1.25rem', borderRadius: 12 }}>
        <span className="icon" style={{ fontSize: '1.8rem' }}>🚨</span>
        <div style={{ flex: 1 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.5rem' }}>
            <span className={`badge ${SEV_BADGE[anomaly.severity] || 'media'}`}>{SEV_LABEL[anomaly.severity]}</span>
            <strong style={{ fontSize: '1.1rem' }}>{anomaly.type} · {anomaly.facilityId || 'Sin área'}</strong>
          </div>
          <p style={{ fontSize: '0.85rem' }}>
            Detectado: <strong>{new Date(anomaly.measuredAt).toLocaleString('es-BO')}</strong> · Medidor: {anomaly.meterId || '—'}
          </p>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '1.25rem', marginBottom: '1.25rem' }}>
        <div className="card">
          <div className="card-title">Score de Anomalía</div>
          <div style={{ textAlign: 'center', padding: '1rem 0' }}>
            <div className="score-value">{score.toFixed(2)}</div>
            <p style={{ fontSize: '0.8rem', color: 'var(--gray-500)', marginTop: '0.25rem' }}>Score IA (0 - 1)</p>
          </div>
          <div className="score-bar"><div className="score-fill" style={{ width: `${scorePct}%` }} /></div>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '0.5rem', fontSize: '0.7rem', color: 'var(--gray-500)' }}>
            <span>Normal</span><span>Crítico</span>
          </div>
        </div>

        <div className="card">
          <div className="card-title">Datos de Consumo</div>
          <table>
            <tbody>
              <tr><td style={{ fontWeight: 600 }}>Desviación</td><td><strong style={{ color: 'var(--red)' }}>+{anomaly.deviationPercent}%</strong> vs baseline</td></tr>
              <tr><td style={{ fontWeight: 600 }}>Tipo</td><td>{anomaly.type}</td></tr>
              <tr><td style={{ fontWeight: 600 }}>Severidad</td><td>{SEV_LABEL[anomaly.severity]}</td></tr>
              <tr><td style={{ fontWeight: 600 }}>Costo estimado</td><td><strong>Bs. {anomaly.estimatedCostImpact ?? '—'}</strong></td></tr>
              <tr><td style={{ fontWeight: 600 }}>CO₂ estimado</td><td><strong>{anomaly.estimatedCo2Impact ?? '—'} kg</strong></td></tr>
              <tr><td style={{ fontWeight: 600 }}>Lectura origen</td><td><code>{anomaly.readingId}</code></td></tr>
            </tbody>
          </table>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '1.25rem', marginBottom: '1.25rem' }}>
        <div className="card" style={{ borderLeft: '4px solid var(--purple)' }}>
          <div className="card-title"><span className="ai-tag">🤖 Recomendación</span></div>
          <p style={{ fontSize: '0.9rem', marginBottom: '1rem', lineHeight: 1.5 }}>
            <strong>Explicación:</strong> {anomaly.explanation || 'Sin explicación.'}
          </p>
          <p style={{ fontSize: '0.9rem', lineHeight: 1.5 }}>
            <strong>Acción sugerida:</strong> {anomaly.recommendation || 'Sin recomendación.'}
          </p>
        </div>

        <div className="card">
          <div className="card-title">Acciones</div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            <Link to={`/tickets/nuevo?anomalia=${anomaly.id}`} className="btn btn-primary btn-block">🔧 Convertir en Ticket</Link>
            <Link to="/anomalias" className="btn btn-secondary btn-block">← Volver a listado</Link>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="card-title">Línea de tiempo</div>
        <div className="timeline">
          <div className="timeline-step done">
            <div className="timeline-dot" />
            <div className="timeline-label">DETECTADA</div>
            <div className="timeline-date">{new Date(anomaly.measuredAt).toLocaleString('es-BO', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' })}</div>
          </div>
          <div className="timeline-step active">
            <div className="timeline-dot" />
            <div className="timeline-label">NOTIFICADA</div>
            <div className="timeline-date">Auto</div>
          </div>
          <div className="timeline-step">
            <div className="timeline-dot" />
            <div className="timeline-label">EN ACCIÓN</div>
            <div className="timeline-date">Pendiente</div>
          </div>
          <div className="timeline-step">
            <div className="timeline-dot" />
            <div className="timeline-label">RESUELTA</div>
            <div className="timeline-date">Pendiente</div>
          </div>
        </div>
      </div>
    </AppLayout>
  )
}

import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import AppLayout from '../components/AppLayout'
import { fetchAnomalyById } from '../services/analyticsService'

const SEV_BADGE = { CRITICAL: 'critica', HIGH: 'alta', MEDIUM: 'media', LOW: 'baja' }
const SEV_LABEL = { CRITICAL: 'Crítica', HIGH: 'Alta', MEDIUM: 'Media', LOW: 'Baja' }
const SEV_SCORE = { CRITICAL: 0.95, HIGH: 0.75, MEDIUM: 0.5, LOW: 0.25 }
const READ_ONLY_STATUSES = ['DERIVADA', 'EN_ATENCION', 'RESUELTA']
const BASELINE_LABEL = {
  HISTORY: 'Baseline dinámico',
  STATIC_BASELINE: 'Baseline fijo',
  INPUT_AS_EXPECTED: 'Lectura actual como referencia'
}

function formatAiNumber(value, decimals = 2) {
  if (value === null || value === undefined || value === '') return '—'
  const numeric = Number(value)
  return Number.isFinite(numeric) ? numeric.toFixed(decimals) : value
}

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
  const isReadOnly = READ_ONLY_STATUSES.includes(anomaly.status)
  const baselineLabel = BASELINE_LABEL[anomaly.baselineSource] || anomaly.baselineSource || '—'
  const usesHybridAi = Boolean(
    anomaly.modelVersion ||
    anomaly.baselineSource === 'HISTORY' ||
    anomaly.zScore !== null && anomaly.zScore !== undefined ||
    anomaly.sampleCount !== null && anomaly.sampleCount !== undefined ||
    /ia hibrida|baseline dinam/i.test(anomaly.explanation || '')
  )

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
              <tr><td style={{ fontWeight: 600 }}>Estado</td><td><span className="badge info">{anomaly.status || 'DETECTADA'}</span></td></tr>
              {anomaly.ticketId && <tr><td style={{ fontWeight: 600 }}>Ticket asociado</td><td><code>{anomaly.ticketId}</code></td></tr>}
              {anomaly.responsibleTechnicianName && <tr><td style={{ fontWeight: 600 }}>Técnico</td><td>{anomaly.responsibleTechnicianName}</td></tr>}
              {anomaly.resolvedAt && <tr><td style={{ fontWeight: 600 }}>Resuelta</td><td>{new Date(anomaly.resolvedAt).toLocaleString('es-BO')}</td></tr>}
            </tbody>
          </table>
        </div>
      </div>

      <div className="card" style={{ marginBottom: '1.25rem', borderLeft: '4px solid var(--blue)' }}>
        <div className="card-title">Evidencia IA híbrida explicable</div>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(155px, 1fr))', gap: '0.85rem' }}>
          <div style={{ padding: '0.85rem', border: '1px solid var(--gray-200)', borderRadius: 8 }}>
            <div style={{ fontSize: '0.72rem', color: 'var(--gray-500)', fontWeight: 700, textTransform: 'uppercase' }}>IA híbrida utilizada</div>
            <div style={{ fontSize: '1rem', fontWeight: 700, marginTop: '0.25rem' }}>{usesHybridAi ? 'Sí' : 'No'}</div>
          </div>
          <div style={{ padding: '0.85rem', border: '1px solid var(--gray-200)', borderRadius: 8 }}>
            <div style={{ fontSize: '0.72rem', color: 'var(--gray-500)', fontWeight: 700, textTransform: 'uppercase' }}>Baseline dinámico</div>
            <div style={{ fontSize: '1rem', fontWeight: 700, marginTop: '0.25rem' }}>{formatAiNumber(anomaly.expectedKwh)} kWh</div>
            <div style={{ fontSize: '0.75rem', color: 'var(--gray-500)', marginTop: '0.15rem' }}>{baselineLabel}</div>
          </div>
          <div style={{ padding: '0.85rem', border: '1px solid var(--gray-200)', borderRadius: 8 }}>
            <div style={{ fontSize: '0.72rem', color: 'var(--gray-500)', fontWeight: 700, textTransform: 'uppercase' }}>Historial analizado</div>
            <div style={{ fontSize: '1rem', fontWeight: 700, marginTop: '0.25rem' }}>{anomaly.sampleCount ?? '—'} lecturas</div>
          </div>
          <div style={{ padding: '0.85rem', border: '1px solid var(--gray-200)', borderRadius: 8 }}>
            <div style={{ fontSize: '0.72rem', color: 'var(--gray-500)', fontWeight: 700, textTransform: 'uppercase' }}>Z-Score</div>
            <div style={{ fontSize: '1rem', fontWeight: 700, marginTop: '0.25rem' }}>{formatAiNumber(anomaly.zScore)}</div>
          </div>
          <div style={{ padding: '0.85rem', border: '1px solid var(--gray-200)', borderRadius: 8 }}>
            <div style={{ fontSize: '0.72rem', color: 'var(--gray-500)', fontWeight: 700, textTransform: 'uppercase' }}>Confianza</div>
            <div style={{ fontSize: '1rem', fontWeight: 700, marginTop: '0.25rem' }}>{formatAiNumber(anomaly.confidence)}</div>
          </div>
          <div style={{ padding: '0.85rem', border: '1px solid var(--gray-200)', borderRadius: 8 }}>
            <div style={{ fontSize: '0.72rem', color: 'var(--gray-500)', fontWeight: 700, textTransform: 'uppercase' }}>Versión modelo</div>
            <div style={{ fontSize: '0.9rem', fontWeight: 700, marginTop: '0.25rem', wordBreak: 'break-word' }}>{anomaly.modelVersion || '—'}</div>
          </div>
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
            {isReadOnly ? (
              <div className="alert alert-info">Anomalía en modo lectura. Ya fue derivada, está en atención o fue resuelta.</div>
            ) : (
              <Link to={`/tickets/nuevo?anomalia=${anomaly.id}`} className="btn btn-primary btn-block">🔧 Convertir en Ticket</Link>
            )}
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

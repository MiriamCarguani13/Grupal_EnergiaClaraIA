import { useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import AppLayout from '../components/AppLayout'
import { fetchAnomalyById } from '../services/analyticsService'

const SEV_BADGE = { CRITICAL: 'critica', HIGH: 'alta', MEDIUM: 'media', LOW: 'baja' }
const SEV_LABEL = { CRITICAL: 'Crítica', HIGH: 'Alta', MEDIUM: 'Media', LOW: 'Baja' }
const READ_ONLY_STATUSES = ['DERIVADA', 'EN_ATENCION', 'RESUELTA']
const BASELINE_LABEL = {
  HISTORY: 'Baseline dinámico',
  STATIC_BASELINE: 'Baseline fijo',
  INPUT_AS_EXPECTED: 'Lectura actual como referencia',
}
const STATUS_LABEL = {
  DETECTADA: 'Detectada',
  DERIVADA: 'Derivada',
  EN_ATENCION: 'En atención',
  RESUELTA: 'Resuelta',
}
const FLOW_STEPS = [
  { id: 'LECTURA', label: 'Lectura registrada' },
  { id: 'AI', label: 'Análisis IA' },
  { id: 'DETECTADA', label: 'Anomalía detectada' },
  { id: 'DERIVADA', label: 'Derivación a mantenimiento' },
  { id: 'RESUELTA', label: 'Resolución' },
]

function formatAiNumber(value, decimals = 2) {
  if (value === null || value === undefined || value === '') return 'No disponible'
  const numeric = Number(value)
  return Number.isFinite(numeric) ? numeric.toFixed(decimals) : value
}

function formatDate(value) {
  if (!value) return 'No disponible'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? 'No disponible' : date.toLocaleString('es-BO')
}

function estimateRegisteredKwh(anomaly) {
  const baseline = Number(anomaly.expectedKwh)
  const deviation = Number(anomaly.deviationPercent)
  if (!Number.isFinite(baseline) || !Number.isFinite(deviation)) return null
  return baseline * (1 + deviation / 100)
}

function normalizeStatus(status) {
  return status || 'DETECTADA'
}

function DetailMetric({ label, value, highlight }) {
  return (
    <div className={highlight ? 'detail-metric highlight' : 'detail-metric'}>
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  )
}

function FlowTimeline({ status, measuredAt }) {
  const normalized = normalizeStatus(status)
  const activeIndex = normalized === 'RESUELTA'
    ? 4
    : normalized === 'DERIVADA' || normalized === 'EN_ATENCION'
      ? 3
      : 2

  return (
    <div className="anomaly-flow">
      {FLOW_STEPS.map((step, index) => (
        <div
          key={step.id}
          className={`anomaly-flow-step ${index <= activeIndex ? 'done' : ''} ${index === activeIndex ? 'active' : ''}`}
        >
          <div className="anomaly-flow-dot" />
          <div>
            <strong>{step.label}</strong>
            <span>{index === 0 ? formatDate(measuredAt) : index <= activeIndex ? 'Completado' : 'Pendiente'}</span>
          </div>
        </div>
      ))}
    </div>
  )
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

  const derived = useMemo(() => {
    if (!anomaly) return null
    const status = normalizeStatus(anomaly.status)
    const registeredKwh = estimateRegisteredKwh(anomaly)
    const baselineLabel = BASELINE_LABEL[anomaly.baselineSource] || anomaly.baselineSource || 'No disponible'
    const usesHybridAi = Boolean(
      anomaly.modelVersion ||
      anomaly.baselineSource === 'HISTORY' ||
      anomaly.zScore !== null && anomaly.zScore !== undefined ||
      anomaly.sampleCount !== null && anomaly.sampleCount !== undefined ||
      /ia hibrida|baseline dinam/i.test(anomaly.explanation || '')
    )
    return {
      status,
      registeredKwh,
      baselineLabel,
      usesHybridAi,
      isReadOnly: READ_ONLY_STATUSES.includes(status),
    }
  }, [anomaly])

  if (loading) return <AppLayout title="Anomalía - Detalle"><p>Cargando...</p></AppLayout>
  if (error) return <AppLayout title="Anomalía - Detalle"><div className="alert alert-danger">{error}</div></AppLayout>

  return (
    <AppLayout title={`Anomalía ${anomaly.id.slice(0, 8)} - Detalle`}>
      <section className="page-hero compact anomaly-detail-hero">
        <div>
          <span className="section-kicker">Detalle de evento energético</span>
          <h1>Anomalía {anomaly.id.slice(0, 8)}</h1>
          <p>{anomaly.type || 'Desviación energética'} · {anomaly.facilityId || 'Área no registrada'}</p>
        </div>
        <div className="anomaly-detail-hero-badges">
          <span className={`badge ${SEV_BADGE[anomaly.severity] || 'media'}`}>{SEV_LABEL[anomaly.severity] || anomaly.severity || 'Media'}</span>
          <span className={`status-badge status-${derived.status.toLowerCase()}`}>{STATUS_LABEL[derived.status] || derived.status}</span>
        </div>
      </section>

      <section className="control-card anomaly-detail-section">
        <div className="control-card-header">
          <div>
            <span className="section-kicker">Sección 1</span>
            <h2>Resumen de anomalía</h2>
          </div>
        </div>
        <div className="detail-metric-grid">
          <DetailMetric label="Medidor" value={anomaly.meterId || 'No disponible'} />
          <DetailMetric label="Estado" value={STATUS_LABEL[derived.status] || derived.status} />
          <DetailMetric label="Severidad" value={SEV_LABEL[anomaly.severity] || anomaly.severity || 'No disponible'} />
          <DetailMetric label="Fecha" value={formatDate(anomaly.measuredAt)} />
          <DetailMetric label="Consumo registrado" value={`${formatAiNumber(derived.registeredKwh)} kWh`} highlight />
          <DetailMetric label="Baseline esperado" value={`${formatAiNumber(anomaly.expectedKwh)} kWh`} />
          <DetailMetric label="Desviación" value={`${formatAiNumber(anomaly.deviationPercent)}%`} />
        </div>
      </section>

      <section className="ai-explainability-panel anomaly-detail-section">
        <div className="ai-explainability-header">
          <div>
            <span className="section-kicker">Sección 2</span>
            <h2>Explicabilidad IA</h2>
            <p>Evidencia calculada para explicar por qué la lectura salió del comportamiento esperado.</p>
          </div>
          <span className="ai-used-pill">{derived.usesHybridAi ? 'IA híbrida utilizada' : 'Fallback determinístico'}</span>
        </div>

        <div className="ai-evidence-grid">
          <div className="ai-evidence-card highlight">
            <span>Modelo usado</span>
            <strong>{anomaly.modelVersion || 'No disponible'}</strong>
          </div>
          <div className="ai-evidence-card">
            <span>Confidence</span>
            <strong>{formatAiNumber(anomaly.confidence)}</strong>
          </div>
          <div className="ai-evidence-card">
            <span>Z-score</span>
            <strong>{formatAiNumber(anomaly.zScore)}</strong>
          </div>
          <div className="ai-evidence-card">
            <span>Score de anomalía</span>
            <strong>{formatAiNumber(anomaly.anomalyScore ?? anomaly.confidence)}</strong>
          </div>
          <div className="ai-evidence-card">
            <span>Muestras históricas</span>
            <strong>{anomaly.sampleCount ?? 'No disponible'}</strong>
          </div>
          <div className="ai-evidence-card">
            <span>Tipo de baseline</span>
            <strong>{derived.baselineLabel}</strong>
          </div>
        </div>

        <div className="ai-explanation-copy single">
          <div>
            <h3>Explicación generada</h3>
            <p>{anomaly.explanation || 'No disponible'}</p>
          </div>
        </div>
      </section>

      <section className="control-card anomaly-detail-section">
        <div className="control-card-header">
          <div>
            <span className="section-kicker">Sección 3</span>
            <h2>Recomendación</h2>
          </div>
        </div>
        <div className="recommendation-layout">
          <div className="recommendation-main">
            <span>Acción sugerida</span>
            <p>{anomaly.recommendation || 'No disponible'}</p>
          </div>
          <div className="recommendation-impact-grid">
            <DetailMetric label="Impacto estimado" value={`${formatAiNumber(anomaly.estimatedEnergyImpact ?? anomaly.energyImpactKwh)} kWh`} />
            <DetailMetric label="Costo estimado" value={`Bs. ${formatAiNumber(anomaly.estimatedCostImpact)}`} />
            <DetailMetric label="CO2 estimado" value={`${formatAiNumber(anomaly.estimatedCo2Impact)} kg`} />
          </div>
        </div>
      </section>

      <section className="control-card anomaly-detail-section">
        <div className="control-card-header">
          <div>
            <span className="section-kicker">Sección 4</span>
            <h2>Flujo operativo</h2>
          </div>
        </div>
        <FlowTimeline status={derived.status} measuredAt={anomaly.measuredAt} />
      </section>

      <section className="control-card anomaly-detail-section">
        <div className="control-card-header">
          <div>
            <span className="section-kicker">Sección 5</span>
            <h2>Mantenimiento</h2>
          </div>
        </div>
        <div className="maintenance-panel">
          <div>
            <span className="anomaly-card-label">Ticket asociado</span>
            <strong>{anomaly.ticketId ? anomaly.ticketId : 'No disponible'}</strong>
            <p>Estado del ticket: {anomaly.ticketStatus || (anomaly.ticketId ? 'Derivado' : 'No disponible')}</p>
            {anomaly.responsibleTechnicianName && <p>Técnico responsable: {anomaly.responsibleTechnicianName}</p>}
            {anomaly.resolvedAt && <p>Fecha de resolución: {formatDate(anomaly.resolvedAt)}</p>}
          </div>
          <div className="maintenance-actions">
            {derived.isReadOnly ? (
              <span className="readonly-pill">Derivación no disponible para este estado</span>
            ) : (
              <Link to={`/tickets/nuevo?anomalia=${anomaly.id}`} className="btn btn-primary">Derivar a mantenimiento</Link>
            )}
            <Link to="/anomalias" className="btn btn-secondary">Volver a anomalías</Link>
          </div>
        </div>
      </section>
    </AppLayout>
  )
}

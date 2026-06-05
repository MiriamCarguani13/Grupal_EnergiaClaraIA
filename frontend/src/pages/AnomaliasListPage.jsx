import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import AppLayout from '../components/AppLayout'
import { fetchAnomalies } from '../services/analyticsService'

const SEV_BADGE = { CRITICAL: 'critica', HIGH: 'alta', MEDIUM: 'media', LOW: 'baja' }
const SEV_LABEL = { CRITICAL: 'Crítica', HIGH: 'Alta', MEDIUM: 'Media', LOW: 'Baja' }
const PENDING = ['DETECTADA']
const IN_PROGRESS = ['DERIVADA', 'EN_ATENCION']
const RESOLVED = ['RESUELTA']

export default function AnomaliasListPage() {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    fetchAnomalies()
      .then(setItems)
      .catch((err) => setError(err.response?.data?.message || 'Error cargando anomalías'))
      .finally(() => setLoading(false))
  }, [])

  return (
    <AppLayout title="Anomalías - Listado">
      <div className="card">
        <div className="card-title">Anomalías detectadas <span className="live-dot"></span></div>
        {loading && <p>Cargando...</p>}
        {error && <div className="alert alert-danger">{error}</div>}
        {!loading && items.length === 0 && <p style={{ color: 'var(--gray-500)' }}>Sin anomalías. ✓</p>}

        {items.length > 0 && renderTable('Anomalías pendientes', items.filter((a) => PENDING.includes(a.status || 'DETECTADA')))}
        {items.length > 0 && renderTable('Anomalías en atención', items.filter((a) => IN_PROGRESS.includes(a.status)))}
        {items.length > 0 && renderTable('Anomalías resueltas', items.filter((a) => RESOLVED.includes(a.status)), true)}
      </div>
    </AppLayout>
  )
}

function renderTable(title, items, resolved = false) {
  return (
    <div style={{ marginTop: '1rem' }}>
      <h3 style={{ fontSize: '1rem', marginBottom: '0.75rem' }}>{title} ({items.length})</h3>
      {items.length === 0 ? (
        <p style={{ color: 'var(--gray-500)', fontSize: '0.85rem' }}>Sin registros.</p>
      ) : (
          <table>
            <thead>
              <tr>
                <th>Detectada</th>
                <th>Área</th>
                <th>Medidor</th>
                <th>Estado</th>
                <th>Severidad</th>
                <th>Desviación</th>
                <th>Impacto Bs.</th>
                {resolved && <th>Técnico</th>}
                {resolved && <th>Resuelta</th>}
                {resolved && <th>Ticket</th>}
                <th></th>
              </tr>
            </thead>
            <tbody>
              {items.map((a) => (
                <tr key={a.id}>
                  <td>{new Date(a.measuredAt).toLocaleString('es-BO')}</td>
                  <td>{a.facilityId || '—'}</td>
                  <td>{a.meterId || '—'}</td>
                  <td><span className="badge info">{a.status || 'DETECTADA'}</span></td>
                  <td><span className={`badge ${SEV_BADGE[a.severity] || 'media'}`}>{SEV_LABEL[a.severity] || a.severity}</span></td>
                  <td><strong style={{ color: 'var(--red)' }}>{a.deviationPercent}%</strong></td>
                  <td>{a.estimatedCostImpact ?? '—'}</td>
                  {resolved && <td>{a.responsibleTechnicianName || '—'}</td>}
                  {resolved && <td>{a.resolvedAt ? new Date(a.resolvedAt).toLocaleString('es-BO') : '—'}</td>}
                  {resolved && <td>{a.ticketId ? <code>{a.ticketId.slice(0, 8)}</code> : '—'}</td>}
                  <td><Link to={`/anomalias/${a.id}`} className="btn btn-secondary" style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem' }}>Ver</Link></td>
                </tr>
              ))}
            </tbody>
          </table>
      )}
    </div>
  )
}

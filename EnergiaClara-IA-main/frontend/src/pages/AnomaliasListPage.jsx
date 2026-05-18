import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import AppLayout from '../components/AppLayout'
import { fetchAnomalies } from '../services/analyticsService'

const SEV_BADGE = { CRITICAL: 'critica', HIGH: 'alta', MEDIUM: 'media', LOW: 'baja' }
const SEV_LABEL = { CRITICAL: 'Crítica', HIGH: 'Alta', MEDIUM: 'Media', LOW: 'Baja' }

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
        {items.length > 0 && (
          <table>
            <thead>
              <tr>
                <th>Detectada</th>
                <th>Área</th>
                <th>Medidor</th>
                <th>Severidad</th>
                <th>Desviación</th>
                <th>Impacto Bs.</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {items.map((a) => (
                <tr key={a.id}>
                  <td>{new Date(a.measuredAt).toLocaleString('es-BO')}</td>
                  <td>{a.facilityId || '—'}</td>
                  <td>{a.meterId || '—'}</td>
                  <td><span className={`badge ${SEV_BADGE[a.severity] || 'media'}`}>{SEV_LABEL[a.severity] || a.severity}</span></td>
                  <td><strong style={{ color: 'var(--red)' }}>{a.deviationPercent}%</strong></td>
                  <td>{a.estimatedCostImpact ?? '—'}</td>
                  <td><Link to={`/anomalias/${a.id}`} className="btn btn-secondary" style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem' }}>Ver</Link></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </AppLayout>
  )
}

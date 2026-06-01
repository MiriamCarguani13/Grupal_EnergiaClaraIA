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
  const [activeTab, setActiveTab] = useState('activas')

  useEffect(() => {
    fetchAnomalies()
      .then(setItems)
      .catch((err) => setError(err.response?.data?.message || 'Error cargando anomalías'))
      .finally(() => setLoading(false))
  }, [])

  const filteredItems = items.filter((a) => {
    if (activeTab === 'activas') return a.estado !== 'RESUELTA'
    return a.estado === 'RESUELTA'
  })

  return (
    <AppLayout title="Anomalías - Listado">
      <div className="card">
        <div className="card-title" style={{ marginBottom: '1rem' }}>Anomalías detectadas <span className="live-dot"></span></div>

        <div style={{ display: 'flex', gap: '1rem', marginBottom: '1.25rem', borderBottom: '1px solid var(--gray-200)', paddingBottom: '0.5rem' }}>
          <button
            onClick={() => setActiveTab('activas')}
            style={{
              background: 'none',
              border: 'none',
              fontWeight: 700,
              fontSize: '0.9rem',
              color: activeTab === 'activas' ? 'var(--green-dark)' : 'var(--gray-500)',
              borderBottom: activeTab === 'activas' ? '2.5px solid var(--green-dark)' : 'none',
              paddingBottom: '0.4rem',
              cursor: 'pointer'
            }}
          >
            🔍 Activas ({items.filter(a => a.estado !== 'RESUELTA').length})
          </button>
          <button
            onClick={() => setActiveTab('resueltas')}
            style={{
              background: 'none',
              border: 'none',
              fontWeight: 700,
              fontSize: '0.9rem',
              color: activeTab === 'resueltas' ? 'var(--green-dark)' : 'var(--gray-500)',
              borderBottom: activeTab === 'resueltas' ? '2.5px solid var(--green-dark)' : 'none',
              paddingBottom: '0.4rem',
              cursor: 'pointer'
            }}
          >
            ✓ Historial / Resueltas ({items.filter(a => a.estado === 'RESUELTA').length})
          </button>
        </div>

        {loading && <p>Cargando...</p>}
        {error && <div className="alert alert-danger">{error}</div>}
        {!loading && filteredItems.length === 0 && <p style={{ color: 'var(--gray-500)' }}>Sin anomalías en esta sección. ✓</p>}
        {filteredItems.length > 0 && (
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
              {filteredItems.map((a) => (
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

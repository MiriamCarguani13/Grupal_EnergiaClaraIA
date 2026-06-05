import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import MobileLayout from '../components/MobileLayout'
import { getMaintenanceTickets } from '../services/maintenanceService'

const ESTADO_BADGE = { PENDIENTE: 'info', EN_PROGRESO: 'process', REPARADO: 'success', CERRADO: 'success', ASIGNADO: 'info', ABIERTO: 'info' }
const PRIORIDAD_CLASS = { BAJA: 'baja', MEDIA: 'media', ALTA: 'alta', CRITICA: 'critica' }
const RESOLVED_STATUSES = ['REPARADO', 'CERRADO']

export default function MobileTicketsPage() {
  const navigate = useNavigate()
  const [tickets, setTickets] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    getMaintenanceTickets()
      .then(setTickets)
      .catch((err) => setError(err.response?.data?.message || 'No se pudieron cargar tickets.'))
      .finally(() => setLoading(false))
  }, [])

  const activeTickets = tickets.filter((t) => !RESOLVED_STATUSES.includes(t.status))
  const resolvedTickets = tickets.filter((t) => RESOLVED_STATUSES.includes(t.status))

  const renderTicket = (t, readOnly = false) => (
    <div
      key={t.id}
      className={`ticket-mobile-card ${PRIORIDAD_CLASS[t.priority] || 'media'}`}
      onClick={() => navigate(`/m/cierre/${t.id}`)}
      style={readOnly ? { opacity: 0.85 } : undefined}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <h4>{t.title}</h4>
        <span className={`badge ${ESTADO_BADGE[t.status] || 'info'}`} style={{ fontSize: '0.6rem' }}>{t.status.replace('_', ' ')}</span>
      </div>
      <div className="meta">{t.id.slice(0, 8)} · {t.description || 'Ticket de mantenimiento'}</div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span className={`badge ${PRIORIDAD_CLASS[t.priority] || 'media'}`}>{t.priority}</span>
        <span className="sla">{readOnly ? 'Solo lectura' : (t.technicalStatus || 'Pendiente ficha')}</span>
      </div>
    </div>
  )

  return (
    <MobileLayout title="Mis Tickets">
      {error && <div className="alert alert-danger" style={{ marginBottom: '1rem' }}>{error}</div>}
      {loading && <div className="alert alert-info" style={{ marginBottom: '1rem' }}>Cargando tickets...</div>}

      <h3 style={{ fontSize: '0.95rem', marginBottom: '0.75rem' }}>Tickets activos ({activeTickets.length})</h3>
      {activeTickets.map((t) => renderTicket(t))}
      {!loading && activeTickets.length === 0 && (
        <div className="alert alert-info" style={{ marginBottom: '1rem' }}>No hay tickets activos.</div>
      )}

      <h3 style={{ fontSize: '0.95rem', margin: '1.25rem 0 0.75rem' }}>Tickets resueltos ({resolvedTickets.length})</h3>
      {resolvedTickets.map((t) => renderTicket(t, true))}

      {!loading && tickets.length === 0 && (
        <div className="alert alert-info">No hay tickets asignados o disponibles.</div>
      )}
    </MobileLayout>
  )
}

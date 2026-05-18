import { useNavigate } from 'react-router-dom'
import MobileLayout from '../components/MobileLayout'
import { mockTickets } from '../services/mockService'

const ESTADO_BADGE = { EN_PROCESO: 'process', ASIGNADO: 'info', CERRADO: 'success' }

export default function MobileTicketsPage() {
  const navigate = useNavigate()

  return (
    <MobileLayout title="Mis Tickets" headerRight={<button className="icon-btn">🔔</button>}>
      <div className="alert alert-warning" style={{ marginBottom: '1rem', fontSize: '0.75rem', padding: '0.5rem' }}>
        <span className="icon">ℹ</span>
        <div>Datos mock - sin backend de tickets</div>
      </div>

      <h3 style={{ fontSize: '0.95rem', marginBottom: '0.75rem' }}>Tickets activos ({mockTickets.filter((t) => t.estado !== 'CERRADO').length})</h3>

      {mockTickets.map((t) => (
        <div
          key={t.id}
          className={`ticket-mobile-card ${t.prioridad}`}
          onClick={() => navigate(`/m/cierre/${t.id}`)}
        >
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <h4>{t.titulo}</h4>
            <span className={`badge ${ESTADO_BADGE[t.estado]}`} style={{ fontSize: '0.6rem' }}>{t.estado.replace('_', ' ')}</span>
          </div>
          <div className="meta">{t.id} · {t.area}</div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span className={`badge ${t.prioridad}`}>{t.prioridad.toUpperCase()}</span>
            {t.slaHorasRestantes > 0 && (
              <span className="sla">⏱ {t.slaHorasRestantes}h restantes</span>
            )}
          </div>
        </div>
      ))}
    </MobileLayout>
  )
}

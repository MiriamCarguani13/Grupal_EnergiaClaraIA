import { useNavigate } from 'react-router-dom'
import { useEffect, useState } from 'react'
import MobileLayout from '../components/MobileLayout'
import { ticketService } from '../services/ticketService'

import { useAuth } from '../context/AuthContext'

const ESTADO_BADGE = { EN_PROCESO: 'process', ASIGNADO: 'info', CERRADO: 'success' }

export default function MobileTicketsPage() {
  const navigate = useNavigate()
  const { auth } = useAuth()
  const [tickets, setTickets] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    ticketService.getTickets().then(data => {
      setTickets(data)
      setLoading(false)
    }).catch(err => {
      console.error(err)
      setLoading(false)
    })
  }, [])

  const myTickets = tickets.filter(t => t.asignadoA === auth.userId)

  return (
    <MobileLayout title="Mis Tickets" headerRight={<button className="icon-btn">🔔</button>}>

      <h3 style={{ fontSize: '0.95rem', marginBottom: '0.75rem' }}>Tickets activos ({myTickets.filter((t) => t.estado !== 'CERRADO').length})</h3>

      {loading ? <p>Cargando tickets...</p> : myTickets.length === 0 ? <p style={{color: 'var(--gray-500)', fontSize: '0.85rem'}}>No tienes tickets asignados.</p> : myTickets.map((t) => (
        <div
          key={t.ticketId}
          className={`ticket-mobile-card ${t.prioridad ? t.prioridad.toLowerCase() : 'media'} ${t.estado === 'CERRADO' ? 'done' : ''}`}
          onClick={() => t.estado !== 'CERRADO' && navigate(`/m/cierre/${t.ticketId}`)}
          style={{ opacity: t.estado === 'CERRADO' ? 0.6 : 1, cursor: t.estado === 'CERRADO' ? 'default' : 'pointer' }}
        >
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <h4>{t.titulo}</h4>
            <span className={`badge ${ESTADO_BADGE[t.estado] || 'info'}`} style={{ fontSize: '0.6rem' }}>{t.estado ? t.estado.replace('_', ' ') : ''}</span>
          </div>
          <div className="meta">{t.ticketId.slice(0,8)} · {t.descripcion?.slice(0,30)}...</div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span className={`badge ${t.prioridad ? t.prioridad.toLowerCase() : 'media'}`}>{t.prioridad ? t.prioridad.toUpperCase() : ''}</span>
          </div>
        </div>
      ))}
    </MobileLayout>
  )
}

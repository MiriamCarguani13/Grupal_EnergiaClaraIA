import { useParams, useNavigate } from 'react-router-dom'
import { useEffect, useState } from 'react'
import MobileLayout from '../components/MobileLayout'
import { ticketService } from '../services/ticketService'
import { useAuth } from '../context/AuthContext'

const CHECKLIST = [
  'Verificación temporizador iluminación',
  'Revisión cuadro eléctrico',
  'Reemplazo componente defectuoso',
  'Prueba de funcionamiento',
]

export default function MobileCierrePage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { auth } = useAuth()
  const [ticket, setTicket] = useState(null)
  const [loading, setLoading] = useState(true)
  const [checks, setChecks] = useState({})
  const [descripcion, setDescripcion] = useState('')
  const [hasFoto, setHasFoto] = useState(false)
  const [submitted, setSubmitted] = useState(false)

  useEffect(() => {
    ticketService.getTicketById(id).then(data => {
      setTicket(data)
      setLoading(false)
    }).catch(err => {
      console.error(err)
      setLoading(false)
    })
  }, [id])

  if (loading) {
    return <MobileLayout title="Cargando..." backTo="/m/tickets"><p>Cargando ticket...</p></MobileLayout>
  }

  if (!ticket) {
    return (
      <MobileLayout title="Cierre" backTo="/m/tickets">
        <div className="alert alert-danger">Ticket no encontrado.</div>
      </MobileLayout>
    )
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await ticketService.closeTicket(id, auth.userId, "QR-FALSO-123")
      setSubmitted(true)
      setTimeout(() => navigate('/m/tickets'), 1500)
    } catch(err) {
      alert("Error al cerrar el ticket")
      console.error(err)
    }
  }

  if (ticket.estado === 'CERRADO') {
    return (
      <MobileLayout title={`Ticket ${ticket.ticketId.slice(0,8)}`} backTo="/m/tickets">
        <div className="alert alert-success" style={{ marginTop: '1rem' }}>
          <span className="icon">✓</span>
          <div><strong>Este ticket ya ha sido cerrado.</strong></div>
        </div>
      </MobileLayout>
    )
  }

  return (
    <MobileLayout title={`Cerrar ${ticket.ticketId.slice(0,8)}`} backTo="/m/tickets">

      <div className="card" style={{ marginBottom: '1rem', padding: '0.875rem' }}>
        <h4 style={{ fontSize: '0.95rem' }}>{ticket.titulo}</h4>
        <p style={{ fontSize: '0.75rem', color: 'var(--gray-500)', marginTop: '0.25rem' }}>{ticket.descripcion}</p>
        <p style={{ fontSize: '0.75rem', marginTop: '0.5rem' }}><span className={`badge ${ticket.prioridad ? ticket.prioridad.toLowerCase() : 'media'}`}>{ticket.prioridad ? ticket.prioridad.toUpperCase() : ''}</span></p>
      </div>

      <form onSubmit={handleSubmit}>
        <h4 style={{ fontSize: '0.9rem', marginBottom: '0.75rem' }}>Checklist</h4>
        {CHECKLIST.map((item, idx) => (
          <div key={idx} className="checklist-item">
            <input
              type="checkbox"
              checked={!!checks[idx]}
              onChange={(e) => setChecks({ ...checks, [idx]: e.target.checked })}
              id={`check-${idx}`}
            />
            <label htmlFor={`check-${idx}`}>{item}</label>
          </div>
        ))}

        <h4 style={{ fontSize: '0.9rem', margin: '1rem 0 0.5rem' }}>Evidencia fotográfica</h4>
        <div
          onClick={() => setHasFoto(!hasFoto)}
          style={{
            border: `2px dashed ${hasFoto ? 'var(--green)' : 'var(--gray-300)'}`,
            borderRadius: 8,
            padding: '1.5rem',
            textAlign: 'center',
            color: 'var(--gray-500)',
            background: hasFoto ? 'var(--green-50)' : 'var(--gray-50)',
            cursor: 'pointer',
          }}
        >
          <div style={{ fontSize: '1.75rem', marginBottom: '0.5rem' }}>{hasFoto ? '✓' : '📷'}</div>
          <p style={{ fontSize: '0.85rem', fontWeight: 600 }}>{hasFoto ? 'Foto capturada' : 'Tocar para capturar'}</p>
        </div>

        <h4 style={{ fontSize: '0.9rem', margin: '1rem 0 0.5rem' }}>Descripción del trabajo</h4>
        <textarea
          rows="4"
          value={descripcion}
          onChange={(e) => setDescripcion(e.target.value)}
          placeholder="Detalle el trabajo realizado..."
          required
          style={{
            width: '100%',
            padding: '0.625rem',
            border: '1.5px solid var(--gray-300)',
            borderRadius: 8,
            fontSize: '0.85rem',
            resize: 'vertical',
            fontFamily: 'inherit',
          }}
        />

        {submitted && (
          <div className="alert alert-success" style={{ marginTop: '1rem' }}>
            <span className="icon">✓</span>
            <div><strong>Ticket cerrado correctamente.</strong></div>
          </div>
        )}

        <button type="submit" className="btn btn-primary btn-block" style={{ marginTop: '1.25rem' }}>
          ✓ Cerrar Ticket
        </button>
      </form>
    </MobileLayout>
  )
}

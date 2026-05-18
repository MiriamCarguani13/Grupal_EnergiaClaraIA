import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import MobileLayout from '../components/MobileLayout'
import { getMockTicketById } from '../services/mockService'

const CHECKLIST = [
  'Verificación temporizador iluminación',
  'Revisión cuadro eléctrico',
  'Reemplazo componente defectuoso',
  'Prueba de funcionamiento',
]

export default function MobileCierrePage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const ticket = getMockTicketById(id)
  const [checks, setChecks] = useState({})
  const [descripcion, setDescripcion] = useState('')
  const [hasFoto, setHasFoto] = useState(false)
  const [submitted, setSubmitted] = useState(false)

  if (!ticket) {
    return (
      <MobileLayout title="Cierre" backTo="/m/tickets">
        <div className="alert alert-danger">Ticket no encontrado.</div>
      </MobileLayout>
    )
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    setSubmitted(true)
    setTimeout(() => navigate('/m/tickets'), 1500)
  }

  return (
    <MobileLayout title={`Cerrar ${ticket.id}`} backTo="/m/tickets">
      <div className="alert alert-warning" style={{ marginBottom: '1rem', fontSize: '0.75rem', padding: '0.5rem' }}>
        <span className="icon">ℹ</span>
        <div>Cierre mock - no persiste</div>
      </div>

      <div className="card" style={{ marginBottom: '1rem', padding: '0.875rem' }}>
        <h4 style={{ fontSize: '0.95rem' }}>{ticket.titulo}</h4>
        <p style={{ fontSize: '0.75rem', color: 'var(--gray-500)', marginTop: '0.25rem' }}>{ticket.area}</p>
        <p style={{ fontSize: '0.75rem', marginTop: '0.5rem' }}><span className={`badge ${ticket.prioridad}`}>{ticket.prioridad.toUpperCase()}</span></p>
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
            <div><strong>Ticket cerrado (mock).</strong></div>
          </div>
        )}

        <button type="submit" className="btn btn-primary btn-block" style={{ marginTop: '1.25rem' }}>
          ✓ Cerrar Ticket
        </button>
      </form>
    </MobileLayout>
  )
}

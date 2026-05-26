import { useState } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import AppLayout from '../components/AppLayout'
import { mockSlaTable } from '../services/mockService'
import { ticketService } from '../services/ticketService'
import { useAuth } from '../context/AuthContext'
import { useEffect } from 'react'

const PRIORIDADES = [
  { key: 'baja', label: 'BAJA' },
  { key: 'media', label: 'MEDIA' },
  { key: 'alta', label: 'ALTA' },
  { key: 'critica', label: 'CRÍTICA' },
]

export default function CrearTicketPage() {
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const { auth } = useAuth()
  const anomaliaId = params.get('anomalia')

  const [prioridad, setPrioridad] = useState('critica')
  const [tecnicoId, setTecnicoId] = useState('')
  const [technicians, setTechnicians] = useState([])
  const [loading, setLoading] = useState(true)

  const [form, setForm] = useState({
    area: 'Bloque B - Aula 3B',
    equipo: 'Sistema A/C + Iluminación',
    descripcion: anomaliaId
      ? `Ticket generado desde anomalía ${anomaliaId.slice(0, 8)}. Verificar equipos involucrados.`
      : '',
    categoria: 'Mantenimiento Correctivo',
    tipo: 'Verificación + Reparación',
  })
  const [submitted, setSubmitted] = useState(false)

  useEffect(() => {
    ticketService.getTechnicians().then((data) => {
      setTechnicians(data)
      if (data.length > 0) setTecnicoId(data[0].id)
      setLoading(false)
    }).catch(err => {
      console.error(err)
      alert("Error al cargar técnicos: " + (err.response?.data?.message || err.message))
      setLoading(false)
    })
  }, [])

  const handleSubmit = async (e) => {
    e.preventDefault()
    
    try {
      const payload = {
        inquilinoId: auth.tenantId,
        edificioId: "22222222-2222-2222-2222-222222222222", // demo
        anomaliaId: anomaliaId || null,
        politicaSlaId: "00000000-0000-0000-0000-000000000000", // demo
        titulo: form.tipo + ' - ' + form.area,
        descripcion: form.descripcion,
        prioridad: prioridad.toUpperCase(),
        creadoPor: auth.userId
      }
      
      const created = await ticketService.createTicket(payload)
      
      if (tecnicoId) {
        await ticketService.assignTicket(created.ticketId, tecnicoId)
      }
      
      setSubmitted(true)
      setTimeout(() => navigate('/dashboard'), 1500)
    } catch (err) {
      alert("Error al crear ticket")
      console.error(err)
    }
  }

  return (
    <AppLayout title="Crear Ticket de Mantenimiento">

      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '1.25rem', maxWidth: 1100 }}>
        <form className="card" onSubmit={handleSubmit}>
          <div className="card-title">Nuevo Ticket {anomaliaId && `- Desde Anomalía ${anomaliaId.slice(0, 8)}`}</div>

          {anomaliaId && (
            <div className="alert alert-success" style={{ marginBottom: '1.25rem' }}>
              <span className="icon">✓</span>
              <div>
                <strong>Pre-cargado desde anomalía</strong>
                <p style={{ fontSize: '0.8rem', marginTop: '0.25rem' }}>ID: {anomaliaId}</p>
              </div>
            </div>
          )}

          <div className="form-row">
            <div className="form-group">
              <label>Área afectada *</label>
              <input value={form.area} onChange={(e) => setForm({ ...form, area: e.target.value })} required />
            </div>
            <div className="form-group">
              <label>Equipo *</label>
              <input value={form.equipo} onChange={(e) => setForm({ ...form, equipo: e.target.value })} required />
            </div>
          </div>

          <div className="form-group">
            <label>Descripción *</label>
            <textarea
              rows="3"
              value={form.descripcion}
              onChange={(e) => setForm({ ...form, descripcion: e.target.value })}
              required
              style={{ resize: 'vertical' }}
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Categoría</label>
              <select value={form.categoria} onChange={(e) => setForm({ ...form, categoria: e.target.value })}>
                <option>Mantenimiento Correctivo</option>
                <option>Mantenimiento Preventivo</option>
              </select>
            </div>
            <div className="form-group">
              <label>Tipo de intervención</label>
              <select value={form.tipo} onChange={(e) => setForm({ ...form, tipo: e.target.value })}>
                <option>Verificación + Reparación</option>
                <option>Solo inspección</option>
              </select>
            </div>
          </div>

          <div className="form-group">
            <label>Prioridad *</label>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '0.5rem' }}>
              {PRIORIDADES.map((p) => (
                <div
                  key={p.key}
                  onClick={() => setPrioridad(p.key)}
                  style={{
                    padding: '0.6rem',
                    border: '1.5px solid var(--gray-300)',
                    borderRadius: 8,
                    textAlign: 'center',
                    cursor: 'pointer',
                    fontSize: '0.8rem',
                    fontWeight: 700,
                    background: prioridad === p.key ? `var(--${p.key === 'critica' ? 'red' : p.key === 'alta' ? 'orange' : p.key === 'media' ? 'yellow' : 'teal'})` : 'var(--white)',
                    color: prioridad === p.key ? 'var(--white)' : 'var(--gray-700)',
                  }}
                >
                  {p.label}
                </div>
              ))}
            </div>
          </div>

          <div className={`alert ${prioridad === 'critica' ? 'alert-danger' : 'alert-info'}`} style={{ marginBottom: '1.25rem' }}>
            <span className="icon">⏱</span>
            <div>
              <strong>SLA: {mockSlaTable[prioridad]} horas</strong>
              <p style={{ fontSize: '0.8rem', marginTop: '0.25rem' }}>El técnico asignado será notificado.</p>
            </div>
          </div>

          <div className="form-group">
            <label>Asignar Técnico *</label>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '0.75rem' }}>
              {loading ? <p>Cargando técnicos...</p> : technicians.map((t) => {
                const isBusy = t.estado !== 'disponible'
                const selected = tecnicoId === t.id
                return (
                  <div
                    key={t.id}
                    onClick={() => !isBusy && setTecnicoId(t.id)}
                    style={{
                      border: `1.5px solid ${selected ? 'var(--green)' : 'var(--gray-300)'}`,
                      borderRadius: 8,
                      padding: '0.875rem',
                      textAlign: 'center',
                      cursor: isBusy ? 'not-allowed' : 'pointer',
                      opacity: isBusy ? 0.5 : 1,
                      background: selected ? 'var(--green-light)' : 'var(--white)',
                    }}
                  >
                    <div style={{ width: '2.5rem', height: '2.5rem', background: selected ? 'var(--green)' : 'var(--gray-300)', borderRadius: '50%', margin: '0 auto 0.5rem', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--white)', fontWeight: 700, fontSize: '0.8rem' }}>
                      {t.nombre.substring(0, 2).toUpperCase()}
                    </div>
                    <strong style={{ fontSize: '0.85rem' }}>{t.nombre}</strong>
                    <p style={{ fontSize: '0.7rem', color: isBusy ? 'var(--gray-500)' : 'var(--green-dark)', marginTop: '0.25rem' }}>
                      {isBusy ? `En ${t.ticketActual}` : 'Disponible'}
                    </p>
                  </div>
                )
              })}
            </div>
          </div>

          {submitted && (
            <div className="alert alert-success" style={{ marginBottom: '1rem' }}>
              <span className="icon">✓</span>
              <div><strong>Ticket asignado exitosamente al técnico.</strong> Redirigiendo...</div>
            </div>
          )}

          <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'flex-end', marginTop: '1.5rem', paddingTop: '1.5rem', borderTop: '1px solid var(--gray-200)' }}>
            <button type="button" className="btn btn-secondary" onClick={() => navigate(-1)}>Cancelar</button>
            <button type="submit" className="btn btn-primary">✓ Crear y Asignar Ticket</button>
          </div>
        </form>

        <div>
          {anomaliaId && (
            <div className="card" style={{ background: 'var(--green-50)', border: '1px solid var(--green)', marginBottom: '1rem' }}>
              <div className="card-title">📌 Anomalía Origen</div>
              <p style={{ fontSize: '0.85rem' }}>ID: <strong>{anomaliaId.slice(0, 8)}</strong></p>
            </div>
          )}
          <div className="card">
            <div className="card-title">📊 SLA por Prioridad</div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', fontSize: '0.85rem' }}>
              {PRIORIDADES.map((p) => (
                <div key={p.key} style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span><span className={`badge ${p.key}`}>{p.label}</span></span>
                  <span>{mockSlaTable[p.key]} hs</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </AppLayout>
  )
}

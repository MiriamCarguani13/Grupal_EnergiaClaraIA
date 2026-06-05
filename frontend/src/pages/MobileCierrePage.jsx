import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import MobileLayout from '../components/MobileLayout'
import { getMaintenanceTicket, updateTechnicalSheet } from '../services/maintenanceService'
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
  const [checks, setChecks] = useState({})
  const [form, setForm] = useState({
    technicalDiagnosis: '',
    appliedSolution: '',
    usedMaterials: '',
    technicalNotes: '',
    technicalStatus: 'EN_REVISION',
  })
  const [hasFoto, setHasFoto] = useState(false)
  const [evidencePreview, setEvidencePreview] = useState('')
  const [evidenceImageDataUrl, setEvidenceImageDataUrl] = useState('')
  const [submitted, setSubmitted] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    getMaintenanceTicket(id)
      .then((data) => {
        setTicket(data)
        setForm({
          technicalDiagnosis: data.technicalDiagnosis || '',
          appliedSolution: data.appliedSolution || '',
          usedMaterials: data.usedMaterials || '',
          technicalNotes: data.technicalNotes || '',
          technicalStatus: data.technicalStatus || 'EN_REVISION',
        })
        setEvidencePreview(data.evidenceImageUrl || '')
      })
      .catch((err) => setError(err.response?.data?.message || 'Ticket no encontrado.'))
      .finally(() => setLoading(false))
  }, [id])

  if (loading) {
    return (
      <MobileLayout title="Ficha técnica" backTo="/m/tickets">
        <div className="alert alert-info">Cargando ticket...</div>
      </MobileLayout>
    )
  }

  if (!ticket || error) {
    return (
      <MobileLayout title="Cierre" backTo="/m/tickets">
        <div className="alert alert-danger">{error || 'Ticket no encontrado.'}</div>
      </MobileLayout>
    )
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    try {
      const updated = await updateTechnicalSheet(id, { ...form, evidenceImageDataUrl })
      setTicket(updated)
      setEvidencePreview(updated.evidenceImageUrl || evidencePreview)
      setEvidenceImageDataUrl('')
      setSubmitted(true)
      setTimeout(() => navigate('/m/tickets'), 1200)
    } catch (err) {
      setError(err.response?.data?.message || 'No se pudo guardar la ficha tecnica.')
    }
  }

  const readOnly = ['REPARADO', 'CERRADO'].includes(ticket.status) && !auth?.roles?.includes('ADMIN_INSTITUCION')

  const handleEvidenceChange = (event) => {
    const file = event.target.files?.[0]
    setError('')
    if (!file) return
    if (!['image/jpeg', 'image/jpg', 'image/png'].includes(file.type)) {
      setError('La evidencia debe ser JPG o PNG.')
      return
    }
    if (file.size > 5 * 1024 * 1024) {
      setError('La evidencia no debe superar 5MB.')
      return
    }
    const reader = new FileReader()
    reader.onload = () => {
      setEvidenceImageDataUrl(String(reader.result))
      setEvidencePreview(String(reader.result))
      setHasFoto(true)
    }
    reader.readAsDataURL(file)
  }

  return (
    <MobileLayout title={`Ficha ${ticket.id.slice(0, 8)}`} backTo="/m/tickets">
      <div className="card" style={{ marginBottom: '1rem', padding: '0.875rem' }}>
        <h4 style={{ fontSize: '0.95rem' }}>{ticket.title}</h4>
        <p style={{ fontSize: '0.75rem', color: 'var(--gray-500)', marginTop: '0.25rem' }}>{ticket.description}</p>
        <p style={{ fontSize: '0.75rem', marginTop: '0.5rem', display: 'flex', gap: '0.5rem' }}>
          <span className="badge alta">{ticket.priority}</span>
          <span className={`badge ${readOnly ? 'success' : 'info'}`}>{ticket.status.replace('_', ' ')}</span>
        </p>
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
              disabled={readOnly}
            />
            <label htmlFor={`check-${idx}`}>{item}</label>
          </div>
        ))}

        <h4 style={{ fontSize: '0.9rem', margin: '1rem 0 0.5rem' }}>Evidencia fotográfica</h4>
        <label
          style={{
            display: 'block',
            border: `2px dashed ${hasFoto || evidencePreview ? 'var(--green)' : 'var(--gray-300)'}`,
            borderRadius: 8,
            padding: '1.5rem',
            textAlign: 'center',
            color: 'var(--gray-500)',
            background: hasFoto || evidencePreview ? 'var(--green-50)' : 'var(--gray-50)',
            cursor: readOnly ? 'default' : 'pointer',
          }}
        >
          {evidencePreview ? (
            <img
              src={evidencePreview}
              alt="Evidencia fotográfica"
              style={{ width: '100%', maxHeight: 180, objectFit: 'cover', borderRadius: 8, marginBottom: '0.75rem' }}
            />
          ) : (
            <div style={{ fontSize: '1.75rem', marginBottom: '0.5rem' }}>📷</div>
          )}
          <p style={{ fontSize: '0.85rem', fontWeight: 600 }}>
            {readOnly ? 'Evidencia guardada' : evidencePreview ? 'Cambiar evidencia' : 'Seleccionar evidencia'}
          </p>
          {!readOnly && (
            <input type="file" accept="image/jpeg,image/png" onChange={handleEvidenceChange} style={{ display: 'none' }} />
          )}
        </label>

        <h4 style={{ fontSize: '0.9rem', margin: '1rem 0 0.5rem' }}>Diagnóstico técnico</h4>
        <textarea
          rows="4"
          value={form.technicalDiagnosis}
          onChange={(e) => setForm({ ...form, technicalDiagnosis: e.target.value })}
          placeholder="Detalle el diagnóstico técnico..."
          required
          disabled={readOnly}
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

        <h4 style={{ fontSize: '0.9rem', margin: '1rem 0 0.5rem' }}>Solución aplicada</h4>
        <textarea
          rows="3"
          value={form.appliedSolution}
          onChange={(e) => setForm({ ...form, appliedSolution: e.target.value })}
          placeholder="Describa la solución aplicada..."
          required
          disabled={readOnly}
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

        <h4 style={{ fontSize: '0.9rem', margin: '1rem 0 0.5rem' }}>Materiales utilizados</h4>
        <input
          value={form.usedMaterials}
          onChange={(e) => setForm({ ...form, usedMaterials: e.target.value })}
          placeholder="Ej. fusible, contactor, cableado..."
          disabled={readOnly}
          style={{
            width: '100%',
            padding: '0.625rem',
            border: '1.5px solid var(--gray-300)',
            borderRadius: 8,
            fontSize: '0.85rem',
            fontFamily: 'inherit',
          }}
        />

        <h4 style={{ fontSize: '0.9rem', margin: '1rem 0 0.5rem' }}>Estado técnico</h4>
        <select
          value={form.technicalStatus}
          onChange={(e) => setForm({ ...form, technicalStatus: e.target.value })}
          disabled={readOnly}
          style={{
            width: '100%',
            padding: '0.625rem',
            border: '1.5px solid var(--gray-300)',
            borderRadius: 8,
            fontSize: '0.85rem',
            fontFamily: 'inherit',
          }}
        >
          <option value="EN_REVISION">En revisión</option>
          <option value="REPARADO">Reparado</option>
          <option value="REQUIERE_REPUESTO">Requiere repuesto</option>
          <option value="ESCALADO">Escalado</option>
        </select>

        <h4 style={{ fontSize: '0.9rem', margin: '1rem 0 0.5rem' }}>Observaciones</h4>
        <textarea
          rows="3"
          value={form.technicalNotes}
          onChange={(e) => setForm({ ...form, technicalNotes: e.target.value })}
          placeholder="Observaciones adicionales..."
          disabled={readOnly}
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

        {error && <div className="alert alert-danger" style={{ marginTop: '1rem' }}>{error}</div>}

        {submitted && (
          <div className="alert alert-success" style={{ marginTop: '1rem' }}>
            <span className="icon">✓</span>
            <div><strong>Ficha técnica guardada.</strong></div>
          </div>
        )}

        {readOnly ? (
          <div className="alert alert-info" style={{ marginTop: '1rem' }}>Ticket resuelto. La ficha técnica está en modo lectura.</div>
        ) : (
          <button type="submit" className="btn btn-primary btn-block" style={{ marginTop: '1.25rem' }}>
            ✓ Guardar ficha técnica
          </button>
        )}
      </form>
    </MobileLayout>
  )
}

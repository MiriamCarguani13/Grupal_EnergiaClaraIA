import { useState } from 'react'
import AppLayout from '../components/AppLayout'
import { analyzeReading } from '../services/energyOpsService'

export default function RegistroLecturaPage() {
  const [form, setForm] = useState({
    facilityId: 'Sede Central - Bloque B - Aula 3B',
    meterId: 'MED-DEMO-001',
    measuredAt: new Date().toISOString().slice(0, 16),
    kwh: 1248,
    voltage: 220,
    powerFactor: 0.95,
    observaciones: '',
  })
  const [result, setResult] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    setError('')
    setResult(null)
    try {
      const payload = {
        facilityId: form.facilityId,
        meterId: form.meterId,
        measuredAt: new Date(form.measuredAt).toISOString(),
        kwh: parseFloat(form.kwh),
        voltage: form.voltage ? parseFloat(form.voltage) : null,
        powerFactor: form.powerFactor ? parseFloat(form.powerFactor) : null,
      }
      const data = await analyzeReading(payload)
      setResult(data)
    } catch (err) {
      setError(err.response?.data?.message || 'Error registrando lectura')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AppLayout title="Registrar Lectura Energética">
      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '1.25rem', maxWidth: 1100 }}>
        <div className="card">
          <div className="card-title">Nueva Lectura</div>

          <form onSubmit={handleSubmit}>
            <div className="form-row">
              <div className="form-group">
                <label>Medidor (etiqueta) *</label>
                <input name="meterId" value={form.meterId} onChange={handleChange} required />
              </div>
              <div className="form-group">
                <label>Periodo *</label>
                <input name="measuredAt" type="datetime-local" value={form.measuredAt} onChange={handleChange} required />
              </div>
            </div>

            <div className="form-group">
              <label>Sede / Área *</label>
              <input name="facilityId" value={form.facilityId} onChange={handleChange} required />
            </div>

            <div className="form-group">
              <label>Valor de Consumo (kWh) *</label>
              <input
                name="kwh"
                type="number"
                step="0.01"
                value={form.kwh}
                onChange={handleChange}
                required
                style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--green-dark)', textAlign: 'center' }}
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Voltaje</label>
                <input name="voltage" type="number" step="0.1" value={form.voltage} onChange={handleChange} />
              </div>
              <div className="form-group">
                <label>Factor de Potencia</label>
                <input name="powerFactor" type="number" step="0.01" value={form.powerFactor} onChange={handleChange} />
              </div>
            </div>

            <div className="form-group">
              <label>Observaciones</label>
              <textarea name="observaciones" rows="3" value={form.observaciones} onChange={handleChange} style={{ resize: 'vertical' }} />
            </div>

            {error && <div className="alert alert-danger" style={{ marginBottom: '1rem' }}><span className="icon">⚠</span><div>{error}</div></div>}

            {result && (
              <div className={`alert ${result.anomalyDetected ? 'alert-warning' : 'alert-success'}`} style={{ marginBottom: '1rem' }}>
                <span className="icon">{result.anomalyDetected ? '⚠' : '✓'}</span>
                <div>
                  <strong>
                    {result.anomalyDetected
                      ? `Anomalía ${result.severity} detectada (desv. ${result.deviationPercent}%)`
                      : 'Lectura registrada sin anomalía'}
                  </strong>
                  <p style={{ fontSize: '0.8rem', marginTop: '0.25rem' }}>
                    {result.recommendation} {result.anomalyId && `· ID: ${result.anomalyId}`}
                  </p>
                  {result.estimatedCostImpact && (
                    <p style={{ fontSize: '0.8rem', marginTop: '0.25rem' }}>
                      Impacto: Bs. {result.estimatedCostImpact} · CO₂: {result.estimatedCo2Impact} kg
                    </p>
                  )}
                </div>
              </div>
            )}

            <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'flex-end', marginTop: '1.5rem' }}>
              <button type="button" className="btn btn-secondary" onClick={() => { setResult(null); setError('') }}>
                Limpiar
              </button>
              <button type="submit" disabled={submitting} className="btn btn-primary">
                {submitting ? 'Registrando...' : '✓ Registrar Lectura'}
              </button>
            </div>
          </form>
        </div>

        <div>
          <div className="card" style={{ marginBottom: '1rem' }}>
            <div className="card-title">Tip de uso</div>
            <p style={{ fontSize: '0.85rem', color: 'var(--gray-700)', lineHeight: 1.5 }}>
              El backend compara la lectura contra el <strong>baseline activo</strong> del medidor.
              Si la desviación supera la tolerancia, se crea una anomalía automáticamente con score, severidad e impacto.
            </p>
          </div>

          <div className="card">
            <div className="card-title">Endpoints conectados</div>
            <code style={{ fontSize: '0.75rem', color: 'var(--green-dark)' }}>POST /api/energyops/analyze-reading</code>
          </div>
        </div>
      </div>
    </AppLayout>
  )
}

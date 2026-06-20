import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { loginRequest } from '../services/authService'

const DEMO_TENANT_ID = '11111111-1111-1111-1111-111111111111'

export default function LoginPage() {
  const navigate = useNavigate()
  const { login } = useAuth()
  const [form, setForm] = useState({ email: '', password: '', tenantId: DEMO_TENANT_ID })
  const [error, setError] = useState(() => {
    const authError = sessionStorage.getItem('ec_auth_error')
    if (authError) {
      sessionStorage.removeItem('ec_auth_error')
    }
    return authError || ''
  })
  const [loading, setLoading] = useState(false)

  const handleChange = (e) =>
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const data = await loginRequest(form)
      login(data)
      navigate(data.roles?.includes('TECNICO') ? '/m/tickets' : '/dashboard')
    } catch (err) {
      setError(err.response?.data?.message || 'Credenciales inválidas')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-wrapper">
      <section className="login-hero-panel">
        <div className="energy-grid-bg" aria-hidden="true">
          <span className="energy-node node-a" />
          <span className="energy-node node-b" />
          <span className="energy-node node-c" />
          <span className="energy-line line-a" />
          <span className="energy-line line-b" />
        </div>
        <div className="login-brand-block">
          <div className="login-brand-mark">⚡</div>
          <h1>EnergíaClara AI</h1>
          <p>Plataforma inteligente de monitoreo y optimización energética</p>
        </div>
        <div className="login-benefits">
          <div>✓ Detección de anomalías con IA híbrida</div>
          <div>✓ Baseline dinámico y análisis estadístico</div>
          <div>✓ Gestión integrada de mantenimiento</div>
        </div>
        <div className="login-visual-card">
          <div className="visual-card-header">
            <span>Consumo institucional</span>
            <strong>AI Monitor</strong>
          </div>
          <div className="visual-bars">
            <span style={{ height: '42%' }} />
            <span style={{ height: '58%' }} />
            <span style={{ height: '38%' }} />
            <span style={{ height: '76%' }} />
            <span style={{ height: '54%' }} />
            <span style={{ height: '88%' }} />
          </div>
          <div className="visual-signal">
            <span>Baseline dinámico</span>
            <strong>Activo</strong>
          </div>
        </div>
      </section>

      <section className="login-form-panel">
        <div className="login-card">
          <div className="login-header">
            <span className="logo-icon">⚡</span>
            <h2>Iniciar sesión</h2>
            <p>Accede al centro de monitoreo energético</p>
          </div>

        <form onSubmit={handleSubmit} className="login-form">
          <div className="field">
            <label>Correo electrónico</label>
            <input
              type="email"
              name="email"
              value={form.email}
              onChange={handleChange}
              placeholder="usuario@institucion.edu"
              required
            />
          </div>

          <div className="field">
            <label>Contraseña</label>
            <input
              type="password"
              name="password"
              value={form.password}
              onChange={handleChange}
              placeholder="••••••••"
              required
            />
          </div>

          {error && <div className="error-msg">{error}</div>}

          <button type="submit" disabled={loading} className="btn-primary">
            {loading ? 'Validando acceso...' : 'Acceder al sistema'}
          </button>
        </form>
          <p className="login-footnote">Monitoreo energético institucional</p>
        </div>
      </section>
      </div>
  )
}

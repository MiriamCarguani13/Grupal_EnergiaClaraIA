import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const ROLE_LABELS = {
  ADMIN_INSTITUCION: 'Administrador',
  DIRECTOR: 'Director',
  DOCENTE: 'Docente',
  ESTUDIANTE: 'Estudiante',
  TECNICO: 'Técnico',
  AUDITOR: 'Auditor',
}

const NAV_ITEMS = [
  { to: '/dashboard', icon: '📊', label: 'Dashboard' },
  { to: '/lecturas', icon: '⚡', label: 'Lecturas' },
  { to: '/anomalias', icon: '🔍', label: 'Anomalías' },
  { to: '/tickets/nuevo', icon: '🔧', label: 'Tickets' },
  { to: '/retos', icon: '🎯', label: 'Retos' },
]

export default function AppLayout({ title, children }) {
  const { auth, logout } = useAuth()
  const navigate = useNavigate()
  const roleLabel = auth?.roles?.map((r) => ROLE_LABELS[r] ?? r).join(', ')

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="app-wrapper">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <span className="icon">⚡</span>
          <span>EnergíaClara AI</span>
        </div>
        <nav className="sidebar-nav">
          {NAV_ITEMS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => 'nav-item' + (isActive ? ' active' : '')}
            >
              <span className="icon">{item.icon}</span>
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="main-content">
        <header className="topbar">
          <div className="topbar-title">{title}</div>
          <div className="topbar-user">
            <span className="user-email">{auth?.email}</span>
            <span className="role-badge">{roleLabel}</span>
            <button onClick={handleLogout} className="btn btn-secondary" style={{ padding: '0.4rem 0.75rem', fontSize: '0.8rem' }}>
              Salir
            </button>
          </div>
        </header>
        <main className="page-content">{children}</main>
      </div>
    </div>
  )
}

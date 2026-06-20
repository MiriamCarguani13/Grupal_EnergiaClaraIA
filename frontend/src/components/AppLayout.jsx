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
  { to: '/dashboard', label: 'Dashboard', roles: ['ADMIN_INSTITUCION', 'DIRECTOR', 'AUDITOR'] },
  { to: '/lecturas', label: 'Lecturas', roles: ['ADMIN_INSTITUCION', 'DIRECTOR', 'AUDITOR'] },
  { to: '/anomalias', label: 'Anomalías', roles: ['ADMIN_INSTITUCION', 'DIRECTOR', 'AUDITOR'] },
  { to: '/mantenimiento', label: 'Mantenimiento', roles: ['ADMIN_INSTITUCION'] },
  { to: '/m/tickets', label: 'Mantenimiento', roles: ['TECNICO'] },
  { to: '/administracion', label: 'Administración' },
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
      <header className="app-header">
        <NavLink to={auth?.roles?.includes('TECNICO') ? '/m/tickets' : '/dashboard'} className="app-logo">
          <span className="app-logo-mark">⚡</span>
          <span>EnergíaClara AI</span>
        </NavLink>
        <nav className="app-nav">
          {NAV_ITEMS.filter((item) => !item.roles || item.roles.some((role) => auth?.roles?.includes(role))).map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => 'app-nav-item' + (isActive ? ' active' : '')}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="app-header-user">
          <div className="app-user-meta">
            <span className="user-email">{auth?.email}</span>
            <span className="role-badge">{roleLabel}</span>
          </div>
          <button onClick={handleLogout} className="btn btn-secondary app-logout">
            Salir
          </button>
        </div>
      </header>

      <div className="main-content">
        <div className="page-title-strip">{title}</div>
        <main className="page-content">{children}</main>
      </div>
    </div>
  )
}

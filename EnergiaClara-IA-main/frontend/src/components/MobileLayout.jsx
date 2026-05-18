import { useNavigate, useLocation } from 'react-router-dom'

const TABS = [
  { to: '/m/tickets', icon: '🔧', label: 'Tickets' },
  { to: '/m/reto', icon: '🎯', label: 'Reto' },
]

export default function MobileLayout({ title, headerRight, children, backTo }) {
  const navigate = useNavigate()
  const location = useLocation()

  return (
    <div className="mobile-wrapper">
      <div className="mobile-frame">
        <div className="mobile-statusbar">
          <span>9:41</span>
          <span>📶 100%</span>
        </div>
        <header className="mobile-header">
          {backTo ? (
            <button className="icon-btn" onClick={() => navigate(backTo)}>‹</button>
          ) : (
            <span style={{ width: 36 }} />
          )}
          <h2>{title}</h2>
          {headerRight || <span style={{ width: 36 }} />}
        </header>
        <div className="mobile-content">{children}</div>
        <nav className="mobile-bottombar">
          {TABS.map((tab) => {
            const active = location.pathname.startsWith(tab.to)
            return (
              <div
                key={tab.to}
                className={'bottombar-item' + (active ? ' active' : '')}
                onClick={() => navigate(tab.to)}
              >
                <span className="icon">{tab.icon}</span>
                <span>{tab.label}</span>
              </div>
            )
          })}
        </nav>
      </div>
    </div>
  )
}

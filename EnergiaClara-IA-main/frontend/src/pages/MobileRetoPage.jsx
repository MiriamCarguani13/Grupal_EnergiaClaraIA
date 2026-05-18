import MobileLayout from '../components/MobileLayout'
import { mockReto, mockRanking } from '../services/mockService'

export default function MobileRetoPage() {
  return (
    <MobileLayout title="Reto Activo" headerRight={<button className="icon-btn">👤</button>}>
      <div className="alert alert-warning" style={{ marginBottom: '1rem', fontSize: '0.75rem', padding: '0.5rem' }}>
        <span className="icon">ℹ</span><div>Datos mock</div>
      </div>

      <div className="reto-banner">
        <h3>{mockReto.nombre}</h3>
        <p style={{ fontSize: '0.85rem', opacity: 0.95 }}>{mockReto.meta}</p>
        <div className="progress-bar"><div className="progress-fill" style={{ width: `${mockReto.progreso}%` }} /></div>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '0.5rem', fontSize: '0.85rem' }}>
          <span>Progreso</span>
          <strong>{mockReto.progreso}%</strong>
        </div>
      </div>

      <h4 style={{ fontSize: '0.9rem', marginBottom: '0.75rem' }}>🏆 Mi Ranking</h4>
      {mockRanking.map((r) => (
        <div key={r.posicion} className={`ranking-item ${r.esMio ? 'me' : ''}`}>
          <div className={`ranking-position ${r.posicion === 1 ? 'gold' : r.posicion === 2 ? 'silver' : r.posicion === 3 ? 'bronze' : ''}`}>
            {r.posicion}
          </div>
          <div className="ranking-name">{r.nombre}{r.esMio && ' (tú)'}</div>
          <div className="ranking-points">{r.puntos} pts</div>
        </div>
      ))}

      <div className="card" style={{ marginTop: '1rem', background: 'var(--green-50)', border: '1px solid var(--green)' }}>
        <div className="card-title" style={{ fontSize: '0.9rem' }}>⚡ Acción rápida</div>
        <p style={{ fontSize: '0.85rem', color: 'var(--gray-700)' }}>
          Apaga luces y A/C al salir del aula para sumar puntos al ranking.
        </p>
      </div>
    </MobileLayout>
  )
}

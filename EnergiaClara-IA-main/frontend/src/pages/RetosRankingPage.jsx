import AppLayout from '../components/AppLayout'
import { mockReto, mockRanking } from '../services/mockService'

export default function RetosRankingPage() {
  return (
    <AppLayout title="Gestión de Retos y Ranking">
      <div className="alert alert-warning" style={{ marginBottom: '1rem' }}>
        <span className="icon">ℹ</span>
        <div>
          <strong>Pantalla con mocks</strong>
          <p style={{ fontSize: '0.8rem', marginTop: '0.25rem' }}>
            Módulo de retos aún no expone REST. Tablas en DB: <code>educacion.reto</code>, <code>educacion.snapshot_ranking</code>.
          </p>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '1.25rem' }}>
        <div className="card">
          <div className="card-title">🎯 Reto Activo</div>
          <h2 style={{ color: 'var(--green-dark)', marginBottom: '0.5rem' }}>{mockReto.nombre}</h2>
          <p style={{ color: 'var(--gray-500)', marginBottom: '1rem' }}>{mockReto.meta}</p>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '1rem', marginBottom: '1.5rem' }}>
            <div className="kpi-card teal" style={{ padding: '1rem' }}>
              <div className="kpi-label">Progreso</div>
              <div className="kpi-value">{mockReto.progreso}%</div>
            </div>
            <div className="kpi-card info" style={{ padding: '1rem' }}>
              <div className="kpi-label">Participantes</div>
              <div className="kpi-value">{mockReto.participantes}</div>
            </div>
            <div className="kpi-card" style={{ padding: '1rem' }}>
              <div className="kpi-label">Líder</div>
              <div className="kpi-value" style={{ fontSize: '1.1rem' }}>{mockReto.lider}</div>
            </div>
          </div>

          <div className="progress-bar"><div className="progress-fill" style={{ width: `${mockReto.progreso}%` }} /></div>

          <div style={{ marginTop: '1rem', fontSize: '0.85rem', color: 'var(--gray-500)' }}>
            <p>Inicio: {mockReto.fechaInicio} · Fin: {mockReto.fechaFin}</p>
          </div>

          <div className="alert alert-success" style={{ marginTop: '1.5rem' }}>
            <span className="icon">🤖</span>
            <div>
              <strong>Sugerencia IA:</strong>
              <p style={{ fontSize: '0.85rem', marginTop: '0.25rem' }}>
                Incentivar Aula 5A con visibilidad de su impacto en kWh ahorrados. Probabilidad de mantener liderazgo: 78%.
              </p>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="card-title">🏆 Ranking</div>
          <div>
            {mockRanking.map((r) => (
              <div key={r.posicion} className={`ranking-item ${r.esMio ? 'me' : ''}`}>
                <div className={`ranking-position ${r.posicion === 1 ? 'gold' : r.posicion === 2 ? 'silver' : r.posicion === 3 ? 'bronze' : ''}`}>
                  {r.posicion}
                </div>
                <div className="ranking-name">{r.nombre} {r.esMio && <span style={{ fontSize: '0.7rem', color: 'var(--green-dark)' }}>(tú)</span>}</div>
                <div className="ranking-points">{r.puntos} pts</div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </AppLayout>
  )
}

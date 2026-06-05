import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'
import LoginPage from './pages/LoginPage'
import DashboardKpisPage from './pages/DashboardKpisPage'
import RegistroLecturaPage from './pages/RegistroLecturaPage'
import AnomaliasListPage from './pages/AnomaliasListPage'
import AnomaliaDetallePage from './pages/AnomaliaDetallePage'
import CrearTicketPage from './pages/CrearTicketPage'
import RetosRankingPage from './pages/RetosRankingPage'
import MobileTicketsPage from './pages/MobileTicketsPage'
import MobileCierrePage from './pages/MobileCierrePage'
import MobileRetoPage from './pages/MobileRetoPage'

const ADMIN_ROLES = ['ADMIN_INSTITUCION']
const OPS_ROLES = ['ADMIN_INSTITUCION', 'DIRECTOR', 'AUDITOR']
const MAINTENANCE_ROLES = ['TECNICO']

function Protected({ children, roles }) {
  return <ProtectedRoute requiredRoles={roles}>{children}</ProtectedRoute>
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />

          <Route path="/dashboard" element={<Protected roles={OPS_ROLES}><DashboardKpisPage /></Protected>} />
          <Route path="/lecturas" element={<Protected roles={OPS_ROLES}><RegistroLecturaPage /></Protected>} />
          <Route path="/anomalias" element={<Protected roles={OPS_ROLES}><AnomaliasListPage /></Protected>} />
          <Route path="/anomalias/:id" element={<Protected roles={OPS_ROLES}><AnomaliaDetallePage /></Protected>} />
          <Route path="/tickets/nuevo" element={<Protected roles={ADMIN_ROLES}><CrearTicketPage /></Protected>} />
          <Route path="/retos" element={<Protected><RetosRankingPage /></Protected>} />

          <Route path="/m/tickets" element={<Protected roles={MAINTENANCE_ROLES}><MobileTicketsPage /></Protected>} />
          <Route path="/m/cierre/:id" element={<Protected roles={MAINTENANCE_ROLES}><MobileCierrePage /></Protected>} />
          <Route path="/m/reto" element={<Protected><MobileRetoPage /></Protected>} />

          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}

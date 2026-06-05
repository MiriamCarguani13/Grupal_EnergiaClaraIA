import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function ProtectedRoute({ children, requiredRoles }) {
  const { auth } = useAuth()

  if (!auth) return <Navigate to="/login" replace />

  if (requiredRoles && !requiredRoles.some((r) => auth.roles?.includes(r))) {
    const fallback = auth.roles?.includes('TECNICO') ? '/m/tickets' : '/dashboard'
    return <Navigate to={fallback} replace />
  }

  return children
}

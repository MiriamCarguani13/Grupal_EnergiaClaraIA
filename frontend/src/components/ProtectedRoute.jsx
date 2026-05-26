import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function ProtectedRoute({ children, requiredRoles }) {
  const { auth } = useAuth()

  if (!auth) return <Navigate to="/login" replace />

  if (requiredRoles && !requiredRoles.some((r) => auth.roles?.includes(r))) {
    if (auth.roles?.includes('TECNICO')) {
      return <Navigate to="/m/tickets" replace />
    } else if (auth.roles?.includes('ESTUDIANTE')) {
      return <Navigate to="/m/reto" replace />
    }
    return <Navigate to="/dashboard" replace />
  }

  return children
}

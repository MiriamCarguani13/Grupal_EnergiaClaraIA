import { createContext, useContext, useState, useCallback } from 'react'

const AuthContext = createContext(null)

function readStoredAuth() {
  const stored = localStorage.getItem('ec_auth')
  if (!stored) return null

  try {
    const parsed = JSON.parse(stored)
    const valid =
      parsed &&
      typeof parsed.token === 'string' &&
      parsed.token.trim() &&
      Array.isArray(parsed.roles) &&
      parsed.roles.length > 0

    if (valid) return parsed
  } catch {
    // Ignore malformed persisted sessions and force a clean login.
  }

  localStorage.removeItem('ec_auth')
  return null
}

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(readStoredAuth)

  const login = useCallback((authData) => {
    localStorage.setItem('ec_auth', JSON.stringify(authData))
    setAuth(authData)
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('ec_auth')
    setAuth(null)
  }, [])

  return (
    <AuthContext.Provider value={{ auth, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider')
  return ctx
}

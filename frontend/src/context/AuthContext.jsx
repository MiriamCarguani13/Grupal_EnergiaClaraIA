import { createContext, useContext, useState, useCallback, useEffect } from 'react'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => {
    const stored = localStorage.getItem('ec_auth')
    return stored ? JSON.parse(stored) : null
  })

  const login = useCallback((authData) => {
    localStorage.setItem('ec_auth', JSON.stringify(authData))
    setAuth(authData)
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('ec_auth')
    setAuth(null)
  }, [])

  useEffect(() => {
    const handleStorageChange = (e) => {
      if (e.key === 'ec_auth') {
        const newValue = e.newValue ? JSON.parse(e.newValue) : null
        setAuth(newValue)
      }
    }
    window.addEventListener('storage', handleStorageChange)
    return () => window.removeEventListener('storage', handleStorageChange)
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

import axios from 'axios'

export const api = axios.create({ baseURL: '/api' })

api.interceptors.request.use((config) => {
  const stored = localStorage.getItem('ec_auth')
  if (stored) {
    try {
      const { token } = JSON.parse(stored)
      if (token) config.headers.Authorization = `Bearer ${token}`
    } catch {
      localStorage.removeItem('ec_auth')
    }
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    const onLogin = window.location.pathname === '/login'
    if ((status === 401 || status === 403) && !onLogin) {
      localStorage.removeItem('ec_auth')
      sessionStorage.setItem('ec_auth_error', 'Sesión expirada o no autorizada')
      window.location.assign('/login')
    }
    return Promise.reject(error)
  }
)

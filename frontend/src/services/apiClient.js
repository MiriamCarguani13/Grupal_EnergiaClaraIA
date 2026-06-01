import axios from 'axios'

export const api = axios.create({ baseURL: '/api' })

api.interceptors.request.use((config) => {
  const stored = localStorage.getItem('ec_auth')
  if (stored) {
    const { token } = JSON.parse(stored)
    if (token) config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('ec_auth')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

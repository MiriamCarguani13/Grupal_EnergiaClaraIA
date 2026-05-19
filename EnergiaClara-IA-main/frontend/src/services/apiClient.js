import axios from 'axios'

export const api = axios.create({ baseURL: '/api' })

api.interceptors.request.use((config) => {
  const stored = localStorage.getItem('ec_auth')
  if (stored) {
    const parsed = JSON.parse(stored)
    const token = parsed.accessToken || parsed.token
    if (token) config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

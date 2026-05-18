import { api } from './apiClient'

export async function fetchDashboard() {
  const { data } = await api.get('/analytics/dashboard')
  return data
}

export async function fetchKpis() {
  const { data } = await api.get('/analytics/kpis')
  return data
}

export async function fetchAnomalies() {
  const { data } = await api.get('/analytics/anomalies')
  return data
}

export async function fetchAnomalyById(id) {
  const list = await fetchAnomalies()
  return list.find((a) => a.id === id) || null
}

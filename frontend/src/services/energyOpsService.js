import { api } from './apiClient'

export async function analyzeReading(payload) {
  const { data } = await api.post('/energyops/analyze-reading', payload)
  return data
}

export async function fetchEnergyReadings() {
  const { data } = await api.get('/energyops/readings')
  return data
}

export async function fetchEnergyReadingTrends() {
  const { data } = await api.get('/energyops/readings/trends')
  return data
}

import { api } from './apiClient'

export async function analyzeReading(payload) {
  const { data } = await api.post('/energyops/analyze-reading', payload)
  return data
}

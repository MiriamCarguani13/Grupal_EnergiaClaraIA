import { api } from './apiClient'

export async function getMaintenanceTickets() {
  const { data } = await api.get('/maintenance/tickets')
  return data
}

export async function getMaintenanceTicket(id) {
  const { data } = await api.get(`/maintenance/tickets/${id}`)
  return data
}

export async function createMaintenanceTicket(payload) {
  const { data } = await api.post('/maintenance/tickets', payload)
  return data
}

export async function updateTechnicalSheet(id, payload) {
  const { data } = await api.put(`/maintenance/tickets/${id}/technical-sheet`, payload)
  return data
}

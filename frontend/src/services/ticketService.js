import { api } from './apiClient'

export const ticketService = {
  getTechnicians: async () => {
    const res = await api.get('/auth/users?role=TECNICO')
    // Mapear al formato que espera el frontend
    return res.data.map((u) => ({
      id: u.id,
      nombre: u.fullName,
      estado: 'disponible', // por ahora mockeamos el estado
      ticketActual: null,
    }))
  },

  getTickets: async () => {
    const res = await api.get('/maintenance/tickets')
    return res.data
  },

  getTicketById: async (id) => {
    const res = await api.get(`/maintenance/tickets/${id}`)
    return res.data
  },

  createTicket: async (payload) => {
    const res = await api.post('/maintenance/tickets', payload)
    return res.data
  },

  assignTicket: async (ticketId, tecnicoId) => {
    const res = await api.put(`/maintenance/tickets/${ticketId}/assign`, { tecnicoId })
    return res.data
  },

  closeTicket: async (ticketId, tecnicoId, qrHash) => {
    const res = await api.post(`/maintenance/tickets/${ticketId}/close`, { tecnicoId, qrHash })
    return res.data
  },
}

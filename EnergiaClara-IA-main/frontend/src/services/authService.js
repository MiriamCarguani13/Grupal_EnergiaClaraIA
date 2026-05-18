import { api } from './apiClient'

export { api }

export async function loginRequest({ email, password, tenantId }) {
  const { data } = await api.post('/auth/login', { email, password, tenantId })
  return data
}

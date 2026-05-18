// Mock data — pantallas 4-7 sin backend REST aún.
// TODO: reemplazar por endpoints reales cuando exista módulo tickets/retos.

export const mockTickets = [
  {
    id: 'TK-042',
    titulo: 'Reemplazo luminaria Aula 3B',
    area: 'Bloque B › Aula 3B',
    prioridad: 'critica',
    estado: 'EN_PROCESO',
    tecnico: 'Juan Pérez',
    slaHorasRestantes: 2.5,
    descripcion: 'Falla en temporizador de iluminación nocturna.',
    creadoEn: '2026-05-06T02:30:00Z',
  },
  {
    id: 'TK-041',
    titulo: 'Revisión A/C Lab. Informática',
    area: 'Bloque A › Lab. Inf.',
    prioridad: 'alta',
    estado: 'ASIGNADO',
    tecnico: 'María García',
    slaHorasRestantes: 5.0,
    descripcion: 'Pico de consumo durante horario.',
    creadoEn: '2026-05-05T09:00:00Z',
  },
  {
    id: 'TK-040',
    titulo: 'Calibración medidor Bloque C',
    area: 'Bloque C',
    prioridad: 'media',
    estado: 'CERRADO',
    tecnico: 'Pedro Rojas',
    slaHorasRestantes: 0,
    descripcion: 'Calibración trimestral.',
    creadoEn: '2026-05-04T11:00:00Z',
  },
]

export const mockTechnicians = [
  { id: 'JP', nombre: 'Juan Pérez', estado: 'disponible' },
  { id: 'MG', nombre: 'María García', estado: 'ocupado', ticketActual: 'TK-041' },
  { id: 'PR', nombre: 'Pedro Rojas', estado: 'disponible' },
]

export const mockReto = {
  id: 'RETO-2026-05',
  nombre: 'Mes de Ahorro Energético',
  meta: 'Reducir 10% vs abril',
  progreso: 72,
  participantes: 18,
  lider: 'Aula 5A',
  fechaInicio: '2026-05-01',
  fechaFin: '2026-05-31',
}

export const mockRanking = [
  { posicion: 1, nombre: 'Aula 5A', puntos: 1240, esMio: false },
  { posicion: 2, nombre: 'Aula 3B', puntos: 1180, esMio: true },
  { posicion: 3, nombre: 'Aula 4A', puntos: 1095, esMio: false },
  { posicion: 4, nombre: 'Lab. Informática', puntos: 980, esMio: false },
  { posicion: 5, nombre: 'Oficina Admin', puntos: 870, esMio: false },
]

export const mockSlaTable = {
  baja: 72,
  media: 24,
  alta: 8,
  critica: 4,
}

export function getMockTicketById(id) {
  return mockTickets.find((t) => t.id === id) || null
}

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
  User,
  Ticket,
  HistoryItem,
  Analyst,
  DashboardStats,
  Category,
  Priority,
  ApiTicket,
  ApiHistoryItem,
  Anexo,
} from '@/types/index'
import api from '@/services/api'
import { jwtDecode } from 'jwt-decode'

// Re-exportar Ticket para uso em outros componentes
export type { Ticket }


export function calculateSlaDeadline(openedAt: string, priority: Ticket['priority']): Date {
  const startDate = new Date(openedAt)
  switch (priority) {
    case 'Crítica':
      startDate.setHours(startDate.getHours() + 2)
      return startDate
    case 'Alta':
    case 'Alto':
      startDate.setHours(startDate.getHours() + 8)
      return startDate
    case 'Média':
    case 'Medio':
      startDate.setHours(startDate.getHours() + 24)
      return startDate
    case 'Baixa':
    case 'Baixo':
      startDate.setDate(startDate.getDate() + 2)
      return startDate
    default:
      startDate.setHours(startDate.getHours() + 24)
      return startDate
  }
}

export const useTicketStore = defineStore('tickets', () => {
  const currentUser = ref<User>({ name: '', email: '', role: null })
  const token = ref(sessionStorage.getItem('token') || '')
  const tickets = ref<Ticket[]>([])
  const activeTicket = ref<Ticket | null>(null)
  const categories = ref<Category[]>([])
  const priorities = ref<Priority[]>([])
  const analysts = ref<Analyst[]>([])
  const dashboardStats = ref<DashboardStats | null>(null)

  // Inicializar currentUser do token se existir
  if (token.value) {
    try {
      const decoded: { sub: string; name?: string; role?: User['role']; roles?: string } = jwtDecode(token.value)
      currentUser.value.name = decoded.name || 'Usuário'
      // Backend usa 'roles' (plural) então verificar ambos
      const roleFromToken = (decoded.roles || decoded.role || 'user').toLowerCase()
      currentUser.value.role = roleFromToken as User['role']
      currentUser.value.email = decoded.sub
      api.defaults.headers.common['Authorization'] = `Bearer ${token.value}`
    } catch (error) {
      console.error('Erro ao decodificar token:', error)
      sessionStorage.clear()
    }
  }
  const mapTicketFromApi = (ticketData: ApiTicket): Ticket => ({
    id: ticketData.id,
    numeroChamado: ticketData.numeroChamado,
    user: ticketData.nomeSolicitante,
    description: ticketData.descricao,
    category: ticketData.categoria,
    priority: ticketData.prioridade,
    status: ticketData.status,
    openedAt: ticketData.dataAbertura,
    closedAt: ticketData.dataFechamento,
    assignedTo: ticketData.nomeTecnicoAtribuido,
    solution: ticketData.solucao,
    history: (ticketData.historico || [])
      .map(
        (item: ApiHistoryItem): HistoryItem => ({
          author: item.autor,
          comment: item.comentario,
          date: item.dataOcorrencia,
        }),
      )
      .sort(
        (a: HistoryItem, b: HistoryItem) => new Date(b.date).getTime() - new Date(a.date).getTime(),
      ),
    isReopened: ticketData.foiReaberto || false,
    slaDeadline: new Date(),
    anexos: ticketData.anexos || [],
  })

  async function downloadAnexo(anexo: Anexo) {
    const response = await api.get(`/api/anexos/${anexo.id}/download`, {
      responseType: 'blob',
    })

    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', anexo.nomeArquivo)
    document.body.appendChild(link)
    link.click()

    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  }

  async function fetchFormData() {
    try {
      const [catResponse, prioResponse] = await Promise.all([
        api.get('/api/categorias'),
        api.get('/api/prioridades'),
      ])
      categories.value = catResponse.data
      priorities.value = prioResponse.data
    } catch (error) {
      console.error('ERRO ao buscar dados de formulário:', error)
    }
  }
  async function fetchAnalysts() {
    try {
      const response = await api.get('/api/users/technicians')
      analysts.value = response.data.map((u: { id: number; nome: string }) => ({
        id: u.id,
        name: u.nome,
      }))
    } catch (error) {
      console.error('ERRO ao buscar analistas:', error)
      analysts.value = []
    }
  }
  async function createTicket(payload: FormData) {
    const response = await api.post('/api/tickets', payload)
    const newTicket = mapTicketFromApi(response.data)
    tickets.value.unshift(newTicket)
    return newTicket
  }
  async function fetchTickets() {
    if (!token.value) {
      console.warn('⚠️ fetchTickets: Sem token!')
      return
    }
    try {
      const response = await api.get('/api/tickets?size=1000')
      // O backend retorna Page<TicketResponseDTO>, então usamos response.data.content
      const ticketsData = response.data.content || response.data
      tickets.value = ticketsData.map(mapTicketFromApi)
    } catch (error) {
      console.error('Erro ao buscar chamados:', error)
      tickets.value = []
    }
  }
  async function fetchDashboardStats() {
    try {
      const response = await api.get('/api/dashboard/stats')
      dashboardStats.value = response.data
    } catch (error) {
      console.error('Falha ao buscar dados do painel:', error)
      dashboardStats.value = null
    }
  }
  async function fetchTicketById(id: number) {
    try {
      const response = await api.get(`/api/tickets/${id}`)
      activeTicket.value = mapTicketFromApi(response.data)
    } catch (error) {
      console.error(`Falha ao buscar chamado ${id}:`, error)
      activeTicket.value = null
    }
  }
  if (token.value) {
    try {
      const decoded: { sub: string; name?: string; role?: User['role'] } = jwtDecode(token.value)
      currentUser.value.name = decoded.name || 'Usuário'
      currentUser.value.role = decoded.role || 'user'
      currentUser.value.email = decoded.sub
      api.defaults.headers.common['Authorization'] = `Bearer ${token.value}`
    } catch (error) {
      console.error('Erro ao decodificar token:', error)
      sessionStorage.removeItem('token')
      token.value = ''
    }
  }
  async function login(credentials: { email: string; senha: string }) {
    const response = await api.post('/api/auth/login', credentials)
    // Backend retorna TokenPairDTO com accessToken e refreshToken
    const accessToken: string = response.data.accessToken
    const refreshToken: string = response.data.refreshToken

    token.value = accessToken
    sessionStorage.setItem('token', accessToken)
    sessionStorage.setItem('refreshToken', refreshToken)

    api.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`

    const decoded: { sub: string; name?: string; role?: User['role']; roles?: string } = jwtDecode(accessToken)
    currentUser.value.name = decoded.name || 'Usuário'
    // Backend usa 'roles' (plural) então verificar ambos
    const roleFromToken = (decoded.roles || decoded.role || 'user').toLowerCase()
    currentUser.value.role = roleFromToken as User['role']
    currentUser.value.email = decoded.sub

    await fetchTickets()
  }
  async function logout() {
    // Revogar refresh token no backend
    const refreshToken = sessionStorage.getItem('refreshToken')
    if (refreshToken) {
      try {
        await api.post('/api/auth/logout', { refreshToken })
      } catch (error) {
        console.error('Erro ao revogar refresh token:', error)
      }
    }

    // Limpar estado
    currentUser.value = { name: '', email: '', role: null }
    token.value = ''
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('refreshToken')
    sessionStorage.clear() // Garante limpeza completa
    api.defaults.headers.common['Authorization'] = ''
    tickets.value = []
    activeTicket.value = null
    dashboardStats.value = null
  }
  async function assignTicketToSelf(ticketId: number) {
    const response = await api.post(`/api/tickets/${ticketId}/assign-self`)
    activeTicket.value = mapTicketFromApi(response.data)
    await fetchTickets()
  }
  async function assignTicket(ticketId: number, technicianId: number) {
    const response = await api.post(`/api/tickets/${ticketId}/assign/${technicianId}`)
    activeTicket.value = mapTicketFromApi(response.data)
    await fetchTickets()
  }
  async function addCommentToTicket(ticketId: number, comentario: string) {
    await api.post(`/api/tickets/${ticketId}/comments`, { comentario })
    await fetchTicketById(ticketId)
  }
  async function closeTicket(ticketId: number, solucao: string) {
    const response = await api.post(`/api/tickets/${ticketId}/close`, { solucao })
    activeTicket.value = mapTicketFromApi(response.data)
    await fetchTickets()
  }
  async function reopenTicket(ticketId: number, motivo: string) {
    const response = await api.post(`/api/tickets/${ticketId}/reopen`, { motivo })
    activeTicket.value = mapTicketFromApi(response.data)
    await fetchTickets()
  }
  async function createCategory(name: string) {
    const response = await api.post('/api/categorias', { nome: name })
    categories.value.push(response.data)
  }
  async function createPriority(name: string) {
    const response = await api.post('/api/prioridades', { nome: name })
    priorities.value.push(response.data)
  }
  const ticketsWithSla = computed(() =>
    tickets.value.map((ticket) => ({
      ...ticket,
      slaDeadline: calculateSlaDeadline(ticket.openedAt, ticket.priority),
    })),
  )
  const openTickets = computed(() =>
    ticketsWithSla.value.filter((t) => t.status?.toUpperCase() === 'ABERTO' || t.status === 'Aberto'),
  )
  const inProgressTickets = computed(() =>
    ticketsWithSla.value.filter((t) => t.status?.toUpperCase() === 'EM ANDAMENTO' || t.status === 'Em Andamento'),
  )
  const closedTickets = computed(() =>
    ticketsWithSla.value.filter((t) => {
      const statusUpper = t.status?.toUpperCase()
      return ['RESOLVIDO', 'FECHADO', 'CANCELADO', 'ENCERRADO'].includes(statusUpper) ||
             ['Resolvido', 'Fechado', 'Cancelado', 'Encerrado'].includes(t.status)
    }),
  )
  const myOpenTickets = computed(() =>
    ticketsWithSla.value.filter(
      (t) =>
        t.user === currentUser.value.name &&
        !['Resolvido', 'Fechado', 'Cancelado'].includes(t.status),
    ),
  )
  const myClosedTickets = computed(() =>
    ticketsWithSla.value.filter(
      (t) =>
        t.user === currentUser.value.name &&
        ['Resolvido', 'Fechado', 'Cancelado'].includes(t.status),
    ),
  )
  const openTicketsCount = computed(() => dashboardStats.value?.chamadosNaFila ?? 0)
  const violatedSlaTicketsCount = computed(
    () => dashboardStats.value?.chamadosSlaViolado.length ?? 0,
  )
  const ticketsByAnalyst = computed(() => {
    if (!dashboardStats.value?.chamadosPorAnalista) return {}
    return dashboardStats.value.chamadosPorAnalista.reduce(
      (acc: Record<string, number>, curr) => {
        acc[curr.nomeAnalista] = curr.totalChamados
        return acc
      },
      {} as Record<string, number>,
    )
  })

  // ============================================
  // PROPRIEDADES E MÉTODOS PARA RELATÓRIOS
  // ============================================

  // Filtros de data para relatórios
  const selectedYear = ref<number>(new Date().getFullYear())
  const selectedMonth = ref<number>(new Date().getMonth() + 1)

  // Anos disponíveis baseados nos tickets
  const availableYears = computed(() => {
    const years = new Set(tickets.value.map(t => new Date(t.openedAt).getFullYear()))
    return Array.from(years).sort((a, b) => b - a)
  })

  // Meses disponíveis baseados no ano selecionado
  const availableMonths = computed(() => {
    return [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]
  })

  // Unidades únicas dos tickets
  const unidades = computed(() => {
    const unidadesSet = new Set(
      tickets.value
        .map(t => t.unidade)
        .filter((u): u is string => !!u)
    )
    return Array.from(unidadesSet).sort()
  })

  // Locais únicos dos tickets
  const locais = computed(() => {
    const locaisSet = new Set(
      tickets.value
        .map(t => t.local)
        .filter((l): l is string => !!l)
    )
    return Array.from(locaisSet).sort()
  })

  // Tickets com alerta de SLA
  const slaAlertTickets = computed(() => {
    const now = new Date()
    return ticketsWithSla.value.filter(t => {
      if (['Resolvido', 'Fechado', 'Cancelado'].includes(t.status)) return false
      const timeRemaining = t.slaDeadline.getTime() - now.getTime()
      return timeRemaining < 2 * 60 * 60 * 1000 // Menos de 2 horas
    })
  })

  // Relatório de performance dos analistas
  const analystPerformanceReport = computed(() => {
    return dashboardStats.value?.chamadosPorAnalista || []
  })

  // Relatório de tempo médio por categoria
  const avgResolutionTimeByCategoryReport = computed(() => {
    const categoryTimes: Record<string, { total: number; count: number }> = {}

    tickets.value
      .filter(t => t.closedAt && t.status === 'Resolvido')
      .forEach(t => {
        const openTime = new Date(t.openedAt).getTime()
        const closeTime = new Date(t.closedAt!).getTime()
        const hours = (closeTime - openTime) / (1000 * 60 * 60)

        if (!categoryTimes[t.category]) {
          categoryTimes[t.category] = { total: 0, count: 0 }
        }
        categoryTimes[t.category].total += hours
        categoryTimes[t.category].count++
      })

    return Object.entries(categoryTimes).map(([categoria, data]) => ({
      categoria,
      tempoMedioHoras: data.total / data.count
    }))
  })

  // Relatório de tickets por mês
  const ticketsPerMonthReport = computed(() => {
    const monthlyData: Record<number, number> = {}

    tickets.value
      .filter(t => new Date(t.openedAt).getFullYear() === selectedYear.value)
      .forEach(t => {
        const month = new Date(t.openedAt).getMonth() + 1
        monthlyData[month] = (monthlyData[month] || 0) + 1
      })

    return Object.entries(monthlyData).map(([mes, totalChamados]) => ({
      mes: parseInt(mes),
      totalChamados
    }))
  })

  // Relatório de satisfação (placeholder - requer dados do backend)
  const satisfactionReport = computed(() => {
    return {
      totalRespostas: 0,
      mediaSatisfacao: 0,
      distribuicao: {
        muitoSatisfeito: 0,
        satisfeito: 0,
        neutro: 0,
        insatisfeito: 0,
        muitoInsatisfeito: 0
      }
    }
  })

  // Relatório de performance de SLA
  const slaPerformanceReport = computed(() => {
    const total = tickets.value.filter(t => ['Resolvido', 'Fechado'].includes(t.status)).length
    const dentroDoSla = tickets.value.filter(t => {
      if (!['Resolvido', 'Fechado'].includes(t.status)) return false
      if (!t.closedAt) return false
      const slaDeadline = calculateSlaDeadline(t.openedAt, t.priority)
      return new Date(t.closedAt) <= slaDeadline
    }).length

    return {
      total,
      dentroDoSla,
      foraDoSla: total - dentroDoSla,
      percentualCumprimento: total > 0 ? (dentroDoSla / total) * 100 : 0
    }
  })

  // Função para atualizar filtro de data
  function setDateFilter(year: number, month: number) {
    selectedYear.value = year
    selectedMonth.value = month
  }

  return {
    currentUser,
    token,
    tickets,
    activeTicket,
    categories,
    priorities,
    analysts,
    dashboardStats,
    login,
    logout,
    fetchTickets,
    fetchTicketById,
    fetchFormData,
    createTicket,
    assignTicketToSelf,
    addCommentToTicket,
    closeTicket,
    reopenTicket,
    fetchAnalysts,
    fetchDashboardStats,
    assignTicket,
    createCategory,
    createPriority,

    downloadAnexo,
    ticketsWithSla,
    openTickets,
    inProgressTickets,
    closedTickets,
    myOpenTickets,
    myClosedTickets,
    openTicketsCount,
    violatedSlaTicketsCount,
    ticketsByAnalyst,

    // Propriedades de relatórios
    selectedYear,
    selectedMonth,
    availableYears,
    availableMonths,
    unidades,
    locais,
    slaAlertTickets,
    analystPerformanceReport,
    avgResolutionTimeByCategoryReport,
    ticketsPerMonthReport,
    satisfactionReport,
    slaPerformanceReport,
    setDateFilter,
  }
})

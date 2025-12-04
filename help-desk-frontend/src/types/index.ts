export interface Anexo {
  id: number
  nomeArquivo: string
  tipoArquivo: string
}
export interface HistoryItem {
  author: string
  date: string
  comment: string
}

export interface TicketDTO {
  id: number
  numeroChamado: string
  descricao: string
  categoria: string
  prioridade: string
  status: string
  dataAbertura: string
  dataFechamento: string | null
  solucao: string | null
  local: string
  unidade: string
  foiReaberto: boolean
  nomeSolicitante: string
  nomeTecnicoAtribuido: string | null
  historico: ApiHistoryItem[]
  slaDeadline: string
  anexos: Anexo[]
}

export interface Ticket {
  id: number
  numeroChamado: string
  user: string
  description: string
  category: string
  priority: string
  status: string
  openedAt: string
  closedAt?: string | null
  assignedTo?: string | null
  solution?: string | null
  history: HistoryItem[]
  isReopened?: boolean
  slaDeadline: Date
  anexos: Anexo[]
  local?: string
  unidade?: string
}
export interface Category {
  id: number
  nome: string
}
export interface Priority {
  id: number
  nome: string
}
export interface ApiHistoryItem {
  autor: string
  comentario: string
  dataOcorrencia: string
}
export interface ApiTicket {
  id: number
  numeroChamado: string
  nomeSolicitante: string
  descricao: string
  categoria: string
  prioridade: string
  status: string
  dataAbertura: string
  dataFechamento: string | null
  nomeTecnicoAtribuido: string | null
  solucao: string | null
  historico: ApiHistoryItem[]
  foiReaberto: boolean
  anexos: Anexo[]
}
export interface User {
  name: string
  email: string
  role: 'admin' | 'user' | 'technician' | 'manager' | null
}
export interface Analyst {
  id: number
  name: string
}
export interface NewTicketPayload {
  description: string
  category: string
  priority: string
}
export interface AnalistaComChamados {
  nomeAnalista: string
  totalChamados: number
}
export interface DashboardStats {
  chamadosNaFila: number
  chamadosPorAnalista: AnalistaComChamados[]
  chamadosSlaViolado: Ticket[]
}

export interface RelatorioAnalista {
  nomeAnalista: string
  totalChamados: number
}

export interface RelatorioCategoria {
  categoria: string
  tempoMedioHoras: number
}

export interface RelatorioMensal {
  mes: number
  totalChamados: number
}

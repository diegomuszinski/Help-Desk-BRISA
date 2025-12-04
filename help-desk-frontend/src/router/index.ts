import { createRouter, createWebHistory } from 'vue-router'
import { useTicketStore } from '@/stores/ticketStore'
import LoginView from '../views/LoginView.vue'
import DashboardView from '../views/DashboardView.vue'
import TicketDetailView from '../views/TicketDetailView.vue'
import CreateTicketView from '../views/CreateTicketView.vue'
import UserTicketsView from '../views/UserTicketsView.vue'
import ManagerDashboardView from '../views/ManagerDashboardView.vue'
import AnalyticsDashboardView from '../views/AnalyticsDashboardView.vue'
import ReportsView from '../views/ReportsView.vue'
import SatisfactionReportView from '../views/SatisfactionReportView.vue'
import SlaReportView from '../views/SlaReportView.vue'
import ManagementView from '../views/ManagementView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/',
      redirect: '/fila/abertos',
    },
    {
      path: '/fila/:status',
      name: 'ticket-queue',
      component: DashboardView,
    },
    {
      path: '/chamado/:id',
      name: 'ticket-detail',
      component: TicketDetailView,
    },
    {
      path: '/abrir-chamado',
      name: 'create-ticket',
      component: CreateTicketView,
    },
    {
      path: '/meus-chamados',
      name: 'user-tickets',
      component: UserTicketsView,
      props: { status: 'abertos' },
    },
    {
      path: '/meus-chamados/fechados',
      name: 'user-tickets-closed',
      component: UserTicketsView,
      props: { status: 'fechados' },
    },
    {
      path: '/painel-gestao',
      name: 'manager-dashboard',
      component: ManagerDashboardView,
    },
    {
      path: '/dashboards',
      name: 'analytics-dashboard',
      component: AnalyticsDashboardView,
    },
    {
      path: '/relatorios',
      name: 'reports',
      component: ReportsView,
    },
    {
      path: '/relatorios/satisfacao',
      name: 'satisfaction-report',
      component: SatisfactionReportView,
    },
    {
      path: '/relatorios/sla',
      name: 'sla-report',
      component: SlaReportView,
    },
    {
      path: '/gerenciamento',
      name: 'management',
      component: ManagementView,
    },
  ],
})

router.beforeEach((to, from, next) => {
  const ticketStore = useTicketStore()
  // Verificar token tanto no store quanto no sessionStorage
  const isLoggedIn = !!ticketStore.token && !!sessionStorage.getItem('token')

  const requiredAdminRole = ['management']
  if (requiredAdminRole.includes(to.name as string) && ticketStore.currentUser.role !== 'admin') {
    next({ name: 'ticket-queue', params: { status: 'abertos' } })
    return
  }

  if (to.name !== 'login' && !isLoggedIn) {
    // Limpar qualquer resto de sessão antes de redirecionar
    sessionStorage.clear()
    next({ name: 'login' })
  } else if (to.name === 'login' && isLoggedIn) {
    if (ticketStore.currentUser.role === 'user') {
      next({ name: 'user-tickets' })
    } else {
      next({ name: 'ticket-queue', params: { status: 'abertos' } })
    }
  } else {
    next()
  }
})

export default router

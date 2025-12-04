<script setup lang="ts">
import { computed, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useTicketStore } from '@/stores/ticketStore'
import AppModal from './AppModal.vue'
import logoBrisa from '@/assets/brisa.png'
import {
  InboxArrowDownIcon,
  WrenchScrewdriverIcon,
  CheckCircleIcon,
  ChartPieIcon,
  DocumentTextIcon,
  PlusCircleIcon,
  ArrowLeftOnRectangleIcon,
  TvIcon,
  TicketIcon,
  FaceSmileIcon,
  CheckBadgeIcon,
  Cog6ToothIcon,
  ExclamationTriangleIcon,
} from '@heroicons/vue/24/outline'

const router = useRouter()
const ticketStore = useTicketStore()
const showLogoutModal = ref(false)

const userRoleDisplay = computed(() => {
  const role = ticketStore.currentUser.role
  if (role === 'admin') return 'Administrador'
  if (role === 'user') return 'Usuário'
  if (role === 'technician') return 'Técnico'
  if (role === 'manager') return 'Gestor'
  return 'Não autenticado'
})

function openLogoutModal() {
  showLogoutModal.value = true
}

function closeLogoutModal() {
  showLogoutModal.value = false
}

async function confirmLogout() {
  await ticketStore.logout()
  showLogoutModal.value = false
  // Forçar redirecionamento para login usando replace para evitar histórico
  await router.replace('/login')
}
</script>

<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <h2 class="sidebar-title">Help Desk</h2>
      <img :src="logoBrisa" alt="Logo Brisa" class="company-logo" />
      <div v-if="ticketStore.currentUser.role" class="user-profile">
        <strong>{{ userRoleDisplay }}</strong>
        <span>{{ ticketStore.currentUser.email }}</span>
      </div>
    </div>

    <nav class="main-nav">
      <template v-if="['user', 'admin', 'manager'].includes(ticketStore.currentUser.role || '')">
        <RouterLink to="/abrir-chamado" class="nav-link">
          <PlusCircleIcon class="nav-icon" />
          <span>Abrir Chamado</span>
        </RouterLink>
      </template>

      <template v-if="ticketStore.currentUser.role === 'user'">
        <RouterLink to="/meus-chamados" class="nav-link">
          <TicketIcon class="nav-icon" />
          <span>Meus Chamados</span>
        </RouterLink>
        <RouterLink to="/meus-chamados/fechados" class="nav-link">
          <CheckBadgeIcon class="nav-icon" />
          <span>Chamados Fechados</span>
        </RouterLink>
      </template>

      <template
        v-if="['admin', 'technician', 'manager'].includes(ticketStore.currentUser.role || '')"
      >
        <RouterLink to="/fila/abertos" class="nav-link">
          <InboxArrowDownIcon class="nav-icon" />
          <span>Fila de Entrada</span>
        </RouterLink>
        <RouterLink to="/fila/em-andamento" class="nav-link">
          <WrenchScrewdriverIcon class="nav-icon" />
          <span>Em Atendimento</span>
        </RouterLink>
        <RouterLink to="/fila/fechados" class="nav-link">
          <CheckCircleIcon class="nav-icon" />
          <span>Fechados</span>
        </RouterLink>
      </template>

      <template v-if="['admin', 'manager'].includes(ticketStore.currentUser.role || '')">
        <RouterLink to="/painel-gestao" class="nav-link">
          <TvIcon class="nav-icon" />
          <span>Painel de Gestão</span>
        </RouterLink>
        <RouterLink to="/dashboards" class="nav-link">
          <ChartPieIcon class="nav-icon" />
          <span>Dashboards</span>
        </RouterLink>
        <RouterLink to="/relatorios" class="nav-link">
          <DocumentTextIcon class="nav-icon" />
          <span>Relatórios</span>
        </RouterLink>
        <RouterLink to="/relatorios/satisfacao" class="nav-link">
          <FaceSmileIcon class="nav-icon" />
          <span>Pesq. de Satisfação</span>
        </RouterLink>
      </template>

      <template v-if="ticketStore.currentUser.role === 'admin'">
        <RouterLink to="/gerenciamento" class="nav-link">
          <Cog6ToothIcon class="nav-icon" />
          <span>Gerenciamento</span>
        </RouterLink>
      </template>
    </nav>

    <a href="#" @click.prevent="openLogoutModal" class="nav-link logout-link">
      <ArrowLeftOnRectangleIcon class="nav-icon" />
      <span>Sair</span>
    </a>

    <!-- Modal de confirmação de logout -->
    <AppModal v-if="showLogoutModal" @close="closeLogoutModal">
      <div class="logout-modal">
        <div class="modal-icon">
          <ExclamationTriangleIcon class="warning-icon" />
        </div>
        <h2 class="modal-title">Confirmar Saída</h2>
        <p class="modal-message">
          Você tem certeza que deseja sair do sistema?
          <br />
          <span class="modal-submessage">Você precisará fazer login novamente para acessar.</span>
        </p>
        <div class="modal-actions">
          <button @click="closeLogoutModal" class="btn-cancel">
            Cancelar
          </button>
          <button @click="confirmLogout" class="btn-confirm">
            Sim, Sair
          </button>
        </div>
      </div>
    </AppModal>
  </aside>
</template>

<style scoped>
.sidebar {
  width: 250px;
  background-color: var(--brisa-blue-primary);
  color: #fff;
  display: flex;
  flex-direction: column;
  height: 100vh;
}
.sidebar-header {
  padding: 1.5rem;
  text-align: center;
  border-bottom: 1px solid var(--brisa-blue-secondary);
  flex-shrink: 0;
}
.sidebar-header h2 {
  margin: 0;
  font-size: 1.5rem;
}
.company-logo {
  width: 80px;
  margin: 1rem auto;
}
.user-profile {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
  margin-top: 1rem;
}
.user-profile strong {
  font-size: 1rem;
  font-weight: 500;
}
.user-profile span {
  font-size: 0.8rem;
  color: #ccc;
}
.main-nav {
  flex-grow: 1;
  overflow-y: auto;
  padding-top: 1rem;
}
.nav-link {
  color: #fff;
  text-decoration: none;
  padding: 1rem 1.5rem;
  transition: background-color 0.3s;
  border-left: 4px solid transparent;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}
.nav-icon {
  width: 24px;
  height: 24px;
  flex-shrink: 0;
}
.nav-link:hover {
  background-color: var(--brisa-blue-secondary);
}
.nav-link.router-link-exact-active {
  background-color: var(--brisa-blue-secondary);
  font-weight: bold;
  border-left: 4px solid #3498db;
}
.logout-link {
  flex-shrink: 0;
  border-top: 1px solid var(--brisa-blue-secondary);
}

/* Estilos do modal de logout */
.logout-modal {
  text-align: center;
  padding: 1rem;
}

.modal-icon {
  display: flex;
  justify-content: center;
  margin-bottom: 1.5rem;
}

.warning-icon {
  width: 64px;
  height: 64px;
  color: #f39c12;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.05);
  }
}

.modal-title {
  font-size: 1.75rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 1rem;
}

.modal-message {
  font-size: 1.1rem;
  color: #555;
  margin-bottom: 0.5rem;
  line-height: 1.6;
}

.modal-submessage {
  font-size: 0.95rem;
  color: #888;
  font-style: italic;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  margin-top: 2rem;
}

.btn-cancel,
.btn-confirm {
  padding: 0.75rem 2rem;
  font-size: 1rem;
  font-weight: 500;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 120px;
}

.btn-cancel {
  background-color: #95a5a6;
  color: white;
}

.btn-cancel:hover {
  background-color: #7f8c8d;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.btn-confirm {
  background-color: #e74c3c;
  color: white;
}

.btn-confirm:hover {
  background-color: #c0392b;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(231, 76, 60, 0.4);
}

.btn-cancel:active,
.btn-confirm:active {
  transform: translateY(0);
}
</style>

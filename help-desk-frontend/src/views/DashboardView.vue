<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import TicketList from '@/components/TicketList.vue'
import { useTicketStore } from '@/stores/ticketStore'
import { useRoute } from 'vue-router'

const ticketStore = useTicketStore()
const route = useRoute()

onMounted(() => {
  ticketStore.fetchTickets()
  // Busca os dados de categoria e analistas para os filtros
  if (['admin', 'manager'].includes(ticketStore.currentUser.role || '')) {
    ticketStore.fetchFormData() // Esta função busca as categorias
    ticketStore.fetchAnalysts()
  }
})

const searchField = ref('description')
const searchTerm = ref('')
const categoryFilter = ref('todas')
const analystFilter = ref('todos')

const searchOptions = [
  { value: 'id', text: 'Chamado' },
  { value: 'description', text: 'Descrição' },
  { value: 'priority', text: 'Prioridade' },
  { value: 'user', text: 'Solicitante' },
]

const filteredTickets = computed(() => {
  const status = route.params.status as string
  let baseList

  if (status === 'em-andamento') {
    baseList = ticketStore.inProgressTickets
  } else if (status === 'fechados') {
    baseList = ticketStore.closedTickets
  } else {
    baseList = ticketStore.openTickets
  }

  if (!baseList) return []

  return baseList.filter((ticket) => {
    const isManagerOrAdmin = ['admin', 'manager'].includes(ticketStore.currentUser.role || '')

    const categoryMatch =
      !isManagerOrAdmin ||
      categoryFilter.value === 'todas' ||
      ticket.category === categoryFilter.value

    const analystMatch =
      !isManagerOrAdmin ||
      analystFilter.value === 'todos' ||
      ticket.assignedTo === analystFilter.value

    const term = searchTerm.value.toLowerCase().trim()
    if (!term) {
      return categoryMatch && analystMatch
    }

    const fieldValue = String(ticket[searchField.value as keyof typeof ticket] ?? '').toLowerCase()
    const searchMatch = fieldValue.includes(term)

    return categoryMatch && analystMatch && searchMatch
  })
})

const pageTitle = computed(() => {
  const status = route.params.status
  if (status === 'em-andamento') return 'Chamados em Atendimento'
  if (status === 'fechados') return 'Chamados Fechados'
  return 'Fila de Entrada (Aguardando Atendimento)'
})
</script>

<template>
  <div class="dashboard-view">
    <header class="view-header">
      <h1>{{ pageTitle }}</h1>
      <div class="filters-container">
        <template v-if="['admin', 'manager'].includes(ticketStore.currentUser.role || '')">
          <select v-model="categoryFilter" class="filter-select">
            <option value="todas">Todas as Categorias</option>
            <option v-for="cat in ticketStore.categories" :key="cat.id" :value="cat.nome">
              {{ cat.nome }}
            </option>
          </select>

          <select v-model="analystFilter" class="filter-select">
            <option value="todos">Todos os Técnicos</option>
            <option v-for="analyst in ticketStore.analysts" :key="analyst.id" :value="analyst.name">
              {{ analyst.name }}
            </option>
          </select>
        </template>
        <div class="search-bar">
          <select v-model="searchField">
            <option v-for="option in searchOptions" :key="option.value" :value="option.value">
              Buscar por {{ option.text }}
            </option>
          </select>
          <input type="search" v-model="searchTerm" placeholder="Digite para buscar..." />
        </div>
      </div>
    </header>
    <div class="view-content">
      <TicketList :tickets="filteredTickets" />
    </div>
  </div>
</template>

<style scoped>
.dashboard-view {
  padding: 2rem;
}
.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  gap: 1rem;
}
.view-header h1 {
  margin: 0;
  font-size: 1.8rem;
  color: #2c3e50;
}
.filters-container {
  display: flex;
  gap: 1rem;
  align-items: center;
  flex-wrap: wrap;
}
.search-bar {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}
.search-bar input,
.search-bar select,
.filter-select {
  padding: 0.75rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 0.9rem;
  background-color: #fff;
  height: 45px;
}
.search-bar input {
  width: 250px;
}
.view-content {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}
</style>

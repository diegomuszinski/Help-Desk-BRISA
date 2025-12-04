<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useTicketStore } from '@/stores/ticketStore'
import TicketList from '@/components/TicketList.vue'
import { RouterLink } from 'vue-router'

const props = defineProps<{
  status: 'abertos' | 'fechados'
}>()

const ticketStore = useTicketStore()

onMounted(() => {
  ticketStore.fetchTickets()
})

const viewConfig = computed(() => {
  if (props.status === 'fechados') {
    return {
      title: 'Meus Chamados Fechados',
      tickets: ticketStore.myClosedTickets,
    }
  }
  return {
    title: 'Meus Chamados',
    tickets: ticketStore.myOpenTickets,
  }
})
</script>

<template>
  <div class="user-tickets-view">
    <header class="view-header">
      <h1>{{ viewConfig.title }}</h1>
    </header>

    <div class="view-content">
      <div v-if="viewConfig.tickets.length > 0">
        <TicketList :tickets="viewConfig.tickets" />
      </div>
      <div v-else class="no-tickets">
        <p>Nenhum chamado encontrado nesta visualização.</p>
        <RouterLink v-if="props.status === 'abertos'" to="/abrir-chamado" class="btn-primary">
          Abrir um novo chamado
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<style scoped>
.user-tickets-view {
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
.view-content {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}
.no-tickets {
  padding: 4rem 2rem;
  text-align: center;
}
.no-tickets p {
  font-size: 1.2rem;
  color: #6c757d;
  margin-bottom: 1.5rem;
}
.btn-primary {
  padding: 0.8rem 1.5rem;
  background-color: var(--brisa-blue-primary);
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  text-decoration: none;
  transition: background-color 0.3s;
}
.btn-primary:hover {
  background-color: var(--brisa-blue-secondary);
}
</style>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import type { Ticket } from '@/stores/ticketStore'

defineProps<{
  tickets: (Ticket & { slaDeadline: Date })[]
}>()

const router = useRouter()

const goToTicket = (ticketId: number) => {
  router.push(`/ticket/${String(ticketId)}`)
}

function getTimeStatus(ticket: Ticket & { slaDeadline: Date }) {
  const now = new Date().getTime()
  const deadline = ticket.slaDeadline.getTime()
  const diff = deadline - now

  if (diff <= 0) {
    const overdue = now - deadline
    const hours = Math.floor(overdue / (1000 * 60 * 60))
    const minutes = Math.floor((overdue % (1000 * 60 * 60)) / (1000 * 60))
    return { text: `Atrasado há ${hours}h ${minutes}m`, class: 'violated' }
  }

  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  return { text: `Vence em ${hours}h ${minutes}m`, class: 'warning' }
}
</script>

<template>
  <div class="sla-alerts-card">
    <h2 class="card-title">🚨 Alertas de SLA</h2>
    <div class="alert-list">
      <div
        v-for="ticket in tickets"
        :key="ticket.id"
        class="alert-item"
        @click="goToTicket(ticket.id)"
        :class="getTimeStatus(ticket).class"
      >
        <div class="ticket-info">
          <strong>Chamado #{{ ticket.id }}</strong>
          <span>Solicitante: {{ ticket.user }}</span>
        </div>
        <div class="ticket-status">
          {{ getTimeStatus(ticket).text }}
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.sla-alerts-card {
  background-color: #fff3cd;
  border: 1px solid #ffeeba;
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 2rem;
}
.card-title {
  margin-top: 0;
  margin-bottom: 1rem;
  color: #856404;
  font-size: 1.5rem;
}
.alert-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.alert-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background-color: #fff;
  border-radius: 6px;
  border-left: 5px solid;
  cursor: pointer;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
}
.alert-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.08);
}
.ticket-info {
  display: flex;
  flex-direction: column;
}
.ticket-info strong {
  font-size: 1.1rem;
  color: #333;
}
.ticket-info span {
  font-size: 0.9rem;
  color: #6c757d;
}
.ticket-status {
  font-weight: bold;
  font-size: 1rem;
  padding: 0.5rem 1rem;
  border-radius: 15px;
  color: #fff;
}


.alert-item.warning {
  border-left-color: #fd7e14;
}
.alert-item.warning .ticket-status {
  background-color: #fd7e14;
}
.alert-item.violated {
  border-left-color: #dc3545;
}
.alert-item.violated .ticket-status {
  background-color: #dc3545;
}
</style>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import type { Ticket, TicketDTO } from '@/types/index'
import { formatDateTime } from '@/utils/formatters'

import { ExclamationTriangleIcon } from '@heroicons/vue/24/outline'

defineProps<{
  tickets: (Ticket | TicketDTO)[]
}>()

const router = useRouter()

function goToTicket(id: number) {
  router.push(`/chamado/${id}`)
}

function getPriorityClass(priority?: string | null): string {
  if (!priority) {
    return 'priority-default'
  }

  const priorityKey = priority
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
  return `priority-${priorityKey}`
}

function isTicketReopened(ticket: Ticket | TicketDTO): boolean {
  return !!(ticket as Ticket).isReopened || !!(ticket as TicketDTO).foiReaberto
}
</script>

<template>
  <div class="ticket-list">
    <table class="ticket-table">
      <thead>
        <tr>
          <th>Chamado</th>
          <th>Descrição</th>
          <th>Prioridade</th>
          <th>Solicitante</th>
          <th>Prazo SLA</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="ticket in tickets as any[]" :key="ticket.id" @click="goToTicket(ticket.id)">
          <td class="ticket-number">
            <div class="ticket-id-cell">
              <span
                v-if="isTicketReopened(ticket)"
                class="reopened-icon-wrapper"
                title="Chamado Reaberto"
              >
                <ExclamationTriangleIcon class="reopened-icon" />
              </span>
              <span>{{ ticket.numeroChamado || 'N/D' }}</span>
            </div>
          </td>
          <td>{{ ticket.description || ticket.descricao || 'Sem descrição' }}</td>
          <td>
            <span
              :class="['priority-badge', getPriorityClass(ticket.priority || ticket.prioridade)]"
            >
              {{ ticket.priority || ticket.prioridade || 'N/D' }}
            </span>
          </td>
          <td>{{ ticket.user || ticket.nomeSolicitante || 'N/D' }}</td>
          <td class="sla-date">{{ formatDateTime(ticket.slaDeadline) }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.ticket-list {
  overflow-x: auto;
}
.ticket-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.95rem;
}
.ticket-table th,
.ticket-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #e9ecef;
}
.ticket-table td {
  color: #495057;
}
.ticket-table th {
  color: #6c757d;
  font-weight: 600;
  text-transform: uppercase;
  font-size: 0.8rem;
}
.ticket-table tbody tr {
  cursor: pointer;
  transition: background-color 0.2s;
}
.ticket-table tbody tr:hover {
  background-color: #f8f9fa;
}

.ticket-id-cell {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.reopened-icon-wrapper {
  display: inline-flex;

  margin: 0;
  padding: 0;
  line-height: 1;
}

.reopened-icon {
  width: 1.1rem;
  height: 1.1rem;
  color: #f59e0b;
  vertical-align: middle;
}

.ticket-number {
  font-weight: 700;
  color: var(--brisa-blue-primary);
}
.priority-badge {
  padding: 0.3rem 0.8rem;
  border-radius: 15px;
  font-weight: 600;
  color: #fff;
  text-transform: uppercase;
  font-size: 0.75rem;
  white-space: nowrap;
}

.priority-baixa {
  background-color: #198754;
}
.priority-media {
  background-color: #ffc107;
  color: #333;
}
.priority-alta {
  background-color: #dc3545;
}
.priority-urgente {
  background-color: #000000;
}
.priority-critica {
  background-color: #8b0000;
}
.priority-default {
  background-color: #6c757d;
}

.sla-date {
  font-weight: 500;
}
</style>

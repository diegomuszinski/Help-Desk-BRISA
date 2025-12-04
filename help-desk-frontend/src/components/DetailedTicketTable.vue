<script setup lang="ts">
import type { Ticket } from '@/stores/ticketStore'
import { formatDateTime } from '@/utils/formatters'
import { useRouter } from 'vue-router'

defineProps<{
  tickets: Ticket[]
}>()

const router = useRouter()

const goToTicket = (id: number) => {
  router.push(`/ticket/${String(id)}`)
}
</script>

<template>
  <div class="table-container">
    <table>
      <thead>
        <tr>
          <th>Número</th>
          <th>Problema</th>
          <th>Solicitante</th>
          <th>Local</th>
          <th>Data de Abertura</th>
          <th>Status</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="ticket in tickets" :key="ticket.id" @click="goToTicket(ticket.id)">
          <td>
            <strong>{{ ticket.id }}</strong>
          </td>
          <td>{{ ticket.category }}</td>
          <td>{{ ticket.user }}</td>
          <td>{{ ticket.local }}</td>
          <td>{{ formatDateTime(ticket.openedAt) }}</td>
          <td>{{ ticket.status }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.table-container {
  overflow-x: auto;
}
table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 2rem;
}
thead {
  background-color: #f8f9fa;
}
th,
td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #dee2e6;
  white-space: nowrap;
}
th {
  font-weight: 600;
  color: #495057;
}
tbody tr {
  cursor: pointer;
  transition: background-color 0.2s;
}
tbody tr:hover {
  background-color: #f1f1f1;
}
td strong {
  color: var(--brisa-blue-primary);
}

td {
  color: #212529;
}
</style>

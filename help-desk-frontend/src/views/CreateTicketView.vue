<script setup lang="ts">
import TicketCreateForm from '@/components/TicketCreateForm.vue'
import { useRouter } from 'vue-router'

const router = useRouter()

function onTicketCreated(newTicketId: number) {
  router.push(`/chamado/${newTicketId}`)
}
</script>

<template>
  <div class="create-ticket-view">
    <header class="view-header">
      <h1>Abertura de Novo Chamado</h1>
      <p>Preencha os campos abaixo para registrar sua solicitação.</p>
    </header>
    <div class="view-content">
      <TicketCreateForm @ticket-created="onTicketCreated" />
    </div>
  </div>
</template>

<style scoped>
.create-ticket-view {
  padding: 2rem;
  max-width: 900px;
  margin: 0 auto;
}
.view-header {
  margin-bottom: 2rem;
}
.view-header h1 {
  margin: 0;
  font-size: 1.8rem;
  color: #2c3e50;
}
.view-header p {
  color: #6c757d;
}
.view-content {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  padding: 2rem;
}
</style>

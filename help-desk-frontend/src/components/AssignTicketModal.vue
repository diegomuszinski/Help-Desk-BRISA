<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useTicketStore } from '@/stores/ticketStore'
import AppModal from './AppModal.vue'

defineProps<{
  ticketId: number
}>()

const emit = defineEmits(['close', 'assigned'])

const ticketStore = useTicketStore()
const selectedTechnicianId = ref<number | null>(null)

onMounted(() => {
  if (ticketStore.analysts.length > 0) {
    selectedTechnicianId.value = ticketStore.analysts[0].id
  }
})

function handleSubmit() {
  if (selectedTechnicianId.value) {
    emit('assigned', selectedTechnicianId.value)
  }
}
</script>

<template>
  <AppModal @close="$emit('close')">
    <div class="assign-ticket-modal">
      <h3>Atribuir Chamado</h3>
      <p>Selecione o técnico para qual você deseja atribuir este chamado.</p>
      <form @submit.prevent="handleSubmit">
        <select v-model="selectedTechnicianId" required>
          <option disabled :value="null">Selecione um técnico</option>
          <option v-for="analyst in ticketStore.analysts" :key="analyst.id" :value="analyst.id">
            {{ analyst.name }}
          </option>
        </select>
        <div class="modal-actions">
          <button type="button" class="btn-secondary" @click="$emit('close')">Cancelar</button>
          <button type="submit" class="btn-primary">Confirmar Atribuição</button>
        </div>
      </form>
    </div>
  </AppModal>
</template>

<style scoped>
.assign-ticket-modal h3 {
  margin-top: 0;
  color: #2c3e50;
}
.assign-ticket-modal p {
  color: #6c757d;
  font-size: 0.9rem;
}
.assign-ticket-modal select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ced4da;
  border-radius: 4px;
  font-size: 1rem;
  margin-top: 1rem;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1.5rem;
}
.modal-actions button {
  padding: 0.8rem 1.5rem;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
}
.btn-primary {
  background-color: var(--brisa-blue-primary);
  color: white;
}
.btn-secondary {
  background-color: #f8f9fa;
  color: #343a40;
  border: 1px solid #ced4da;
}
</style>

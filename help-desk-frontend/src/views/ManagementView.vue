<script setup lang="ts">
import { ref } from 'vue'
import { useTicketStore } from '@/stores/ticketStore'
import { useToast } from 'vue-toastification'

const ticketStore = useTicketStore()
const toast = useToast()

const newCategoryName = ref('')
const newPriorityName = ref('')

async function handleCreateCategory() {
  if (newCategoryName.value.trim() === '') {
    toast.error('O nome da categoria não pode ser vazio.')
    return
  }
  try {
    await ticketStore.createCategory(newCategoryName.value.trim())
    toast.success(`Categoria "${newCategoryName.value.trim()}" criada com sucesso!`)
    newCategoryName.value = ''
  } catch (error) {
    console.error('Falha ao criar categoria:', error)
    toast.error('Falha ao criar categoria. Verifique se ela já existe.')
  }
}

async function handleCreatePriority() {
  if (newPriorityName.value.trim() === '') {
    toast.error('O nome da prioridade não pode ser vazio.')
    return
  }
  try {
    await ticketStore.createPriority(newPriorityName.value.trim())
    toast.success(`Prioridade "${newPriorityName.value.trim()}" criada com sucesso!`)
    newPriorityName.value = ''
  } catch (error) {
    console.error('Falha ao criar prioridade:', error)
    toast.error('Falha ao criar prioridade. Verifique se ela já existe.')
  }
}
</script>

<template>
  <div class="management-view">
    <header class="view-header">
      <h1>Gerenciamento do Sistema</h1>
    </header>
    <div class="content-grid">
      <div class="management-card">
        <h2>Categorias</h2>
        <p>Adicione novas categorias para a abertura de chamados.</p>
        <form @submit.prevent="handleCreateCategory" class="management-form">
          <input
            type="text"
            v-model="newCategoryName"
            placeholder="Nome da nova categoria"
            required
          />
          <button type="submit">Adicionar Categoria</button>
        </form>
      </div>

      <div class="management-card">
        <h2>Prioridades</h2>
        <p>Adicione novos níveis de prioridade para os chamados.</p>
        <form @submit.prevent="handleCreatePriority" class="management-form">
          <input
            type="text"
            v-model="newPriorityName"
            placeholder="Nome da nova prioridade"
            required
          />
          <button type="submit">Adicionar Prioridade</button>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.management-view {
  padding: 2rem;
}
.view-header {
  margin-bottom: 2rem;
}
.view-header h1 {
  font-size: 1.8rem;
  color: #2c3e50;
}
.content-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 2rem;
}
.management-card {
  background-color: #fff;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}
.management-card h2 {
  margin-top: 0;
  border-bottom: 1px solid #eee;
  padding-bottom: 1rem;
  margin-bottom: 1rem;
  color: #2c3e50;
}
.management-card p {
  color: #6c757d;
  margin-bottom: 1.5rem;
}
.management-form {
  display: flex;
  gap: 0.5rem;
}
.management-form input {
  flex-grow: 1;
  padding: 0.75rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 0.9rem;
}
.management-form button {
  padding: 0.75rem 1.2rem;
  border: none;
  border-radius: 4px;
  background-color: var(--brisa-blue-primary);
  color: white;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
}
.management-form button:hover {
  background-color: var(--brisa-blue-secondary);
}
</style>

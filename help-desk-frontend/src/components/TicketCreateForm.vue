<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useTicketStore } from '@/stores/ticketStore'
import { useToast } from 'vue-toastification'

const emit = defineEmits(['ticket-created'])
const ticketStore = useTicketStore()
const toast = useToast()

const description = ref('')
const category = ref('')
const priority = ref('Média')

const selectedFiles = ref<File[]>([])
const isLoading = ref(false)

const MIN_CHARS = 10
const MAX_CHARS = 5000
const currentChars = computed(() => description.value.length)

const fileInputText = computed(() => {
  if (selectedFiles.value.length === 0) return 'Nenhum arquivo selecionado'
  if (selectedFiles.value.length === 1) return selectedFiles.value[0].name
  return `${selectedFiles.value.length} arquivos selecionados`
})

onMounted(() => {
  ticketStore.fetchFormData()
})

function handleFileChange(event: Event) {
  const target = event.target as HTMLInputElement
  if (target.files) {
    const newFiles = Array.from(target.files)

    // Validação 1: Limite de arquivos
    const MAX_FILES = 5
    if (selectedFiles.value.length + newFiles.length > MAX_FILES) {
      toast.error(`Máximo de ${MAX_FILES} arquivos permitidos`)
      target.value = ''
      return
    }

    // Validação 2: Tamanho de cada arquivo
    const MAX_FILE_SIZE = 10 * 1024 * 1024 // 10MB
    for (const file of newFiles) {
      if (file.size > MAX_FILE_SIZE) {
        toast.error(`Arquivo "${file.name}" excede o tamanho máximo de 10MB`)
        target.value = ''
        return
      }
    }

    selectedFiles.value.push(...newFiles)
  }
  target.value = ''
}

function removeFile(fileIndex: number) {
  selectedFiles.value.splice(fileIndex, 1)
}

async function handleSubmit() {
  if (!description.value.trim() || !category.value) {
    toast.error('Por favor, preencha a descrição e a categoria.')
    return
  }

  // Validar tamanho mínimo da descrição (backend exige 10-5000 caracteres)
  if (description.value.trim().length < 10) {
    toast.error('A descrição deve ter no mínimo 10 caracteres.')
    return
  }

  if (description.value.trim().length > 5000) {
    toast.error('A descrição não pode ter mais de 5000 caracteres.')
    return
  }

  isLoading.value = true

  // Converter prioridade para o formato esperado pelo backend (MAIÚSCULA)
  const priorityMap: { [key: string]: string } = {
    'Baixa': 'BAIXA',
    'Média': 'MEDIA',
    'Alta': 'ALTA',
    'Crítica': 'URGENTE'
  }

  const ticketData = {
    description: description.value,
    category: category.value,
    priority: priorityMap[priority.value] || 'MEDIA',
  }

  const formData = new FormData()

  // Backend espera @RequestPart("ticket") com application/json
  // Criar Blob com nome de arquivo para garantir que o Content-Type seja enviado
  const ticketJson = JSON.stringify(ticketData)
  const ticketBlob = new Blob([ticketJson], { type: 'application/json' })
  formData.append('ticket', ticketBlob, 'ticket.json')

  if (selectedFiles.value.length > 0) {
    for (const file of selectedFiles.value) {
      formData.append('anexos', file)
    }
  }

  try {
    const newTicket = await ticketStore.createTicket(formData)
    toast.success(`Chamado ${newTicket.numeroChamado} criado com sucesso!`)

    // Limpar formulário após sucesso
    description.value = ''
    category.value = ''
    priority.value = 'Média'
    selectedFiles.value = []

    emit('ticket-created', newTicket.id)
  } catch (error: unknown) {
    let errorMessage = 'Ocorreu um erro ao criar o chamado.'

    if (error && typeof error === 'object' && 'response' in error) {
      const axiosError = error as {
        response?: {
          data?: {
            message?: string | Record<string, string>
          }
        }
      }

      const serverMessage = axiosError.response?.data?.message

      // Se message é um objeto com erros de validação
      if (serverMessage && typeof serverMessage === 'object') {
        const errors = Object.values(serverMessage).join(', ')
        errorMessage = `Erros de validação: ${errors}`
      } else if (typeof serverMessage === 'string') {
        errorMessage = serverMessage
      }
    }

    toast.error(errorMessage)
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <form @submit.prevent="handleSubmit" class="ticket-form">
    <div class="form-group">
      <label for="description">Descrição Detalhada do Problema:</label>
      <textarea
        id="description"
        v-model="description"
        required
        rows="5"
        :maxlength="MAX_CHARS"
      ></textarea>
      <small
        class="char-counter"
        :class="{
          'text-danger': currentChars < MIN_CHARS || currentChars > MAX_CHARS,
          'text-success': currentChars >= MIN_CHARS
        }"
      >
        {{ currentChars }} / {{ MAX_CHARS }} caracteres
        <span v-if="currentChars < MIN_CHARS">(mínimo: {{ MIN_CHARS }})</span>
      </small>
    </div>
    <div class="form-grid">
      <div class="form-group">
        <label for="category">Categoria:</label>
        <select id="category" v-model="category" required>
          <option disabled value="">Selecione...</option>
          <option v-for="cat in ticketStore.categories" :key="cat.id" :value="cat.nome">
            {{ cat.nome }}
          </option>
        </select>
      </div>
      <div class="form-group">
        <label for="priority">Prioridade:</label>
        <select id="priority" v-model="priority" required>
          <option v-for="prio in ticketStore.priorities" :key="prio.id" :value="prio.nome">
            {{ prio.nome }}
          </option>
        </select>
      </div>
    </div>
    <div class="form-group">
      <label>Anexar Arquivo(s) (Word, Excel, CSV, txt):</label>
      <div class="file-input-container">
        <label for="anexo-input" class="file-input-button">Procurar...</label>
        <input
          type="file"
          id="anexo-input"
          class="file-input-hidden"
          @change="handleFileChange"
          accept=".doc,.docx,.xls,.xlsx,.csv,.txt"
          multiple
        />
        <span class="file-input-text">{{ fileInputText }}</span>
      </div>
      <div v-if="selectedFiles.length > 0" class="file-list">
        <ul>
          <li v-for="(file, index) in selectedFiles" :key="index">
            <span>{{ file.name }}</span>
            <button @click.prevent="removeFile(index)" class="remove-file-btn" title="Remover">
              &times;
            </button>
          </li>
        </ul>
      </div>
    </div>
    <div class="form-actions">
      <button type="submit" class="submit-btn" :disabled="isLoading">
        {{ isLoading ? 'Enviando...' : 'Abrir Chamado' }}
      </button>
    </div>
  </form>
</template>

<style scoped>
.ticket-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}
.form-group {
  display: flex;
  flex-direction: column;
}
.form-group label {
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #555;
}
.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1rem;
  font-family: inherit;
}
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}
.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 1rem;
}
.submit-btn {
  padding: 0.8rem 1.5rem;
  background-color: var(--brisa-blue-primary);
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.3s;
}
.submit-btn:hover {
  background-color: var(--brisa-blue-secondary);
}
.submit-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}
.char-counter {
  text-align: right;
  margin-top: 0.25rem;
  font-size: 0.8rem;
  color: #6c757d;
  font-weight: 500;
}
.char-counter.text-danger {
  color: #dc3545;
}
.char-counter.text-success {
  color: #28a745;
}
.file-input-container {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
  border: 1px solid #ccc;
  border-radius: 4px;
  padding: 0;
  overflow: hidden;
}
.file-input-hidden {
  display: none;
}
.file-input-button {
  background-color: #f0f0f0;
  padding: 0.75rem 1rem;
  cursor: pointer;
  border-right: 1px solid #ccc;
  white-space: nowrap;
  margin: 0;
  font-weight: normal;
  transition: background-color 0.2s;
}
.file-input-button:hover {
  background-color: #e0e0e0;
}
.file-input-text {
  padding: 0.75rem;
  color: #555;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex-grow: 1;
}
.file-list {
  margin-top: 0.75rem;
  font-size: 0.9rem;
}
.file-list ul {
  list-style-type: none;
  padding-left: 0;
  margin: 0.5rem 0 0 0;
}
.file-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.4rem 0.6rem;
  background-color: #f8f9fa;
  border-radius: 4px;
  margin-bottom: 0.25rem;
  color: #333;
}
.remove-file-btn {
  background: none;
  border: none;
  color: #dc3545;
  font-size: 1.4rem;
  font-weight: bold;
  cursor: pointer;
  line-height: 1;
  padding: 0 0.2rem;
}
.remove-file-btn:hover {
  color: #a71d2a;
}
</style>

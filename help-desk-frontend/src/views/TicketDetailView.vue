<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useTicketStore, calculateSlaDeadline } from '@/stores/ticketStore'
import { formatDateTime } from '@/utils/formatters'
import { useToast } from 'vue-toastification'
import AppModal from '@/components/AppModal.vue'
import AssignTicketModal from '@/components/AssignTicketModal.vue'
import type { Anexo } from '@/types/index'

const ticketStore = useTicketStore()
const route = useRoute()
const toast = useToast()
const ticket = computed(() => ticketStore.activeTicket)

const newComment = ref('')
const isCloseModalVisible = ref(false)
const solutionText = ref('')
const isReopenModalVisible = ref(false)
const reopenReason = ref('')
const isAssignModalVisible = ref(false)

async function handleDownload(anexo: Anexo) {
  toast.info(`Iniciando download de: ${anexo.nomeArquivo}`)
  try {
    await ticketStore.downloadAnexo(anexo)
  } catch (error) {
    toast.error('Falha ao baixar o anexo.')
    console.error('Erro no download:', error)
  }
}

const MAX_CHARS = 1000
const countdown = ref('')
let countdownInterval: number | undefined = undefined
const commentRemainingChars = computed(() => MAX_CHARS - newComment.value.length)
const solutionRemainingChars = computed(() => MAX_CHARS - solutionText.value.length)
const reopenRemainingChars = computed(() => MAX_CHARS - reopenReason.value.length)
function startCountdown() {
  if (countdownInterval) clearInterval(countdownInterval)
  countdownInterval = window.setInterval(() => {
    if (
      !ticket.value ||
      !ticket.value.openedAt ||
      ['Encerrado', 'Fechado', 'Resolvido', 'Cancelado'].includes(ticket.value.status)
    ) {
      const status = ticket.value?.status || 'Finalizado'
      countdown.value = status.charAt(0).toUpperCase() + status.slice(1)
      if (countdownInterval) clearInterval(countdownInterval)
      return
    }
    const now = new Date().getTime()
    const deadline = calculateSlaDeadline(ticket.value.openedAt, ticket.value.priority).getTime()
    const diff = deadline - now
    if (diff <= 0) {
      const overdueDiff = now - deadline
      const days = Math.floor(overdueDiff / (1000 * 60 * 60 * 24))
      const hours = Math.floor((overdueDiff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
      const minutes = Math.floor((overdueDiff % (1000 * 60 * 60)) / (1000 * 60))
      countdown.value = `Atrasado há ${days}d ${hours}h ${minutes}m`
      return
    }
    const days = Math.floor(diff / (1000 * 60 * 60 * 24))
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
    const seconds = Math.floor((diff % (1000 * 60)) / 1000)
    countdown.value = `${days}d ${hours}h ${minutes}m ${seconds}s`
  }, 1000)
}
onMounted(() => {
  const ticketId = Number(route.params.id)
  if (ticketId) {
    ticketStore.fetchTicketById(ticketId).then(() => {
      if (ticket.value) startCountdown()
    })
  }
  if (['admin', 'manager'].includes(ticketStore.currentUser.role || '')) {
    ticketStore.fetchAnalysts()
  }
})
onUnmounted(() => {
  if (countdownInterval) clearInterval(countdownInterval)
})
async function addComment() {
  if (newComment.value.trim() && ticket.value) {
    try {
      await ticketStore.addCommentToTicket(ticket.value.id, newComment.value.trim())
      newComment.value = ''
      toast.success('Observação adicionada com sucesso!')
    } catch (error) {
      console.error('Falha ao adicionar observação:', error)
      toast.error('Falha ao adicionar observação.')
    }
  }
}
async function assignToMe() {
  if (ticket.value) {
    try {
      await ticketStore.assignTicketToSelf(ticket.value.id)
      toast.success('Chamado capturado com sucesso!')
    } catch (error) {
      console.error('Falha ao capturar chamado:', error)
      toast.error('Não foi possível capturar o chamado.')
    }
  }
}
async function handleAssignTicket(technicianId: number) {
  if (ticket.value) {
    try {
      await ticketStore.assignTicket(ticket.value.id, technicianId)
      toast.success('Chamado atribuído com sucesso!')
      isAssignModalVisible.value = false
    } catch (error) {
      console.error('Falha ao atribuir chamado:', error)
      toast.error('Ocorreu um erro ao atribuir o chamado.')
    }
  }
}
async function handleCloseTicket() {
  if (ticket.value && solutionText.value.trim() !== '') {
    try {
      await ticketStore.closeTicket(ticket.value.id, solutionText.value.trim())
      toast.success('Chamado encerrado com sucesso!')
      isCloseModalVisible.value = false
      solutionText.value = ''
    } catch (error) {
      console.error('Falha ao encerrar chamado:', error)
      toast.error('Não foi possível encerrar o chamado.')
    }
  } else {
    toast.error('É necessário fornecer uma solução para encerrar o chamado.')
  }
}
function handleReopenTicket() {
  if (ticket.value) {
    isReopenModalVisible.value = true
  }
}
async function submitReopenTicket() {
  if (ticket.value && reopenReason.value.trim() !== '') {
    try {
      await ticketStore.reopenTicket(ticket.value.id, reopenReason.value.trim())
      toast.success('Chamado reaberto com sucesso!')
      isReopenModalVisible.value = false
      reopenReason.value = ''
    } catch (error) {
      console.error('Falha ao reabrir chamado:', error)
      toast.error('Ocorreu um erro ao reabrir o chamado.')
    }
  } else {
    toast.error('É necessário fornecer um motivo para reabrir o chamado.')
  }
}
</script>

<template>
  <main class="detail-view">
    <div v-if="ticket" class="ticket-card">
      <header class="card-header">
        <div class="header-title">
          <h2>Detalhes do Chamado: #{{ ticket.numeroChamado }}</h2>
          <span class="status-badge">{{ ticket.status }}</span>
        </div>
        <div class="actions">
          <button
            v-if="
              ticket.status === 'Aberto' &&
              ['admin', 'manager'].includes(ticketStore.currentUser.role || '')
            "
            @click="isAssignModalVisible = true"
            class="action-btn assign-manager"
          >
            Atribuir
          </button>
          <button
            v-if="
              ticket.status === 'Aberto' &&
              ['admin', 'manager', 'technician'].includes(ticketStore.currentUser.role || '')
            "
            @click="assignToMe"
            class="action-btn assign"
          >
            Capturar
          </button>
          <button
            v-if="
              ticket.status === 'Em Andamento' && ticket.assignedTo === ticketStore.currentUser.name
            "
            @click="isCloseModalVisible = true"
            class="action-btn close"
          >
            Encerrar
          </button>
          <button
            v-if="
              ['Resolvido', 'Encerrado', 'Fechado'].includes(ticket.status) &&
              ticket.user === ticketStore.currentUser.name
            "
            @click="handleReopenTicket"
            class="action-btn reopen"
          >
            Reabrir
          </button>
        </div>
      </header>
      <section class="card-body">
        <div class="detail-grid">
          <div class="detail-item">
            <strong>Solicitante:</strong><span>{{ ticket.user }}</span>
          </div>
          <div class="detail-item">
            <strong>Aberto em:</strong><span>{{ formatDateTime(ticket.openedAt) }}</span>
          </div>
          <div class="detail-item">
            <strong>Atribuído a:</strong><span>{{ ticket.assignedTo || 'Não atribuído' }}</span>
          </div>
          <div class="detail-item">
            <strong>Prioridade:</strong><span>{{ ticket.priority }}</span>
          </div>
          <div v-if="ticket.openedAt" class="detail-item">
            <strong>Prazo Final (SLA):</strong
            ><span>{{
              formatDateTime(calculateSlaDeadline(ticket.openedAt, ticket.priority))
            }}</span
            ><span class="countdown"> ({{ countdown }})</span>
          </div>
        </div>
        <div class="detail-item description">
          <strong>Descrição do Problema:</strong>
          <p>{{ ticket.description }}</p>
        </div>
      </section>

      <section
        v-if="ticket.anexos && ticket.anexos.length > 0"
        class="card-section attachments-section"
      >
        <h3>Anexos</h3>
        <ul>
          <li v-for="anexo in ticket.anexos" :key="anexo.id">
            <button @click="handleDownload(anexo)" class="download-link">
              {{ anexo.nomeArquivo }}
            </button>
          </li>
        </ul>
      </section>

      <section class="card-section history-section">
        <h3>Histórico e Comentários</h3>
        <div class="comment-list">
          <div v-for="(item, index) in ticket.history" :key="index" class="comment-item">
            <div class="comment-header">
              <strong class="author">{{ item.author || 'Sistema' }}</strong
              ><span class="date">{{ formatDateTime(item.date) }}</span>
            </div>
            <p class="comment-body">{{ item.comment }}</p>
          </div>
        </div>
      </section>
      <section
        v-if="!['Resolvido', 'Fechado', 'Encerrado', 'Cancelado'].includes(ticket.status)"
        class="card-section comment-form-section"
      >
        <h3>Adicionar Observação</h3>
        <form @submit.prevent="addComment">
          <textarea
            v-model="newComment"
            placeholder="Digite sua observação aqui..."
            rows="4"
            required
            :maxlength="MAX_CHARS"
          ></textarea>
          <small class="char-counter"
            >{{ newComment.length }} / {{ MAX_CHARS }} (Restam {{ commentRemainingChars }})</small
          >
          <button type="submit">Adicionar Observação</button>
        </form>
      </section>
    </div>
    <div v-else class="not-found"><h2>Carregando chamado...</h2></div>

    <AssignTicketModal
      v-if="isAssignModalVisible && ticket"
      :ticket-id="ticket.id"
      @close="isAssignModalVisible = false"
      @assigned="handleAssignTicket"
    />
    <AppModal v-if="isCloseModalVisible" @close="isCloseModalVisible = false">
      <div class="close-ticket-modal">
        <h3>Encerrar Chamado #{{ ticket?.numeroChamado }}</h3>
        <p>
          Por favor, descreva a solução aplicada. Esta informação será registrada no histórico e
          visível para o solicitante.
        </p>
        <form @submit.prevent="handleCloseTicket">
          <textarea
            v-model="solutionText"
            rows="5"
            placeholder="Descreva a solução aqui..."
            required
            :maxlength="MAX_CHARS"
          ></textarea>
          <small class="char-counter"
            >{{ solutionText.length }} / {{ MAX_CHARS }} (Restam
            {{ solutionRemainingChars }})</small
          >
          <div class="modal-actions">
            <button type="button" class="btn-secondary" @click="isCloseModalVisible = false">
              Cancelar</button
            ><button type="submit" class="btn-primary">Confirmar Encerramento</button>
          </div>
        </form>
      </div>
    </AppModal>
    <AppModal v-if="isReopenModalVisible" @close="isReopenModalVisible = false">
      <div class="reopen-ticket-modal">
        <h3>Reabrir Chamado #{{ ticket?.numeroChamado }}</h3>
        <p>
          Por favor, descreva o motivo da reabertura. Esta informação será registrada no histórico.
        </p>
        <form @submit.prevent="submitReopenTicket">
          <textarea
            v-model="reopenReason"
            rows="5"
            placeholder="Descreva o motivo aqui..."
            required
            :maxlength="MAX_CHARS"
          ></textarea>
          <small class="char-counter"
            >{{ reopenReason.length }} / {{ MAX_CHARS }} (Restam {{ reopenRemainingChars }})</small
          >
          <div class="modal-actions">
            <button type="button" class="btn-secondary" @click="isReopenModalVisible = false">
              Cancelar</button
            ><button type="submit" class="btn-primary">Confirmar Reabertura</button>
          </div>
        </form>
      </div>
    </AppModal>
  </main>
</template>

<style scoped>
.download-link {
  background: none;
  border: none;
  padding: 0;
  color: var(--brisa-blue-primary);
  text-decoration: underline;
  cursor: pointer;
  font-size: inherit;
  font-family: inherit;
}
.download-link:hover {
  color: var(--brisa-blue-secondary);
}

.detail-view {
  padding: 2rem;
  background-color: #f4f7f6;
  display: flex;
  justify-content: center;
}
.ticket-card {
  width: 100%;
  max-width: 900px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  background-color: #f8f9fa;
  border-bottom: 1px solid var(--color-border);
  gap: 1rem;
  flex-wrap: wrap;
}
.header-title {
  display: flex;
  align-items: center;
  gap: 1rem;
}
.header-title h2 {
  margin: 0;
  font-size: 1.5rem;
  color: #2c3e50;
}
.status-badge {
  padding: 0.4rem 0.8rem;
  border-radius: 15px;
  background: var(--brisa-blue-secondary);
  color: white;
  font-weight: 500;
  white-space: nowrap;
}
.card-body {
  padding: 1.5rem;
}
.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}
.detail-item strong {
  display: block;
  margin-bottom: 0.25rem;
  color: #555;
}
.detail-item span,
.detail-item p {
  color: #212529;
}
.detail-item.description {
  grid-column: 1 / -1;
}
.detail-item.description p {
  margin: 0.25rem 0 0 0;
  padding: 1rem;
  background-color: #f9f9f9;
  border: 1px solid #eee;
  border-radius: 4px;
  white-space: pre-wrap;
}
.countdown {
  font-weight: bold;
  margin-left: 8px;
  color: #212529;
}
.card-section {
  padding: 1rem 1.5rem;
  border-top: 1px solid var(--color-border);
}
.attachments-section ul {
  list-style-type: none;
  padding: 0;
}
.attachments-section li {
  margin-bottom: 0.5rem;
}
.attachments-section a:hover {
  text-decoration: underline;
}
.card-section h3 {
  margin-top: 0;
  margin-bottom: 1rem;
  color: #2c3e50;
}
.comment-list {
  max-height: 400px;
  overflow-y: auto;
  padding-right: 10px;
}
.comment-item {
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #f0f0f0;
}
.comment-item:last-child {
  border-bottom: none;
}
.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}
.author {
  font-weight: bold;
  color: #333;
}
.date {
  font-size: 0.8rem;
  color: #6c757d;
}
.comment-body {
  color: #212529;
  white-space: pre-wrap;
}
.comment-form-section form {
  display: flex;
  flex-direction: column;
}
.comment-form-section textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  font-family: inherit;
  font-size: 1rem;
}
.char-counter {
  text-align: right;
  margin-top: 0.25rem;
  font-size: 0.8rem;
  color: #6c757d;
}
.comment-form-section button {
  align-self: flex-end;
  padding: 0.6rem 1.2rem;
  background-color: var(--brisa-blue-primary);
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s;
  margin-top: 0.5rem;
}
.comment-form-section button:hover {
  background-color: var(--brisa-blue-secondary);
}
.actions {
  display: flex;
  gap: 0.5rem;
}
.action-btn {
  padding: 0.6rem 1rem;
  border: none;
  border-radius: 4px;
  color: white;
  font-weight: 500;
  font-size: 0.9rem;
  cursor: pointer;
  transition: opacity 0.3s;
}
.action-btn:hover {
  opacity: 0.85;
}
.action-btn.assign-manager {
  background-color: #ffc107;
  color: #212529;
}
.action-btn.assign {
  background-color: #28a745;
}
.action-btn.close {
  background-color: #dc3545;
}
.action-btn.reopen {
  background-color: #17a2b8;
}
.reopen-ticket-modal h3,
.close-ticket-modal h3 {
  margin-top: 0;
  color: #2c3e50;
}
.reopen-ticket-modal p,
.close-ticket-modal p {
  color: #6c757d;
  font-size: 0.9rem;
}
.reopen-ticket-modal textarea,
.close-ticket-modal textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ced4da;
  border-radius: 4px;
  font-size: 1rem;
  font-family: inherit;
  margin-top: 1rem;
}
.reopen-ticket-modal .char-counter,
.close-ticket-modal .char-counter {
  display: block;
  text-align: right;
  margin-top: 0.25rem;
  margin-bottom: 1rem;
  font-size: 0.8rem;
  color: #6c757d;
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
  transition: background-color 0.2s;
}
.modal-actions .btn-primary {
  background-color: var(--brisa-blue-primary);
  color: white;
}
.modal-actions .btn-primary:hover {
  background-color: var(--brisa-blue-secondary);
}
.modal-actions .btn-secondary {
  background-color: #f8f9fa;
  color: #343a40;
  border: 1px solid #ced4da;
}
.modal-actions .btn-secondary:hover {
  background-color: #e2e6ea;
}
.not-found {
  padding: 2rem;
  text-align: center;
  color: #6c757d;
}
</style>

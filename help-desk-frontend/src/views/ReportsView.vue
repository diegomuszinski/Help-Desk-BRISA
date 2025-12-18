<script setup lang="ts">
import { ref, computed } from 'vue'
import { useTicketStore } from '@/stores/ticketStore'
import type { Ticket } from '@/types/index'
import DetailedTicketTable from '@/components/DetailedTicketTable.vue'
import jsPDF from 'jspdf'
import 'jspdf-autotable'
import { formatDateTime } from '@/utils/formatters'

const ticketStore = useTicketStore()

const filters = ref({
  areaResponsavel: 'Todos',
  problema: 'Todos',
  unidade: 'Todos',
  local: 'Todos',
  usuario: 'Todos',
  dataInicial: '',
  dataFinal: '',
  status: 'Todos',
  ordenarPor: 'numero',
})

const reportResults = ref<Ticket[]>([])
const searchPerformed = ref(false)

const allUsers = computed(() => {
  const userNames = ticketStore.tickets.map((t: any) => t.user)
  return [...new Set(userNames)].sort()
})

function search() {
  searchPerformed.value = true
  let results = [...ticketStore.tickets]

  if (filters.value.problema !== 'Todos') {
    results = results.filter((t) => t.category === filters.value.problema)
  }
  if (filters.value.unidade !== 'Todos') {
    results = results.filter((t) => t.unidade === filters.value.unidade)
  }
  if (filters.value.local !== 'Todos') {
    results = results.filter((t) => t.local === filters.value.local)
  }
  if (filters.value.usuario !== 'Todos') {
    results = results.filter((t) => t.user === filters.value.usuario)
  }
  if (filters.value.status !== 'Todos') {
    results = results.filter((t) => t.status === filters.value.status)
  }
  if (filters.value.dataInicial) {
    results = results.filter((t) => new Date(t.openedAt) >= new Date(filters.value.dataInicial))
  }
  if (filters.value.dataFinal) {
    const endDate = new Date(filters.value.dataFinal)
    endDate.setDate(endDate.getDate() + 1)
    results = results.filter((t) => new Date(t.openedAt) < endDate)
  }

  results.sort((a, b) => {
    switch (filters.value.ordenarPor) {
      case 'data_abertura':
        return new Date(a.openedAt).getTime() - new Date(b.openedAt).getTime()
      case 'status':
        return a.status.localeCompare(b.status)
      case 'numero':
      default:
        return a.numeroChamado.localeCompare(b.numeroChamado)
    }
  })

  reportResults.value = results
}

function clearFields() {
  filters.value = {
    areaResponsavel: 'Todos',
    problema: 'Todos',
    unidade: 'Todos',
    local: 'Todos',
    usuario: 'Todos',
    dataInicial: '',
    dataFinal: '',
    status: 'Todos',
    ordenarPor: 'numero',
  }
  reportResults.value = []
  searchPerformed.value = false
}

function exportCsv() {
  const dataToExport = reportResults.value.map((t) => ({
    Numero: t.id,
    Problema: t.category,
    Solicitante: t.user,
    Local: t.local,
    DataAbertura: formatDateTime(t.openedAt),
    Status: t.status,
  }))

  if (dataToExport.length === 0) return

  const headers = Object.keys(dataToExport[0])
  const csvRows = [headers.join(',')]
  for (const row of dataToExport) {
    const values = headers.map((header) => `"${row[header as keyof typeof row]}"`)
    csvRows.push(values.join(','))
  }
  const csvContent = csvRows.join('\n')

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.setAttribute('href', URL.createObjectURL(blob))
  link.setAttribute('download', 'relatorio_chamados.csv')
  link.click()
}

function exportPdf() {
  const doc = new jsPDF()

  interface JsPDFWithAutoTable extends jsPDF {
    autoTable: (options: unknown) => void
  }

  ;(doc as JsPDFWithAutoTable).autoTable({
    head: [['Número', 'Problema', 'Solicitante', 'Local', 'Data de Abertura', 'Status']],
    body: reportResults.value.map((t) => [
      t.id,
      t.category,
      t.user,
      t.local,
      formatDateTime(t.openedAt),
      t.status,
    ]),
  })
  doc.save('relatorio_chamados.pdf')
}
</script>

<template>
  <div class="reports-view">
    <header class="view-header">
      <h1>Relatório Geral de Ocorrências</h1>
    </header>

    <div class="report-builder">
      <form @submit.prevent="search" class="filter-form">
        <div class="form-grid">
          <div class="form-group">
            <label for="area">Área Responsável:</label
            ><select id="area" v-model="filters.areaResponsavel">
              <option value="Todos">Todos</option>
              <option v-for="team in ['N1', 'N2']" :key="team" :value="team">{{ team }}</option>
            </select>
          </div>
          <div class="form-group">
            <label for="problema">Problema:</label
            ><select id="problema" v-model="filters.problema">
              <option value="Todos">Todos</option>
              <option v-for="cat in ticketStore.categories" :key="cat.id" :value="cat.nome">
                {{ cat.nome }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label for="unidade">Unidade:</label
            ><select id="unidade" v-model="filters.unidade">
              <option value="Todos">Todos</option>
              <option v-for="unidade in ticketStore.unidades" :key="unidade" :value="unidade">
                {{ unidade }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label for="local">Local:</label
            ><select id="local" v-model="filters.local">
              <option value="Todos">Todos</option>
              <option v-for="local in ticketStore.locais" :key="local" :value="local">
                {{ local }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label for="usuario">Usuário:</label
            ><select id="usuario" v-model="filters.usuario">
              <option value="Todos">Todos</option>
              <option v-for="user in allUsers" :key="String(user)" :value="user">{{ user }}</option>
            </select>
          </div>
          <div class="form-group">
            <label for="status">Status:</label
            ><select id="status" v-model="filters.status">
              <option value="Todos">Todos</option>
              <option value="Aberto">Aberto</option>
              <option value="Em Andamento">Em Atendimento</option>
              <option value="Resolvido">Resolvido</option>
              <option value="Fechado">Fechado</option>
            </select>
          </div>
          <div class="form-group">
            <label for="data-inicial">Data inicial:</label
            ><input type="date" id="data-inicial" v-model="filters.dataInicial" />
          </div>
          <div class="form-group">
            <label for="data-final">Data final:</label
            ><input type="date" id="data-final" v-model="filters.dataFinal" />
          </div>
          <div class="form-group">
            <label for="ordenar">Ordenar por:</label
            ><select id="ordenar" v-model="filters.ordenarPor">
              <option value="numero">Número</option>
              <option value="data_abertura">Data de Abertura</option>
              <option value="status">Status</option>
            </select>
          </div>
        </div>
        <div class="form-actions">
          <button type="submit">Pesquisar</button>
          <button type="button" @click="clearFields">Limpar campos</button>
        </div>
      </form>

      <div v-if="searchPerformed" class="results-section">
        <hr />
        <div class="results-header">
          <h3>Resultados da Busca ({{ reportResults.length }} encontrados)</h3>
          <div class="export-buttons" v-if="reportResults.length > 0">
            <button @click="exportPdf">Exportar PDF</button>
            <button @click="exportCsv">Exportar CSV/Excel</button>
          </div>
        </div>
        <DetailedTicketTable v-if="reportResults.length > 0" :tickets="reportResults" />
        <div v-else class="no-results">
          <p>Nenhum chamado encontrado com os filtros aplicados.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.reports-view {
  padding: 2rem;
}
.view-header h1 {
  margin-top: 0;
  margin-bottom: 2rem;
  font-size: 1.8rem;
  color: #2c3e50;
}
.report-builder {
  background-color: #fff;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}
.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
}
.form-group {
  display: flex;
  flex-direction: column;
}
.form-group label {
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #495057;
}
.form-group select,
.form-group input {
  padding: 0.75rem;
  border: 1px solid #ced4da;
  border-radius: 4px;
  font-size: 1rem;
  font-family: inherit;
}
.form-actions {
  margin-top: 2rem;
  display: flex;
  gap: 1rem;
}
.form-actions button {
  padding: 0.8rem 1.5rem;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}
.form-actions button[type='submit'] {
  background-color: var(--brisa-blue-primary);
  color: white;
}
.form-actions button[type='submit']:hover {
  background-color: var(--brisa-blue-secondary);
}
.form-actions button[type='button'] {
  background-color: #f8f9fa;
  color: #343a40;
  border: 1px solid #ced4da;
}
.form-actions button[type='button']:hover {
  background-color: #e2e6ea;
}
.results-section {
  margin-top: 2rem;
}
.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  flex-wrap: wrap;
  gap: 1rem;
}
.results-header h3 {
  margin: 0;
  color: #495057;
}
.export-buttons {
  display: flex;
  gap: 1rem;
}
.export-buttons button {
  background-color: #6c757d;
  color: white;
  padding: 0.6rem 1.2rem;
  border-radius: 4px;
  border: none;
  cursor: pointer;
}
.export-buttons button:hover {
  background-color: #5a6268;
}
.no-results {
  margin-top: 2rem;
  padding: 3rem;
  text-align: center;
  border: 2px dashed #e0e0e0;
  border-radius: 8px;
  color: #6c757d;
}
</style>

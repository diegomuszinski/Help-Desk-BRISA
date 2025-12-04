<script setup lang="ts">
import { ref, watch } from 'vue'
import { useTicketStore } from '@/stores/ticketStore'
import TicketsPerMonthChart from '@/components/TicketsPerMonthChart.vue'
import AnalystPerformanceChart from '@/components/AnalystPerformanceChart.vue'
import AvgTimeByCategoryChart from '@/components/AvgTimeByCategoryChart.vue'
import SlaAlerts from '@/components/SlaAlerts.vue'
import jsPDF from 'jspdf'
import html2canvas from 'html2canvas'
import logoBrisa from '@/assets/brisa.png'
import { exportToCsv } from '@/utils/exporters'

const ticketStore = useTicketStore()
const activeTab = ref('byAnalyst')
const selectedYear = ref<number | null>(null)
const selectedMonth = ref<number | null>(null)

// Estrutura de meses para o dropdown
const monthOptions = [
  { value: 1, text: 'Janeiro' },
  { value: 2, text: 'Fevereiro' },
  { value: 3, text: 'Março' },
  { value: 4, text: 'Abril' },
  { value: 5, text: 'Maio' },
  { value: 6, text: 'Junho' },
  { value: 7, text: 'Julho' },
  { value: 8, text: 'Agosto' },
  { value: 9, text: 'Setembro' },
  { value: 10, text: 'Outubro' },
  { value: 11, text: 'Novembro' },
  { value: 12, text: 'Dezembro' },
]

watch([selectedYear, selectedMonth], ([newYear, newMonth]) => {
  if (newYear !== null && newMonth !== null) {
    ticketStore.setDateFilter(newYear, newMonth)
  }
})

async function printReport(reportId: string, reportName: string, reportTitle: string) {
  const reportElement = document.getElementById(reportId)
  if (!reportElement) return

  const header = document.createElement('div')
  header.style.padding = '20px'
  header.style.textAlign = 'center'
  header.style.backgroundColor = 'white'
  const date = new Date().toLocaleDateString('pt-BR')

  // Criar elementos de forma segura (sem innerHTML)
  const img = document.createElement('img')
  img.src = logoBrisa
  img.style.width = '80px'
  img.style.marginBottom = '10px'

  const title = document.createElement('h2')
  title.textContent = reportTitle // Usa textContent ao invés de innerHTML

  const dateP = document.createElement('p')
  dateP.textContent = `Data de Emissão: ${date}`

  header.appendChild(img)
  header.appendChild(title)
  header.appendChild(dateP)

  reportElement.prepend(header)

  const canvas = await html2canvas(reportElement)

  reportElement.removeChild(header)

  const imgData = canvas.toDataURL('image/png')
  const pdf = new jsPDF('p', 'mm', 'a4')
  const imgWidth = 210
  const pageHeight = 295
  const imgHeight = (canvas.height * imgWidth) / canvas.width
  let heightLeft = imgHeight
  let position = 0

  pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
  heightLeft -= pageHeight

  while (heightLeft >= 0) {
    position = heightLeft - imgHeight
    pdf.addPage()
    pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
    heightLeft -= pageHeight
  }
  pdf.save(`${reportName}.pdf`)
}

function exportReportAsCsv(
  reportData: { [key: string]: number },
  reportName: string,
  headers: [string, string],
) {
  exportToCsv(reportName, reportData, headers)
}
</script>

<template>
  <div class="reports-view">
    <SlaAlerts
      v-if="ticketStore.slaAlertTickets.length > 0"
      :tickets="ticketStore.slaAlertTickets"
    />

    <header class="view-header">
      <h1>Dashboards Gerenciais</h1>
      <div class="filters">
        <select v-model="selectedYear">
          <option :value="null">Todo o Período (Ano)</option>
          <option v-for="year in ticketStore.availableYears" :key="year" :value="year">
            {{ year }}
          </option>
        </select>
        <select v-model="selectedMonth">
          <option :value="null">Todo o Período (Mês)</option>
          <option
            v-for="month in monthOptions"
            :key="month.value"
            :value="month.value"
          >
            {{ month.text }}
          </option>
        </select>
      </div>
    </header>

    <nav class="tabs">
      <button @click="activeTab = 'byAnalyst'" :class="{ active: activeTab === 'byAnalyst' }">
        Por Analista
      </button>
      <button @click="activeTab = 'byCategory'" :class="{ active: activeTab === 'byCategory' }">
        Por Categoria
      </button>
      <button @click="activeTab = 'byMonth'" :class="{ active: activeTab === 'byMonth' }">
        Por Mês
      </button>
    </nav>

    <div class="tab-content">
      <div v-if="activeTab === 'byAnalyst'">
        <div id="analystReport" class="report-card">
          <h2>Chamados Atendidos por Analista</h2>
          <AnalystPerformanceChart :report-data="ticketStore.analystPerformanceReport" />
        </div>
        <div class="action-buttons">
          <button
            @click="printReport('analystReport', 'dashboard_por_analista', 'Chamados por Analista')"
            class="print-btn"
          >
            Imprimir PDF
          </button>
          <button
            @click="
              exportReportAsCsv(
                Object.fromEntries(ticketStore.analystPerformanceReport.map(item => [item.nomeAnalista, item.totalChamados])),
                'dashboard_por_analista',
                ['Analista', 'Chamados Atendidos']
              )
            "
            class="export-btn"
          >
            Exportar CSV
          </button>
        </div>
      </div>

      <div v-if="activeTab === 'byCategory'">
        <div id="categoryReport" class="report-card">
          <h2>Tempo Médio de Resolução (por Categoria)</h2>
          <AvgTimeByCategoryChart :report-data="ticketStore.avgResolutionTimeByCategoryReport" />
        </div>
        <div class="action-buttons">
          <button
            @click="
              printReport('categoryReport', 'dashboard_por_categoria', 'Tempo Médio por Categoria')
            "
            class="print-btn"
          >
            Imprimir PDF
          </button>
          <button
            @click="
              exportReportAsCsv(
                Object.fromEntries(ticketStore.avgResolutionTimeByCategoryReport.map(item => [item.categoria, item.tempoMedioHoras])),
                'dashboard_por_categoria',
                ['Categoria', 'Tempo Médio (horas)']
              )
            "
            class="export-btn"
          >
            Exportar CSV
          </button>
        </div>
      </div>

      <div v-if="activeTab === 'byMonth'">
        <div id="monthReport" class="report-card">
          <h2>Total de Chamados por Mês</h2>
          <TicketsPerMonthChart :report-data="ticketStore.ticketsPerMonthReport" />
        </div>
        <div class="action-buttons">
          <button
            @click="printReport('monthReport', 'dashboard_por_mes', 'Total de Chamados por Mês')"
            class="print-btn"
          >
            Imprimir PDF
          </button>
          <button
            @click="
              exportReportAsCsv(
                Object.fromEntries(ticketStore.ticketsPerMonthReport.map(item => [`Mês ${item.mes}`, item.totalChamados])),
                'dashboard_por_mes',
                ['Mês/Ano', 'Total de Chamados']
              )
            "
            class="export-btn"
          >
            Exportar CSV
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.reports-view {
  padding: 2rem;
}
.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}
.view-header h1 {
  margin: 0;
  font-size: 1.8rem;
  color: #2c3e50;
}
.filters {
  display: flex;
  gap: 1rem;
}
.filters select {
  padding: 0.5rem;
  border-radius: 4px;
  border: 1px solid #ccc;
  background-color: #fff;
  font-family: inherit;
  font-size: 0.9rem;
}
.tabs {
  display: flex;
  gap: 0.5rem;
  border-bottom: 2px solid #ccc;
  margin-bottom: 1.5rem;
}
.tabs button {
  padding: 0.8rem 1.5rem;
  border: none;
  background-color: transparent;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 500;
  color: #6c757d;
  border-bottom: 3px solid transparent;
  transform: translateY(2px);
}
.tabs button.active {
  color: var(--brisa-blue-primary);
  border-bottom: 3px solid var(--brisa-blue-primary);
}
.report-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}
.report-card h2 {
  margin-top: 0;
  margin-bottom: 1rem;
  font-size: 1.2rem;
  color: var(--brisa-blue-primary);
}
.action-buttons {
  margin-top: 1rem;
  display: flex;
  gap: 1rem;
}
.print-btn {
  padding: 0.6rem 1.2rem;
  background-color: #6c757d;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
}
.print-btn:hover {
  background-color: #5a6268;
}
.export-btn {
  padding: 0.6rem 1.2rem;
  background-color: #217346;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
}
.export-btn:hover {
  background-color: #185433;
}
</style>

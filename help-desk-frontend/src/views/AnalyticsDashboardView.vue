<script setup lang="ts">
import { ref, watch, onMounted, computed } from 'vue'
import BarChart from '@/components/BarChart.vue'
import api from '@/services/api'
import type { RelatorioAnalista, RelatorioCategoria, RelatorioMensal } from '@/types/index'
import type { ChartOptions } from 'chart.js'
import jsPDF from 'jspdf'
import html2canvas from 'html2canvas'
import logoBrisa from '@/assets/brisa.png'

type Tab = 'analyst' | 'category' | 'month'

interface ChartData {
  labels: string[]
  datasets: {
    label: string
    backgroundColor: string
    data: (number | null)[]
  }[]
}

const activeTab = ref<Tab>('analyst')
const selectedYear = ref<number | 'all'>('all')
const selectedMonth = ref<number | 'all'>('all')

const reportData = ref<ChartData | null>(null)
const isLoading = ref(false)
const isExporting = ref(false)
const chartAreaRef = ref<HTMLElement | null>(null)

const years = [new Date().getFullYear(), new Date().getFullYear() - 1, new Date().getFullYear() - 2]
const months = [
  { value: 1, name: 'Janeiro' },
  { value: 2, name: 'Fevereiro' },
  { value: 3, name: 'Março' },
  { value: 4, name: 'Abril' },
  { value: 5, name: 'Maio' },
  { value: 6, name: 'Junho' },
  { value: 7, name: 'Julho' },
  { value: 8, name: 'Agosto' },
  { value: 9, name: 'Setembro' },
  { value: 10, name: 'Outubro' },
  { value: 11, name: 'Novembro' },
  { value: 12, name: 'Dezembro' },
]

const reportTitle = computed(() => {
  switch (activeTab.value) {
    case 'analyst':
      return 'Chamados Atendidos por Analista'
    case 'category':
      return 'Tempo Médio de Resolução por Categoria'
    case 'month':
      return 'Total de Chamados por Mês'
    default:
      return 'Relatório Gerencial'
  }
})

const chartOptions = computed<ChartOptions<'bar'>>(() => {
  const options: ChartOptions<'bar'> = {
    plugins: {
      tooltip: {
        callbacks: {
          label: function (context) {
            let label = context.dataset.label || ''
            if (label) {
              label += ': '
            }
            if (context.parsed.y !== null) {
              // Formata o número para ter no máximo 2 casas decimais
              label += context.parsed.y.toFixed(2)
            }
            return label
          },
        },
      },
    },
    scales: {
      y: {
        beginAtZero: true,
      },
    },
  }

  if (activeTab.value === 'analyst' || activeTab.value === 'month') {
    if (options.scales?.y) {
      options.scales.y.ticks = {
        stepSize: 1,
        callback: function (value) {
          if (Number.isInteger(value)) {
            return value
          }
          return null
        },
      }
      if (activeTab.value === 'analyst' && reportData.value?.datasets[0]?.data.length) {
        const data = reportData.value.datasets[0].data.filter(
          (v) => v !== null,
        ) as number[]
        if (data.length > 0) {
          options.scales.y.max = Math.max(...data, 1)
        }
      }
    }
  }

  return options
})

const fetchData = async () => {
  isLoading.value = true
  reportData.value = null

  const params = new URLSearchParams()
  if (selectedYear.value !== 'all') params.append('year', selectedYear.value.toString())
  if (selectedMonth.value !== 'all' && activeTab.value !== 'month')
    params.append('month', selectedMonth.value.toString())

  let url = ''
  switch (activeTab.value) {
    case 'analyst':
      url = '/api/reports/by-analyst'
      break
    case 'category':
      url = '/api/reports/by-category'
      break
    case 'month':
      url = '/api/reports/by-month'
      break
  }

  try {
    const response = await api.get(`${url}?${params.toString()}`)
    const monthNames = months.map((m) => m.name)

    if (activeTab.value === 'analyst') {
      const data = response.data as RelatorioAnalista[]
      reportData.value = {
        labels: data.map((d) => d.nomeAnalista),
        datasets: [
          {
            label: 'Chamados Atendidos',
            backgroundColor: '#42b983',
            data: data.map((d) => d.totalChamados),
          },
        ],
      }
    } else if (activeTab.value === 'category') {
      const data = response.data as RelatorioCategoria[]
      reportData.value = {
        labels: data.map((d) => d.categoria),
        datasets: [
          {
            label: 'Tempo Médio (em minutos)',
            backgroundColor: '#f87979',
            data: data.map((d) => d.tempoMedioHoras),
          },
        ],
      }
    } else if (activeTab.value === 'month') {
      const data = response.data as RelatorioMensal[]
      const fullYearData: (number | null)[] = Array(12).fill(null)
      data.forEach((d) => {
        if (d.mes >= 1 && d.mes <= 12) {
          fullYearData[d.mes - 1] = d.totalChamados
        }
      })

      reportData.value = {
        labels: monthNames,
        datasets: [{ label: 'Total de Chamados', backgroundColor: '#3498db', data: fullYearData }],
      }
    }
  } catch (error) {
    console.error('Falha ao buscar dados do relatório:', error)
  } finally {
    isLoading.value = false
  }
}

async function exportToPDF() {
  const chartElement = chartAreaRef.value
  if (!chartElement) return

  isExporting.value = true

  try {
    const canvas = await html2canvas(chartElement, { scale: 2 })
    const imgData = canvas.toDataURL('image/png')
    const doc = new jsPDF('p', 'mm', 'a4')
    const docWidth = doc.internal.pageSize.getWidth()
    const margin = 15

    doc.addImage(logoBrisa, 'PNG', margin, 10, 40, 15)
    doc.setFontSize(18)
    doc.text('Relatório Gerencial', docWidth / 2, 20, { align: 'center' })
    doc.setFontSize(12)
    doc.setTextColor(100)
    doc.text(reportTitle.value, margin, 40)
    const date = new Date().toLocaleDateString('pt-BR')
    doc.text(`Gerado em: ${date}`, docWidth - margin, 40, { align: 'right' })
    const imgWidth = docWidth - margin * 2
    const imgHeight = (canvas.height * imgWidth) / canvas.width
    doc.addImage(imgData, 'PNG', margin, 50, imgWidth, imgHeight)
    const fileName = `relatorio_${activeTab.value}_${date.replace(/\//g, '-')}.pdf`
    doc.save(fileName)
  } catch (error) {
    console.error('Erro ao gerar PDF:', error)
  } finally {
    isExporting.value = false
  }
}

onMounted(fetchData)
watch([activeTab, selectedYear, selectedMonth], fetchData, { immediate: false })
</script>

<template>
  <div class="reports-view">
    <header class="view-header">
      <h1>Relatórios Gerenciais</h1>
      <div class="filters">
        <select v-model="selectedYear">
          <option value="all">Todo o Período (Ano)</option>
          <option v-for="year in years" :key="year" :value="year">{{ year }}</option>
        </select>
        <select v-model="selectedMonth" :disabled="activeTab === 'month'">
          <option value="all">Todo o Período (Mês)</option>
          <option v-for="month in months" :key="month.value" :value="month.value">
            {{ month.name }}
          </option>
        </select>
      </div>
    </header>

    <div class="tabs">
      <button :class="{ active: activeTab === 'analyst' }" @click="activeTab = 'analyst'">
        Por Analista
      </button>
      <button :class="{ active: activeTab === 'category' }" @click="activeTab = 'category'">
        Por Categoria
      </button>
      <button :class="{ active: activeTab === 'month' }" @click="activeTab = 'month'">
        Por Mês
      </button>
    </div>

    <div class="chart-area" ref="chartAreaRef">
      <div v-if="isLoading" class="loading-state">Carregando dados...</div>
      <div
        v-else-if="
          reportData && reportData.datasets[0].data.some((d) => d !== null && d > 0)
        "
      >
        <BarChart :chart-data="reportData" :chart-options="chartOptions" />
      </div>
      <div v-else class="no-data">Nenhum dado encontrado para os filtros selecionados.</div>
    </div>

    <div class="export-actions">
      <button @click="exportToPDF" :disabled="isExporting" class="export-btn">
        {{ isExporting ? 'Gerando...' : 'Imprimir PDF' }}
      </button>
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
}
.tabs {
  margin-bottom: 1.5rem;
  border-bottom: 1px solid #dee2e6;
}
.tabs button {
  padding: 0.8rem 1.5rem;
  border: none;
  background-color: transparent;
  cursor: pointer;
  font-size: 1rem;
  color: #6c757d;
  border-bottom: 3px solid transparent;
  margin-bottom: -1px;
}
.tabs button.active {
  color: var(--brisa-blue-primary);
  border-bottom-color: var(--brisa-blue-primary);
  font-weight: 600;
}
.chart-area {
  background: #fff;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}
.loading-state,
.no-data {
  text-align: center;
  padding: 4rem;
  color: #6c757d;
  font-size: 1.2rem;
}
.export-actions {
  margin-top: 1.5rem;
  display: flex;
  justify-content: flex-start;
}
.export-btn {
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
.export-btn:hover {
  background-color: var(--brisa-blue-secondary);
}
.export-btn:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}
</style>

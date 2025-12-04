<script setup lang="ts">
import { useTicketStore } from '@/stores/ticketStore'
import { Bar } from 'vue-chartjs'
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale,
} from 'chart.js'
import { computed } from 'vue'

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale)

const ticketStore = useTicketStore()

const chartData = computed(() => {
  const dist = ticketStore.satisfactionReport.distribuicao
  return {
    labels: ['Muito Satisfeito', 'Satisfeito', 'Neutro', 'Insatisfeito', 'Muito Insatisfeito'],
    datasets: [
      {
        label: 'Quantidade de Avaliações',
        backgroundColor: ['#28a745', '#a3d679', '#ffc107', '#fd7e14', '#dc3545'],
        data: [
          dist.muitoSatisfeito,
          dist.satisfeito,
          dist.neutro,
          dist.insatisfeito,
          dist.muitoInsatisfeito,
        ],
      },
    ],
  }
})

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  indexAxis: 'y' as const,
  plugins: {
    legend: {
      display: false,
    },
  },
  scales: {
    x: {
      ticks: {
        stepSize: 1,
      },
    },
  },
}
</script>

<template>
  <div class="satisfaction-view">
    <header class="view-header">
      <h1>Relatório de Satisfação do Cliente</h1>
    </header>

    <div class="report-card">
      <div class="chart-container">
        <Bar :data="chartData" :options="chartOptions" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.satisfaction-view {
  padding: 2rem;
}
.view-header {
  margin-bottom: 2rem;
}
.view-header h1 {
  margin: 0;
  font-size: 1.8rem;
  color: #2c3e50;
}
.report-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}
.chart-container {
  height: 400px;
  position: relative;
}
</style>

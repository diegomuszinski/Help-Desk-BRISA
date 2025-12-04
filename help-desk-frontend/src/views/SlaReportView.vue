<script setup lang="ts">
import { useTicketStore } from '@/stores/ticketStore'
import { Doughnut } from 'vue-chartjs'
import { Chart as ChartJS, Title, Tooltip, Legend, ArcElement } from 'chart.js'
import { computed } from 'vue'

ChartJS.register(Title, Tooltip, Legend, ArcElement)

const ticketStore = useTicketStore()

const chartData = computed(() => ({
  labels: ['No Prazo', 'Fora do Prazo'],
  datasets: [
    {
      backgroundColor: ['#28a745', '#dc3545'],
      data: [
        ticketStore.slaPerformanceReport.dentroDoSla,
        ticketStore.slaPerformanceReport.foraDoSla,
      ],
    },
  ],
}))

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
}
</script>

<template>
  <div class="sla-report-view">
    <header class="view-header">
      <h1>Relatório de Performance de SLA</h1>
    </header>

    <div class="report-card">
      <div class="chart-container">
        <Doughnut :data="chartData" :options="chartOptions" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.sla-report-view {
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
  max-width: 400px;
  margin: 0 auto;
  position: relative;
}
</style>

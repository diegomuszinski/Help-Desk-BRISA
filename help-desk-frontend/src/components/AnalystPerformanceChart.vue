<script setup lang="ts">
import { computed } from 'vue'
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

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale)

const props = defineProps<{
  reportData: { nomeAnalista: string; totalChamados: number }[]
}>()

const chartData = computed(() => ({
  labels: props.reportData.map(item => item.nomeAnalista),
  datasets: [
    {
      label: 'Chamados Atendidos',
      backgroundColor: '#28a745', // Verde
      data: props.reportData.map(item => item.totalChamados),
    },
  ],
}))

const chartOptions = { responsive: true, maintainAspectRatio: false }
</script>
<template>
  <div class="chart-container">
    <Bar :data="chartData" :options="chartOptions" />
  </div>
</template>
<style scoped>
.chart-container {
  height: 400px;
  position: relative;
}
</style>

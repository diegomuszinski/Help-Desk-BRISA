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
  reportData: { categoria: string; tempoMedioHoras: number }[]
}>()

const chartData = computed(() => ({
  labels: props.reportData.map(item => item.categoria),
  datasets: [
    {
      label: 'Tempo Médio (em horas)',
      backgroundColor: '#fd7e14', // Laranja
      data: props.reportData.map(item => item.tempoMedioHoras),
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

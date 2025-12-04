<script setup lang="ts">
import { Bar } from 'vue-chartjs'
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale,
  type ChartOptions,
} from 'chart.js'
import { computed } from 'vue'

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale)

const props = defineProps<{
  chartData: {
    labels: string[]
    datasets: {
      label: string
      backgroundColor: string
      data: (number | null)[]
    }[]
  }
  chartOptions?: ChartOptions<'bar'>
}>()

const options = computed(() => {
  const defaultOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
  }

  return { ...defaultOptions, ...props.chartOptions }
})
</script>

<template>
  <div style="height: 400px">
    <Bar :data="props.chartData" :options="options" />
  </div>
</template>

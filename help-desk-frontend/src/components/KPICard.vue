<script setup lang="ts">
defineProps<{
  title: string
  value: string | number
  isWarning?: boolean
}>()
</script>

<template>
  <div class="kpi-card" :class="{ 'is-warning': isWarning }">
    <span class="value">{{ value }}</span>
    <span class="title">{{ title }}</span>
  </div>
</template>

<style scoped>
.kpi-card {
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: 1.5rem;
  text-align: center;
  display: flex;
  flex-direction: column-reverse;
  gap: 0.5rem;
}

.title {
  font-size: 1rem;
  color: #6c757d;
  font-weight: 500;
}

.value {
  font-size: 3rem;
  font-weight: 700;
  color: var(--brisa-blue-primary);
}

.kpi-card.is-warning {
  background-color: #fffbe6;
  color: #856404;
}

.kpi-card.is-warning .value {
  color: #d97706;
}
</style>

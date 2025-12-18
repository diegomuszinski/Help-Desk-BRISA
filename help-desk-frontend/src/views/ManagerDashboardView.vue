<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useTicketStore } from '@/stores/ticketStore'
import TicketList from '@/components/TicketList.vue'
import KPICard from '@/components/KPICard.vue'

const ticketStore = useTicketStore()

onMounted(() => {
  ticketStore.fetchDashboardStats()
})

const stats = computed(() => ticketStore.dashboardStats)
</script>

<template>
  <div class="manager-dashboard">
    <header class="view-header">
      <h1>Painel de Gestão em Tempo Real</h1>
    </header>

    <div v-if="stats" class="dashboard-content">
      <div class="kpi-grid">
        <KPICard title="Chamados aguardando atendimento" :value="stats.chamadosNaFila" />
        <KPICard
          title="Chamados com SLA violado"
          :value="stats.chamadosSlaViolado.length"
          is-warning
        />
        <KPICard
          title="Total em Atendimento"
          :value="
            stats.chamadosPorAnalista.reduce((total: number, analista: any) => total + analista.totalChamados, 0)
          "
        />
      </div>

      <div class="details-grid">
        <div class="kpi-card-large">
          <h2 class="title">Chamados em atendimento por analista</h2>
          <div v-if="stats.chamadosPorAnalista.length > 0" class="analyst-list">
            <div
              v-for="analyst in stats.chamadosPorAnalista"
              :key="analyst.nomeAnalista"
              class="analyst-item"
            >
              <span class="analyst-name">{{ analyst.nomeAnalista }}</span>
              <span class="analyst-count">{{ analyst.totalChamados }}</span>
            </div>
          </div>
          <div v-else class="no-data">Nenhum chamado em atendimento no momento.</div>
        </div>

        <div class="kpi-card-large">
          <h2 class="title">Lista de Chamados com SLA Violado</h2>
          <TicketList
            v-if="stats.chamadosSlaViolado.length > 0"
            :tickets="stats.chamadosSlaViolado"
          />
          <div v-else class="no-data">Nenhum chamado com SLA violado. Parabéns!</div>
        </div>
      </div>
    </div>
    <div v-else class="loading-state">
      <p>Carregando dados do painel...</p>
    </div>
  </div>
</template>

<style scoped>
.manager-dashboard {
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
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}
.details-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
}
.kpi-card-large {
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: 1.5rem;
}
.kpi-card-large .title {
  margin-top: 0;
  margin-bottom: 1.5rem;
  font-size: 1.2rem;
  color: #6c757d;
  font-weight: 500;
}
.analyst-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.analyst-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.8rem;
  background-color: #f8f9fa;
  border-radius: 6px;
}
.analyst-name {
  font-weight: 500;
  color: #333;
}
.analyst-count {
  font-weight: 700;
  font-size: 1.2rem;
  background-color: var(--brisa-blue-secondary);
  color: white;
  padding: 0.2rem 0.8rem;
  border-radius: 12px;
}
.no-data,
.loading-state {
  text-align: center;
  color: #6c757d;
  padding: 2rem;
  font-size: 1.1rem;
}
</style>

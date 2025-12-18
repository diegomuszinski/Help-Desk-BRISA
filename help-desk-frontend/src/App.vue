<script setup lang="ts">
import { computed } from 'vue'
import { RouterView } from 'vue-router'
import { useTicketStore } from '@/stores/ticketStore'
import AppSidebar from './components/AppSidebar.vue'

const ticketStore = useTicketStore()

const showSidebar = computed(() => {
  return !!ticketStore.currentUser.email
})
</script>

<template>
  <div class="app-layout">
    <AppSidebar v-if="showSidebar" />
    <main class="main-content">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.app-layout {
  display: flex;
}
.main-content {
  flex-grow: 1;
  height: 100vh;
  overflow-y: auto;
  background-color: #f8f9fa;
}
</style>

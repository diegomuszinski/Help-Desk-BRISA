<script setup lang="ts">
import { ref } from 'vue'
import { useTicketStore } from '@/stores/ticketStore'
import { useRouter } from 'vue-router'
import logoBrisa from '@/assets/brisa.png'

const ticketStore = useTicketStore()
const router = useRouter()

const email = ref('')
const password = ref('')
const errorMessage = ref('')
const showPassword = ref(false)
const isLoading = ref(false)

async function handleLogin() {
  errorMessage.value = ''
  isLoading.value = true

  try {
    await ticketStore.login({ email: email.value, senha: password.value })

    const userRole = ticketStore.currentUser.role
    if (userRole === 'user') {
      router.push('/meus-chamados')
    } else {
      router.push('/fila/abertos')
    }
  } catch (error: unknown) {
    const axiosError = error as { response?: { status?: number; data?: string } }
    // Tratar erro 429 (rate limit)
    if (axiosError.response?.status === 429) {
      errorMessage.value = axiosError.response.data || 'Muitas tentativas de login. Aguarde 1 minuto e tente novamente.'
    }
    // Tratar erro 401 (credenciais inválidas)
    else if (axiosError.response?.status === 401) {
      errorMessage.value = 'Email ou senha inválidos. Por favor, verifique suas credenciais.'
    }
    // Outros erros
    else {
      errorMessage.value = 'Erro ao fazer login. Por favor, tente novamente.'
    }
    console.error('Falha no login:', error)
  } finally {
    isLoading.value = false
  }
}

function activateField(event: FocusEvent) {
  const target = event.target as HTMLInputElement
  target.removeAttribute('readonly')
}
</script>

<template>
  <div class="login-view">
    <div class="login-card">
      <img :src="logoBrisa" alt="Logo Brisa" class="logo" />
      <h1 class="title">Help Desk</h1>

      <form @submit.prevent="handleLogin" autocomplete="off">
        <div class="form-group">
          <label for="email">Email:</label>
          <input
            type="email"
            id="email"
            v-model="email"
            required
            readonly
            @focus="activateField"
            autocomplete="off"
          />
        </div>
        <div class="form-group">
          <label for="password">Senha:</label>
          <div class="password-wrapper">
            <input
              :type="showPassword ? 'text' : 'password'"
              id="password"
              v-model="password"
              required
              readonly
              @focus="activateField"
              autocomplete="new-password"
            />
            <button type="button" @click="showPassword = !showPassword" class="show-password-btn">
              {{ showPassword ? 'Ocultar' : 'Mostrar' }}
            </button>
          </div>
        </div>

        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <button type="submit" class="submit-btn" :disabled="isLoading">
          {{ isLoading ? 'Entrando...' : 'Entrar' }}
        </button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.login-view {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f0f2f5;
}
.login-card {
  background: #fff;
  padding: 3rem;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
  text-align: center;
}
.logo {
  max-width: 120px;
  margin-bottom: 1rem;
}
.title {
  font-size: 2rem;
  color: #333;
  margin-bottom: 2rem;
}
form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  text-align: left;
}
.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #555;
}
.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1rem;
}

.form-group input[readonly] {
  background-color: #f9f9f9;
  cursor: text;
}
.password-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}
.password-wrapper input {
  padding-right: 70px;
}
.show-password-btn {
  position: absolute;
  right: 1px;
  top: 1px;
  bottom: 1px;
  background: transparent;
  border: none;
  padding: 0 1rem;
  cursor: pointer;
  color: #555;
  font-weight: 500;
}
.error-message {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
  padding: 0.75rem;
  border-radius: 4px;
  text-align: center;
}
.submit-btn {
  padding: 0.8rem;
  font-size: 1.1rem;
  font-weight: 600;
  color: #fff;
  background-color: var(--brisa-blue-primary);
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}
.submit-btn:hover {
  background-color: var(--brisa-blue-secondary);
}
</style>

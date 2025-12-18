import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  withCredentials: true, // Necessário para enviar/receber cookies HttpOnly
})

// Interceptor de REQUEST: Com cookies HttpOnly, não é mais necessário adicionar token manualmente
// O navegador envia automaticamente os cookies em cada requisição
api.interceptors.request.use(
  (config) => {
    // Cookies são enviados automaticamente pelo navegador
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Interceptor de RESPONSE: Trata erros 401 (token expirado) e 429 (rate limit)
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    // Se receber 401 (token expirado) e ainda não tentou refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      try {
        // Tentar renovar tokens (refreshToken será enviado automaticamente via cookie)
        const baseURL = api.defaults.baseURL || 'http://localhost:8080'
        await axios.post(
          `${baseURL}/api/auth/refresh`,
          {}, // Body vazio, refreshToken vem do cookie
          { withCredentials: true } // Garantir que cookies sejam enviados
        )

        // Backend já atualizou os cookies automaticamente
        // Retentar requisição original
        return api(originalRequest)
      } catch (refreshError) {
        // Falha no refresh, fazer logout
        window.location.href = '/login'
        return Promise.reject(refreshError)
      }
    }

    // Se receber 429 (rate limit)
    if (error.response?.status === 429) {
      const message = error.response.data || 'Muitas tentativas. Aguarde 1 minuto.'
      console.warn('Rate limit atingido:', message)
      // Propagar erro para ser tratado no componente
      error.userMessage = message
    }

    return Promise.reject(error)
  }
)

export default api

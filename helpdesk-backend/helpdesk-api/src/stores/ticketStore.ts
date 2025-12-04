import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { User, Ticket } from '@/types';
import api from '@/services/api';
import { jwtDecode } from 'jwt-decode';

export const useTicketStore = defineStore('tickets', () => {
  const currentUser = ref<User>({ name: '', email: '', role: null });
  const token = ref(sessionStorage.getItem('token') || '');

  if (token.value) {
    const decoded: any = jwtDecode(token.value);
    currentUser.value.name = decoded.name;
    currentUser.value.role = decoded.role;
    currentUser.value.email = decoded.sub; 
    api.defaults.headers.common['Authorization'] = `Bearer ${token.value}`;
  }

  async function login(credentials: { email: string, senha: any }) {
    const response = await api.post('/api/auth/login', credentials);
    const newToken = response.data.token;
    token.value = newToken;
    
    sessionStorage.setItem('token', newToken);
    api.defaults.headers.common['Authorization'] = `Bearer ${newToken}`;
    
    const decoded: any = jwtDecode(newToken);
    currentUser.value.name = decoded.name;
    currentUser.value.role = decoded.role;
    currentUser.value.email = decoded.sub;
  }

  function logout() {
    currentUser.value = { name: '', email: '', role: null };
    token.value = '';
    sessionStorage.removeItem('token');
    api.defaults.headers.common['Authorization'] = '';
  }


  
  return { currentUser, token, login, logout };
});
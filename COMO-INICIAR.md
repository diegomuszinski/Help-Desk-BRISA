# 🚀 Help Desk - Como Iniciar

Este documento explica como iniciar o sistema Help Desk de forma automatizada.

---

## ⚡ Início Rápido (Recomendado)

### Opção 1: Arquivo BAT (Clique Duplo)

1. Na pasta raiz do projeto, clique duas vezes em:
   ```
   START-HELPDESK.bat
   ```

2. O script irá:
   - ✅ Verificar se o backend está rodando
   - ✅ Iniciar o backend automaticamente (se necessário)
   - ✅ Aguardar o backend ficar pronto
   - ✅ Iniciar o frontend Vue.js
   - ✅ Abrir o navegador automaticamente

3. Pronto! O sistema estará disponível em:
   - **Frontend**: http://localhost:5173
   - **Backend**: http://localhost:8080

---

### Opção 2: Via NPM (Terminal)

1. Abra o terminal na pasta `help-desk-frontend`:
   ```powershell
   cd help-desk-frontend
   ```

2. Execute o comando:
   ```powershell
   npm run start
   ```

3. O sistema irá iniciar backend + frontend automaticamente

---

### Opção 3: PowerShell Direto

1. Abra o terminal na pasta `help-desk-frontend`:
   ```powershell
   cd help-desk-frontend
   ```

2. Execute o script:
   ```powershell
   .\start-dev.ps1
   ```

---

## 🔑 Credenciais de Teste

Após o sistema iniciar, use uma destas credenciais para fazer login:

| Perfil | E-mail | Senha |
|--------|--------|-------|
| **ADMIN** | admin@admin.net | 123456 |
| **MANAGER** | sonia.lima@gestor.net | 1234546 |
| **TECHNICIAN** | mariana@tecnico.net | 123456 |
| **USER** | usuario@teste.net | 123456 |

---

## 🛠️ Início Manual (Se Preferir)

### Backend:
```powershell
cd helpdesk-backend/helpdesk-api
.\configurar-env.ps1
.\gradlew.bat bootRun
```

### Frontend (em outro terminal):
```powershell
cd help-desk-frontend
npm run dev
```

---

## ⚠️ Pré-requisitos

Certifique-se de ter instalado:

- ✅ **Java 17** ou superior
- ✅ **Node.js 20** ou superior
- ✅ **PostgreSQL 12** ou superior
- ✅ **Banco de dados** `helpdesk` criado e populado

---

## 🔧 Verificar se está Rodando

### Backend:
```powershell
curl http://localhost:8080/api/auth/login -Method OPTIONS
```

### Frontend:
Abra o navegador em: http://localhost:5173

---

## 🐛 Solução de Problemas

### Backend não inicia:
1. Verifique se o PostgreSQL está rodando
2. Confirme que o banco `helpdesk` existe
3. Verifique as credenciais no arquivo `.env`

### Frontend não conecta:
1. Verifique se o backend está rodando (porta 8080)
2. Confirme que o CORS está configurado
3. Limpe o cache do navegador (Ctrl+Shift+Delete)

### Porta já está em uso:
```powershell
# Verificar o que está usando a porta 8080
netstat -ano | findstr :8080

# Matar o processo (substitua <PID> pelo número do processo)
taskkill /PID <PID> /F
```

---

## 📱 URLs do Sistema

- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080
- **Swagger (se habilitado)**: http://localhost:8080/swagger-ui.html

---

## 🎯 Próximos Passos

Após iniciar o sistema:

1. Faça login com uma das credenciais acima
2. Explore o dashboard
3. Crie um novo ticket
4. Teste as funcionalidades

---

**Desenvolvido com ❤️ para facilitar sua vida!**

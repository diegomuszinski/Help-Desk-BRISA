# 🎫 Sistema Help Desk - Guia de Configuração

Sistema completo de Help Desk com frontend (Vue.js) e backend (Spring Boot + PostgreSQL).

---

## 📋 Estrutura do Projeto

```
Arquivos/
├── help-desk-frontend/        # Frontend Vue.js + Vite
├── helpdesk-backend/          # Backend Spring Boot + Java
│   └── helpdesk-api/
├── init-database.sql          # Script de inicialização do banco ⭐
└── SETUP-BANCO.md            # Guia detalhado de setup do banco
```

---

## 🚀 Quick Start (3 Passos)

### 1️⃣ Configurar Banco de Dados PostgreSQL

**Opção A: pgAdmin (Visual)**
1. Abra o pgAdmin
2. Crie o banco `helpdesk` (botão direito → Databases → Create)
3. Abra Query Tool no banco `helpdesk`
4. Execute o arquivo `init-database.sql` (Open File → F5)

**Opção B: Terminal psql**
```bash
# Criar banco
psql -U postgres -c "CREATE DATABASE helpdesk WITH ENCODING 'UTF8';"

# Executar script
psql -U postgres -d helpdesk -f init-database.sql
```

📖 **Mais detalhes**: Veja o arquivo `SETUP-BANCO.md`

---

### 2️⃣ Iniciar o Backend (Spring Boot)

```bash
cd helpdesk-backend/helpdesk-api

# Windows
gradlew.bat bootRun

# Linux/Mac
./gradlew bootRun
```

O backend estará disponível em: **http://localhost:8080**

**Credenciais do Banco** (já configuradas no `application.properties`):
- Username: `postgres`
- Password: `admin`
- Database: `helpdesk`

---

### 3️⃣ Iniciar o Frontend (Vue.js)

```bash
cd help-desk-frontend

# Instalar dependências (primeira vez)
npm install

# Iniciar servidor de desenvolvimento
npm run dev
```

O frontend estará disponível em: **http://localhost:5173**

---

## 🔐 Usuários de Teste

Use estas credenciais para fazer login no sistema:

| Email | Senha | Perfil | Descrição |
|-------|-------|--------|-----------|
| `admin@admin.net` | `123456` | **ADMIN** | Administrador completo |
| `sonia.lima@gestor.net` | `1234546` | **MANAGER** | Gestor de equipe |
| `mariana@tecnico.net` | `123456` | **TECHNICIAN** | Técnico de suporte |
| `usuario@teste.net` | `123456` | **USER** | Usuário comum |

⚠️ **IMPORTANTE**: Altere estas senhas antes de usar em produção!

---

## 🗄️ Estrutura do Banco de Dados

O script `init-database.sql` cria automaticamente:

### Tabelas Principais:
- **usuarios** - Usuários do sistema (4 perfis)
- **equipes** - Equipes de suporte
- **chamados** - Tickets/chamados
- **categorias** - Categorias dos chamados (6 padrão)
- **prioridades** - Níveis de prioridade (4 padrão)
- **historico_chamados** - Histórico de interações
- **anexos_chamados** - Arquivos anexados
- **pesquisas_satisfacao** - Avaliações de satisfação

### Dados Padrão Inseridos:
- ✅ 6 categorias (Toner, Software, Hardware, etc.)
- ✅ 4 prioridades (Baixa, Média, Alta, Crítica)
- ✅ 1 equipe de suporte
- ✅ 4 usuários de teste

---

## 🛠️ Tecnologias Utilizadas

### Frontend
- Vue.js 3
- TypeScript
- Vite
- Pinia (State Management)
- Vue Router

### Backend
- Java 17+
- Spring Boot
- Spring Security + JWT
- PostgreSQL
- Gradle

---

## 📝 Scripts Disponíveis

### Backend
```bash
./gradlew bootRun          # Iniciar aplicação
./gradlew build            # Build do projeto
./gradlew test             # Executar testes
```

### Frontend
```bash
npm run dev                # Servidor de desenvolvimento
npm run build              # Build para produção
npm run preview            # Preview do build
npm run lint               # Verificar código
```

---

## 🔧 Configuração

### Backend (application.properties)

O arquivo está em: `helpdesk-backend/helpdesk-api/src/main/resources/application.properties`

```properties
# Banco de dados
spring.datasource.url=jdbc:postgresql://localhost:5432/helpdesk
spring.datasource.username=postgres
spring.datasource.password=admin

# Porta do servidor
server.port=8080
```

### Frontend

O arquivo de configuração da API está em: `help-desk-frontend/src/services/api.ts`

```typescript
const API_BASE_URL = 'http://localhost:8080'
```

---

## 🐛 Troubleshooting

### Erro: "database does not exist"
```bash
# Crie o banco primeiro
psql -U postgres -c "CREATE DATABASE helpdesk WITH ENCODING 'UTF8';"
```

### Erro: "Connection refused" no backend
- Verifique se o PostgreSQL está rodando
- Confirme as credenciais no `application.properties`
- Verifique se o banco `helpdesk` existe

### Erro: "Network Error" no frontend
- Verifique se o backend está rodando (http://localhost:8080)
- Confirme a URL da API no frontend

### Erro no pgAdmin: "Mark decorations may not be empty"
- Este é um bug do pgAdmin
- **Solução**: Copie e cole o script em vez de usar "Open File"
- Ou use o DBeaver: https://dbeaver.io/download/

---

## 📚 Documentação Adicional

- **SETUP-BANCO.md** - Guia detalhado de configuração do banco de dados
- **init-database.sql** - Script SQL completo comentado

---

## ⚙️ Ambiente de Desenvolvimento

### Pré-requisitos
- **Node.js** 18+ (para o frontend)
- **Java** 17+ (para o backend)
- **PostgreSQL** 12+ (banco de dados)
- **Gradle** (incluído via wrapper)

### Instalação do PostgreSQL
- **Windows**: https://www.postgresql.org/download/windows/
- **Linux**: `sudo apt install postgresql`
- **Mac**: `brew install postgresql`

---

## 📊 Fluxo de Trabalho

1. **Usuário** cria um chamado
2. **Sistema** atribui número único ao chamado
3. **Gestor** atribui o chamado a um **Técnico**
4. **Técnico** resolve e fecha o chamado
5. **Usuário** avalia a satisfação (nota 1-5)

---

## 🤝 Perfis e Permissões

| Ação | USER | TECHNICIAN | MANAGER | ADMIN |
|------|:----:|:----------:|:-------:|:-----:|
| Criar chamados | ✅ | ✅ | ✅ | ✅ |
| Ver próprios chamados | ✅ | ✅ | ✅ | ✅ |
| Ver todos chamados | ❌ | ✅ | ✅ | ✅ |
| Atribuir técnicos | ❌ | ❌ | ✅ | ✅ |
| Gerenciar equipes | ❌ | ❌ | ✅ | ✅ |
| Gerenciar usuários | ❌ | ❌ | ❌ | ✅ |
| Relatórios e analytics | ❌ | ✅ | ✅ | ✅ |

---

## 📞 Suporte

Se encontrar problemas:
1. Verifique o arquivo `SETUP-BANCO.md` para problemas com banco de dados
2. Consulte os logs do backend no terminal
3. Verifique o console do navegador para erros do frontend

---

## ⚠️ Avisos Importantes

- 🔴 **Não use as senhas padrão em produção!**
- 🔒 Configure CORS adequadamente antes de fazer deploy
- 🔑 Use variáveis de ambiente para credenciais sensíveis
- 📝 Faça backup regular do banco de dados

---

**Versão**: 1.0  
**Data**: Outubro 2025  
**Status**: ✅ Pronto para desenvolvimento

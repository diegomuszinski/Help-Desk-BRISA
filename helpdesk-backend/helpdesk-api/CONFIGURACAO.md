# 🔧 CONFIGURAÇÃO DO HELP DESK BACKEND

## 📋 Pré-requisitos

- ✅ Java 17 ou superior
- ✅ PostgreSQL 12 ou superior
- ✅ Gradle (incluído via wrapper)

---

## 🚀 Configuração Inicial

### 1️⃣ Banco de Dados PostgreSQL

#### Criar o banco de dados:

```sql
CREATE DATABASE helpdesk;
```

#### Criar as tabelas (executar na ordem):

```sql
-- Tabela de usuários
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    perfil VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de categorias
CREATE TABLE categorias (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) UNIQUE NOT NULL
);

-- Tabela de prioridades
CREATE TABLE prioridades (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(50) UNIQUE NOT NULL
);

-- Tabela de tickets (chamados)
CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    numero_chamado VARCHAR(50) UNIQUE NOT NULL,
    descricao TEXT NOT NULL,
    categoria_id BIGINT REFERENCES categorias(id),
    prioridade_id BIGINT REFERENCES prioridades(id),
    status VARCHAR(50) NOT NULL,
    solicitante_id BIGINT REFERENCES usuarios(id),
    tecnico_id BIGINT REFERENCES usuarios(id),
    solucao TEXT,
    data_abertura TIMESTAMP NOT NULL,
    data_fechamento TIMESTAMP,
    foi_reaberto BOOLEAN DEFAULT FALSE,
    unidade VARCHAR(255),
    local VARCHAR(255)
);

-- Tabela de histórico dos tickets
CREATE TABLE historico_tickets (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT REFERENCES tickets(id) ON DELETE CASCADE,
    autor_id BIGINT REFERENCES usuarios(id),
    comentario TEXT NOT NULL,
    data_ocorrencia TIMESTAMP NOT NULL
);

-- Tabela de anexos
CREATE TABLE anexos (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT REFERENCES tickets(id) ON DELETE CASCADE,
    nome_arquivo VARCHAR(500) NOT NULL,
    tipo_arquivo VARCHAR(100),
    caminho_arquivo VARCHAR(1000) NOT NULL,
    tamanho_bytes BIGINT,
    data_upload TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de refresh tokens (nova - para sistema de autenticação)
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES usuarios(id),
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    revoked BOOLEAN NOT NULL DEFAULT FALSE
);

-- Tabela de audit logs (nova - para rastreamento de ações)
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES usuarios(id),
    action VARCHAR(100) NOT NULL,
    details TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    timestamp TIMESTAMP NOT NULL,
    status VARCHAR(20)
);

-- Índices para melhor performance
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_tickets_solicitante ON tickets(solicitante_id);
CREATE INDEX idx_tickets_tecnico ON tickets(tecnico_id);
CREATE INDEX idx_historico_ticket ON historico_tickets(ticket_id);
CREATE INDEX idx_anexos_ticket ON anexos(ticket_id);
CREATE INDEX idx_refresh_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_user ON refresh_tokens(user_id);
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_ip ON audit_logs(ip_address);
```

#### Inserir dados iniciais:

```sql
-- Categorias padrão
INSERT INTO categorias (nome) VALUES 
    ('Hardware'),
    ('Software'),
    ('Rede'),
    ('Acesso'),
    ('Outros');

-- Prioridades padrão
INSERT INTO prioridades (nome) VALUES 
    ('Baixa'),
    ('Média'),
    ('Alta'),
    ('Crítica');

-- Usuário admin padrão (senha: 123456)
INSERT INTO usuarios (nome, email, senha, perfil) VALUES 
    ('Administrador', 'admin@admin.net', '$2a$10$XdwXQU0k2LCj3h1FeEq9nOHqI3YKqxDyH6iKPqxhVqXnYqN5qZqYO', 'ADMIN');
```

---

### 2️⃣ Variáveis de Ambiente

#### Copie o arquivo de exemplo:

```bash
cd helpdesk-backend/helpdesk-api
cp .env.example .env
```

#### Edite o arquivo `.env`:

```env
# Gere um JWT_SECRET seguro
JWT_SECRET=seu-secret-super-seguro-aqui-use-comando-abaixo

# Configure a senha do PostgreSQL
DB_PASSWORD=sua-senha-postgres
```

#### Gerar JWT_SECRET seguro:

**Windows PowerShell:**
```powershell
[Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(64))
```

**Linux/Mac:**
```bash
openssl rand -base64 64
```

**Ou use um gerador online:**
- https://generate-secret.vercel.app/64

---

### 3️⃣ Configurar application.properties

Edite `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/helpdesk
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# JWT
api.security.token.secret=${JWT_SECRET}

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Server
server.port=8080
```

---

### 4️⃣ Executar a Aplicação

#### Desenvolvimento:

```bash
# Compilar e executar
.\gradlew.bat bootRun

# Ou apenas compilar
.\gradlew.bat build
```

#### Produção:

```bash
# Gerar JAR
.\gradlew.bat bootJar

# Executar JAR
java -jar build/libs/helpdesk-api-0.0.1-SNAPSHOT.jar
```

---

## 🧪 Testar a API

### Login:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@admin.net","senha":"123456"}'
```

**Resposta esperada:**
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Renovar Token:

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"550e8400-e29b-41d4-a716-446655440000"}'
```

### Listar Tickets (autenticado):

```bash
curl -X GET http://localhost:8080/api/tickets \
  -H "Authorization: Bearer eyJhbGc..."
```

---

## 🔒 Recursos de Segurança Implementados

### ✅ Autenticação JWT
- Access token (curta duração)
- Refresh token (7 dias)
- Token rotation automático

### ✅ Rate Limiting
- 5 tentativas de login por minuto por IP
- Proteção contra força bruta

### ✅ Audit Logging
- Rastreamento de login/logout
- Registro de ações sensíveis
- Armazenamento de IP e user-agent

### ✅ Security Headers
- Content-Security-Policy
- X-Frame-Options
- X-Content-Type-Options
- X-XSS-Protection
- Referrer-Policy
- Permissions-Policy

### ✅ Validações
- Jakarta Validation em todos os DTOs
- Tratamento global de exceções
- Validação de tipos de arquivo

---

## 📊 Endpoints Principais

### Autenticação
- `POST /api/auth/login` - Login (retorna tokens)
- `POST /api/auth/refresh` - Renovar tokens
- `POST /api/auth/logout` - Logout (revoga refresh token)
- `POST /api/auth/logout-all` - Logout em todos os dispositivos
- `POST /api/auth/register` - Registrar usuário

### Tickets
- `GET /api/tickets` - Listar todos
- `GET /api/tickets/{id}` - Buscar por ID
- `POST /api/tickets` - Criar novo
- `POST /api/tickets/{id}/assign-self` - Atribuir a si mesmo
- `POST /api/tickets/{id}/assign/{technicianId}` - Atribuir a técnico
- `POST /api/tickets/{id}/comments` - Adicionar comentário
- `POST /api/tickets/{id}/close` - Fechar ticket
- `POST /api/tickets/{id}/reopen` - Reabrir ticket

### Dashboard
- `GET /api/dashboard/stats` - Estatísticas gerais

### Relatórios
- `GET /api/relatorios/satisfacao` - Relatório de satisfação
- `GET /api/relatorios/sla` - Relatório de SLA

---

## ⚠️ Problemas Comuns

### Erro: "JWT_SECRET não definido"
✅ **Solução**: Configure a variável de ambiente no arquivo `.env`

### Erro: "Conexão com banco recusada"
✅ **Solução**: Verifique se o PostgreSQL está rodando e as credenciais estão corretas

### Erro: "Tabela não existe"
✅ **Solução**: Execute os scripts SQL de criação de tabelas

### Erro: "Rate limit excedido"
✅ **Solução**: Aguarde 1 minuto ou reinicie a aplicação

---

## 📝 Logs e Monitoramento

### Verificar logs da aplicação:
```bash
tail -f logs/application.log
```

### Verificar audit logs no banco:
```sql
SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 50;
```

### Verificar refresh tokens ativos:
```sql
SELECT u.email, rt.token, rt.expiry_date, rt.revoked 
FROM refresh_tokens rt
JOIN usuarios u ON rt.user_id = u.id
WHERE rt.revoked = false
ORDER BY rt.created_at DESC;
```

---

## 🎯 Próximos Passos

1. ✅ Configure as variáveis de ambiente
2. ✅ Execute os scripts SQL
3. ✅ Teste o login
4. ✅ Configure o frontend para usar os tokens
5. ✅ Verifique os security headers
6. ✅ Teste o rate limiting

---

**Sistema pronto para produção com segurança enterprise! 🚀**

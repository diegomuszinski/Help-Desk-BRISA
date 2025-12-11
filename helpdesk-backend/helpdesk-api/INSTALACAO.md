# 🚀 Guia de Instalação - HelpDesk Backend

## Pré-requisitos

### 1. Instalar Java 21

**Opção A - Via Scoop (Recomendado para Windows)**
```powershell
# Instalar Scoop (se não tiver)
iwr -useb get.scoop.sh | iex

# Instalar Java 21
scoop install openjdk21
```

**Opção B - Via Eclipse Adoptium**
1. Acesse: https://adoptium.net/temurin/releases/?version=21
2. Baixe o instalador para Windows
3. Execute o instalador e siga as instruções

### 2. Verificar Instalação do Java

```powershell
java -version
```

Deve mostrar algo como:
```
openjdk version "21.0.2" 2024-01-16
```

### 3. Instalar PostgreSQL

Se ainda não tiver o PostgreSQL instalado:
```powershell
scoop install postgresql
```

Ou baixe de: https://www.postgresql.org/download/windows/

### 4. Criar o Banco de Dados

```powershell
# Conectar ao PostgreSQL
psql -U postgres

# Criar banco
CREATE DATABASE helpdesk;

# Executar script de inicialização
\i init-database.sql
```

## 🎯 Como Executar o Backend

### Opção 1 - Script Automático (Recomendado)

```powershell
cd helpdesk-backend\helpdesk-api
.\start-backend-java21.ps1
```

Este script irá:
- ✅ Detectar automaticamente o Java 21 instalado
- ✅ Configurar as variáveis de ambiente
- ✅ Iniciar o backend

### Opção 2 - Manual

```powershell
cd helpdesk-backend\helpdesk-api

# Configurar Java (apenas uma vez por sessão)
.\configurar-java.ps1

# Iniciar o backend
.\gradlew.bat bootRun
```

### Opção 3 - Usar JAR compilado

```powershell
cd helpdesk-backend\helpdesk-api

# Compilar
.\gradlew.bat build

# Executar
java -jar build\libs\helpdesk-api-0.0.1-SNAPSHOT.jar
```

## ⚙️ Configuração Personalizada

Se precisar alterar as configurações do banco ou outras variáveis, edite:

```
helpdesk-backend\helpdesk-api\src\main\resources\application.properties
```

Ou crie um arquivo `.env` com:
```properties
DB_URL=jdbc:postgresql://localhost:5432/helpdesk
DB_USERNAME=postgres
DB_PASSWORD=sua_senha
JWT_SECRET=sua_chave_secreta
```

## 🐛 Solução de Problemas

### Erro: "JAVA_HOME is set to an invalid directory"

**Solução**: Execute o script de configuração:
```powershell
.\configurar-java.ps1
```

### Erro: "Could not connect to database"

**Verifique**:
1. PostgreSQL está rodando: `Get-Service postgresql*`
2. Banco existe: `psql -U postgres -l`
3. Credenciais corretas no `application.properties`

### Porta 8080 já em uso

**Solução**: Mude a porta no `application.properties`:
```properties
server.port=8081
```

## 📝 Notas

- O backend rodará em: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console (dev): http://localhost:8080/h2-console

## 🔗 Links Úteis

- [Documentação Spring Boot](https://spring.io/projects/spring-boot)
- [Documentação PostgreSQL](https://www.postgresql.org/docs/)
- [Scoop Package Manager](https://scoop.sh/)

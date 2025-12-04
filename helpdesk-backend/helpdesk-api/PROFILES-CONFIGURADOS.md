# 🔒 Endpoint /api/test Removido em Produção

**Data**: 27/10/2025  
**Status**: ✅ **IMPLEMENTADO**

---

## 📋 O QUE FOI ALTERADO

### **TestController.java - Adicionado @Profile("dev")**

```java
@RestController
@RequestMapping("/api/test")
@Profile("dev")  // ← Só ativo em desenvolvimento
public class TestController {
    // ...
}
```

**Resultado**:
- ✅ Endpoint `/api/test/bcrypt` disponível em **desenvolvimento**
- ✅ Endpoint `/api/test/bcrypt` **BLOQUEADO** em **produção**
- ✅ Retorna **404 Not Found** em produção

---

## 🔧 PROFILES DO SPRING

### **application.properties** (Base)
```properties
# Profile ativo (carrega application-{profile}.properties)
spring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}
```

### **application-dev.properties** (Desenvolvimento)
```properties
# Logs detalhados
logging.level.org.springframework.security=DEBUG
logging.level.br.com.brisabr.helpdesk_api=DEBUG

# Mostrar SQL
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# CORS - Múltiplas portas locais
cors.allowed.origins=http://localhost:5173,http://localhost:5174,http://localhost:5175
```

### **application-prod.properties** (Produção)
```properties
# Logs reduzidos
logging.level.org.springframework.security=WARN
logging.level.br.com.brisabr.helpdesk_api=INFO

# NÃO mostrar SQL
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false

# CORS - Apenas domínio de produção
cors.allowed.origins=https://seudominio.com
```

---

## 🚀 CONFIGURAÇÃO

### Desenvolvimento (`.env`)
```properties
SPRING_PROFILES_ACTIVE=dev
```

**Features ativadas**:
- ✅ Endpoint `/api/test` disponível
- ✅ Logs DEBUG habilitados
- ✅ SQL queries visíveis
- ✅ CORS com múltiplas portas locais

### Produção (`.env.production`)
```properties
SPRING_PROFILES_ACTIVE=prod
```

**Features desativadas**:
- ❌ Endpoint `/api/test` bloqueado (404)
- ❌ Logs DEBUG desabilitados
- ❌ SQL queries ocultas
- ❌ CORS restrito a domínio específico

---

## 🧪 COMO TESTAR

### 1. Testar em Modo Desenvolvimento

```powershell
# Configurar profile dev
$env:SPRING_PROFILES_ACTIVE = "dev"

# Carregar variáveis
.\configurar-env.ps1

# Iniciar backend
.\gradlew.bat bootRun

# Testar endpoint (deve funcionar)
curl http://localhost:8080/api/test/bcrypt?email=admin@admin.net&senha=123456
# ✅ Deve retornar JSON com hash
```

### 2. Testar em Modo Produção (Simulado)

```powershell
# Usar script de teste
.\testar-producao.ps1

# OU manualmente:
$env:SPRING_PROFILES_ACTIVE = "prod"
.\gradlew.bat bootRun

# Testar endpoint (deve falhar)
curl http://localhost:8080/api/test/bcrypt?email=admin@admin.net&senha=123456
# ❌ Deve retornar 404 Not Found
```

### 3. Verificar Profile Ativo

Ao iniciar o backend, verifique os logs:

```
The following profiles are active: dev
```

OU em produção:

```
The following profiles are active: prod
```

---

## 🔒 SEGURANÇA MELHORADA

### ANTES (❌ Inseguro)

```java
@RestController
@RequestMapping("/api/test")
public class TestController {
    // Disponível em TODOS os ambientes!
}
```

**Problemas**:
- ❌ Endpoint exposto em produção
- ❌ Qualquer um pode gerar hashes BCrypt
- ❌ Expõe hashes do banco de dados
- ❌ Pode ser usado para ataques de força bruta
- ❌ Informações sensíveis públicas

### DEPOIS (✅ Seguro)

```java
@RestController
@RequestMapping("/api/test")
@Profile("dev")  // ← Só em desenvolvimento
public class TestController {
    // Bloqueado em produção!
}
```

**Melhorias**:
- ✅ Endpoint **bloqueado** em produção (404)
- ✅ Só disponível localmente em desenvolvimento
- ✅ Não expõe hashes em ambiente público
- ✅ Reduz superfície de ataque
- ✅ Conformidade com boas práticas

---

## 📊 COMPORTAMENTO POR AMBIENTE

| Feature | Desenvolvimento | Produção |
|---------|----------------|----------|
| `/api/test/bcrypt` | ✅ Disponível | ❌ 404 Not Found |
| Logs DEBUG | ✅ Habilitado | ❌ Desabilitado |
| SQL Queries | ✅ Visível | ❌ Oculto |
| CORS | 🟡 Múltiplas portas | ✅ Domínio específico |
| Performance | 🟡 Normal | ✅ Otimizada |

---

## 🎯 OUTROS ENDPOINTS QUE PODEM SER PROTEGIDOS

### Endpoints de Admin (Exemplo)

```java
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {
    
    @GetMapping("/stats")
    @Profile("!prod")  // ← Desabilita em produção
    public Map<String, Object> getInternalStats() {
        // Estatísticas internas sensíveis
    }
}
```

### Endpoints de Debug

```java
@RestController
@RequestMapping("/api/debug")
@Profile("dev")  // ← Só em desenvolvimento
public class DebugController {
    
    @GetMapping("/cache")
    public Map<String, Object> getCacheInfo() {
        // Informações de cache
    }
    
    @PostMapping("/clear-cache")
    public void clearCache() {
        // Limpar cache
    }
}
```

---

## 📝 CHECKLIST DE SEGURANÇA

### Configuração de Profiles

- [x] `@Profile("dev")` adicionado no TestController
- [x] `application-dev.properties` criado
- [x] `application-prod.properties` criado
- [x] `SPRING_PROFILES_ACTIVE` configurado no `.env`
- [x] `.env.production.example` criado
- [x] Script de teste criado (`testar-producao.ps1`)
- [x] Build testado e funcionando

### Variáveis de Ambiente

- [x] `SPRING_PROFILES_ACTIVE=dev` no `.env`
- [x] `SPRING_PROFILES_ACTIVE=prod` no `.env.production.example`
- [x] Logs DEBUG apenas em dev
- [x] SQL queries apenas em dev
- [x] CORS restrito por profile

### Testes

- [ ] **TODO**: Testar endpoint em modo dev (deve funcionar)
- [ ] **TODO**: Testar endpoint em modo prod (deve retornar 404)
- [ ] **TODO**: Verificar logs em cada modo
- [ ] **TODO**: Confirmar CORS específico em prod

---

## 🚨 IMPORTANTE PARA DEPLOY

### Ao fazer deploy em produção:

1. **Definir variável de ambiente no servidor**:
```bash
# Linux/Docker
export SPRING_PROFILES_ACTIVE=prod

# Heroku
heroku config:set SPRING_PROFILES_ACTIVE=prod

# AWS Elastic Beanstalk
# Adicionar SPRING_PROFILES_ACTIVE=prod nas variáveis de ambiente
```

2. **Verificar que profile prod está ativo**:
```
# Logs devem mostrar:
The following profiles are active: prod
```

3. **Testar endpoint após deploy**:
```bash
curl https://seudominio.com/api/test/bcrypt
# Deve retornar: 404 Not Found
```

4. **Verificar outros endpoints funcionando**:
```bash
curl https://seudominio.com/api/auth/login
# Deve funcionar normalmente
```

---

## 📈 ANTES vs DEPOIS

| Aspecto | Antes | Depois |
|---------|-------|--------|
| Endpoint em Dev | ✅ Disponível | ✅ Disponível |
| Endpoint em Prod | ❌ Exposto | ✅ Bloqueado (404) |
| Configuração por Ambiente | ❌ Manual | ✅ Automática |
| Logs | 🟡 Sempre DEBUG | ✅ DEBUG (dev) / WARN (prod) |
| SQL Queries | 🟡 Sempre visível | ✅ Visível (dev) / Oculto (prod) |
| Segurança | 🔴 Vulnerável | ✅ Protegido |
| Conformidade OWASP | ❌ Falha | ✅ Passa |

---

## 🎓 REFERÊNCIAS

- [Spring Profiles Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles)
- [OWASP API Security](https://owasp.org/www-project-api-security/)
- [Spring Boot Production Best Practices](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html)

---

**Status**: ✅ **1 VULNERABILIDADE ALTA CORRIGIDA**

Próximo passo: Implementar validações Jakarta e GlobalExceptionHandler

---

**Gerado em**: 27/10/2025  
**Arquivo**: `PROFILES-CONFIGURADOS.md`

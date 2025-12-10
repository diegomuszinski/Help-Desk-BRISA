# Health Checks - Sistema HelpDesk

## 📊 Visão Geral

Sistema completo de monitoramento de saúde da aplicação usando **Spring Boot Actuator** com health indicators customizados.

## 🎯 Health Indicators Implementados

### 1. **Database Health Indicator**
Monitora a saúde do banco de dados PostgreSQL.

**Verificações:**
- ✅ Conectividade com o banco de dados
- ✅ Tempo de resposta de queries
- ✅ Número de conexões ativas
- ✅ Performance geral

**Thresholds:**
- ⚠️ WARNING: Query > 500ms
- ❌ DOWN: Query > 1000ms ou erro de conexão

**Detalhes reportados:**
```json
{
  "database": "PostgreSQL",
  "responseTime": "45ms",
  "activeConnections": 3,
  "status": "Conectado e responsivo"
}
```

---

### 2. **Disk Space Health Indicator**
Monitora o espaço em disco disponível para uploads.

**Verificações:**
- ✅ Espaço livre no diretório de uploads
- ✅ Percentual de uso do disco
- ✅ Espaço utilizável pelo sistema

**Thresholds:**
- ⚠️ WARNING: Espaço livre < 5GB
- ❌ DOWN: Espaço livre < 1GB

**Detalhes reportados:**
```json
{
  "status": "Espaço em disco adequado",
  "path": "C:\\Users\\...\\uploads",
  "freeSpace": "125.43 GB",
  "totalSpace": "465.76 GB",
  "usableSpace": "125.43 GB",
  "freePercentage": "26.93%"
}
```

---

### 3. **Cache Health Indicator**
Monitora o cache Caffeine da aplicação.

**Verificações:**
- ✅ Status dos caches (categorias, prioridades)
- ✅ Taxa de acerto (hit rate)
- ✅ Número de entradas em cache
- ✅ Estatísticas de hits/misses

**Thresholds:**
- ⚠️ WARNING: Hit rate < 50%
- ✅ OK: Hit rate 50-70%
- 🌟 EXCELENTE: Hit rate > 70%

**Detalhes reportados:**
```json
{
  "status": "Excelente - Hit rate alto (>70%)",
  "totalCaches": 2,
  "totalEntries": 45,
  "averageHitRate": "87.34%",
  "categorias": "Tamanho: 20 | Hit Rate: 85.50% | Hits: 850 | Misses: 145",
  "prioridades": "Tamanho: 25 | Hit Rate: 89.18% | Hits: 1078 | Misses: 131"
}
```

---

## 🔧 Configuração

### application.properties

```properties
# Spring Boot Actuator - Health Checks
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
management.endpoint.health.probes.enabled=true
management.health.defaults.enabled=true

# Informações da aplicação
management.info.env.enabled=true
info.app.name=HelpDesk API
info.app.description=Sistema de gerenciamento de chamados
info.app.version=1.0.0
info.app.java.version=${java.version}
```

### SecurityConfig.java

Os endpoints do Actuator estão configurados como públicos para permitir monitoramento:

```java
.requestMatchers("/actuator/health/**").permitAll()
.requestMatchers("/actuator/info").permitAll()
```

---

## 📡 Endpoints Disponíveis

### 1. Health Check Geral
```bash
GET http://localhost:8080/actuator/health
```

**Resposta (sem autenticação):**
```json
{
  "status": "UP"
}
```

**Resposta (autenticado):**
```json
{
  "status": "UP",
  "components": {
    "cacheHealthIndicator": {
      "status": "UP",
      "details": { ... }
    },
    "databaseHealthIndicator": {
      "status": "UP",
      "details": { ... }
    },
    "diskSpaceHealthIndicator": {
      "status": "UP",
      "details": { ... }
    },
    "db": {
      "status": "UP",
      "details": { ... }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

---

### 2. Health Check Individual

**Database:**
```bash
GET http://localhost:8080/actuator/health/database
```

**Cache:**
```bash
GET http://localhost:8080/actuator/health/cache
```

**Disk Space:**
```bash
GET http://localhost:8080/actuator/health/diskSpace
```

---

### 3. Informações da Aplicação
```bash
GET http://localhost:8080/actuator/info
```

**Resposta:**
```json
{
  "app": {
    "name": "HelpDesk API",
    "description": "Sistema de gerenciamento de chamados",
    "version": "1.0.0",
    "java": {
      "version": "21.0.2"
    }
  }
}
```

---

## 🧪 Testando os Health Checks

### PowerShell (Windows)

```powershell
# Health check geral
Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method Get

# Health check com detalhes (autenticado)
$headers = @{
    "Authorization" = "Bearer YOUR_JWT_TOKEN"
}
Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Headers $headers -Method Get

# Info da aplicação
Invoke-RestMethod -Uri "http://localhost:8080/actuator/info" -Method Get
```

### cURL (Linux/Mac)

```bash
# Health check geral
curl http://localhost:8080/actuator/health

# Health check com detalhes (autenticado)
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8080/actuator/health

# Info da aplicação
curl http://localhost:8080/actuator/info
```

---

## 🚨 Monitoramento e Alertas

### Status Possíveis

| Status | Descrição | Ação |
|--------|-----------|------|
| 🟢 **UP** | Sistema operacional | Nenhuma ação necessária |
| 🟡 **WARNING** | Performance degradada | Investigar logs e métricas |
| 🔴 **DOWN** | Sistema com falha | Ação imediata necessária |
| ⚪ **UNKNOWN** | Status indeterminado | Verificar configuração |

### Cenários de Alerta

#### ⚠️ Database Slow Response
```json
{
  "status": "UP",
  "details": {
    "status": "WARNING - Resposta lenta",
    "responseTime": "750ms"
  }
}
```
**Ação:** Verificar índices do banco de dados, queries lentas

#### ❌ Database Connection Failed
```json
{
  "status": "DOWN",
  "details": {
    "error": "Connection refused"
  }
}
```
**Ação:** Verificar se PostgreSQL está rodando, credenciais corretas

#### ⚠️ Low Disk Space
```json
{
  "status": "UP",
  "details": {
    "status": "WARNING - Espaço em disco baixo",
    "freeSpace": "3.50 GB"
  }
}
```
**Ação:** Limpar arquivos antigos, expandir disco

#### 🔴 Critical Disk Space
```json
{
  "status": "DOWN",
  "details": {
    "status": "CRÍTICO - Espaço em disco insuficiente",
    "freeSpace": "0.85 GB"
  }
}
```
**Ação:** Ação imediata - limpar espaço ou parar uploads

#### ⚠️ Low Cache Hit Rate
```json
{
  "status": "UP",
  "details": {
    "status": "WARNING - Hit rate baixo (<50%)",
    "averageHitRate": "42.15%"
  }
}
```
**Ação:** Revisar configuração do cache, aumentar tamanho

---

## 🔗 Integração com Ferramentas de Monitoramento

### Prometheus

Adicione ao `build.gradle`:
```gradle
implementation 'io.micrometer:micrometer-registry-prometheus'
```

Endpoint de métricas:
```
http://localhost:8080/actuator/prometheus
```

### Grafana

Importe dashboards pré-configurados para Spring Boot:
- Dashboard ID: 6756 (Spring Boot Statistics)
- Dashboard ID: 12900 (Spring Boot Observability)

### Docker Health Check

```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
```

### Kubernetes Probes

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 20
  periodSeconds: 5
```

---

## 📊 Métricas Adicionais

### Endpoints de Métricas

```bash
# Todas as métricas
GET http://localhost:8080/actuator/metrics

# Métrica específica
GET http://localhost:8080/actuator/metrics/jvm.memory.used
GET http://localhost:8080/actuator/metrics/http.server.requests
GET http://localhost:8080/actuator/metrics/hikaricp.connections.active
```

---

## 🎯 Benefícios

1. **Observabilidade Total**: Visibilidade completa do estado da aplicação
2. **Detecção Proativa**: Identificação de problemas antes que afetem usuários
3. **Troubleshooting Rápido**: Diagnóstico facilitado de problemas
4. **Alta Disponibilidade**: Monitoramento contínuo garante uptime
5. **Performance**: Métricas detalhadas de cache, database e recursos

---

## 📚 Referências

- [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Health Indicators](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.endpoints.health)
- [Custom Health Indicators](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.endpoints.health.writing-custom-health-indicators)

---

## 🏗️ Arquitetura

```
┌─────────────────────┐
│   Load Balancer     │
│    (Monitoring)     │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│   /actuator/health  │
│    (Public)         │
└──────────┬──────────┘
           │
           ├─── DatabaseHealthIndicator ───► PostgreSQL
           │
           ├─── DiskSpaceHealthIndicator ───► File System
           │
           └─── CacheHealthIndicator ───► Caffeine Cache
```

---

**✅ Health Checks implementados com sucesso!**

Sistema pronto para produção com monitoramento completo de saúde.

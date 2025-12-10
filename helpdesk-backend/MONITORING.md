# HelpDesk Monitoring Stack

Este diretório contém a configuração do stack de monitoramento para o HelpDesk API.

## Stack de Monitoramento

### Zipkin (Distributed Tracing)
- **Porta**: 9411
- **URL**: http://localhost:9411
- **Função**: Visualização de traces distribuídos e latência de requisições

### Prometheus (Metrics Collection)
- **Porta**: 9090
- **URL**: http://localhost:9090
- **Função**: Coleta e armazenamento de métricas da aplicação

### Grafana (Metrics Visualization)
- **Porta**: 3000
- **URL**: http://localhost:3000
- **Credenciais padrão**: admin/admin
- **Função**: Dashboards e visualização de métricas

## Como Usar

### 1. Iniciar o Stack de Monitoramento

```powershell
# No diretório helpdesk-backend
docker-compose -f docker-compose-monitoring.yml up -d
```

### 2. Verificar Status

```powershell
docker-compose -f docker-compose-monitoring.yml ps
```

### 3. Acessar as Interfaces

- **Zipkin**: http://localhost:9411
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000

### 4. Configurar Grafana

1. Acesse http://localhost:3000
2. Login: admin/admin
3. Adicione Prometheus como datasource:
   - URL: http://prometheus:9090
   - Access: Server (default)
4. Importe dashboards:
   - Spring Boot Dashboard (ID: 6756)
   - JVM Dashboard (ID: 4701)
   - Micrometer Dashboard (ID: 11378)

### 5. Visualizar Traces no Zipkin

1. Acesse http://localhost:9411
2. Clique em "Run Query" para ver os traces
3. Filtre por:
   - Service Name: helpdesk-api
   - Span Name: http requests
   - Min/Max Duration

## Configuração da Aplicação

A aplicação já está configurada para enviar traces e métricas automaticamente:

### Tracing (Zipkin)
```properties
management.tracing.sampling.probability=1.0  # 100% das requisições (dev)
management.zipkin.tracing.endpoint=http://localhost:9411/api/v2/spans
```

### Métricas (Prometheus)
```properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.prometheus.enabled=true
```

## Variáveis de Ambiente

Configure estas variáveis para personalizar o comportamento:

```properties
# Zipkin
ZIPKIN_ENDPOINT=http://localhost:9411/api/v2/spans
TRACING_ENABLED=true
TRACING_SAMPLING_PROBABILITY=1.0  # 1.0 = 100%, 0.1 = 10%

# Prometheus
PROMETHEUS_ENABLED=true
```

## Produção

Para produção, ajuste:

1. **Sampling Rate**: Reduza para 0.1 (10%) para diminuir overhead
2. **Storage**: Configure storage persistente para Zipkin
3. **Segurança**: Adicione autenticação e HTTPS
4. **Alerting**: Configure alertas no Prometheus

```properties
# application-prod.properties
management.tracing.sampling.probability=0.1
management.zipkin.tracing.endpoint=https://zipkin.prod.example.com/api/v2/spans
```

## Comandos Úteis

```powershell
# Iniciar stack
docker-compose -f docker-compose-monitoring.yml up -d

# Parar stack
docker-compose -f docker-compose-monitoring.yml down

# Ver logs
docker-compose -f docker-compose-monitoring.yml logs -f

# Remover volumes (reset)
docker-compose -f docker-compose-monitoring.yml down -v

# Verificar health
docker-compose -f docker-compose-monitoring.yml ps
```

## Métricas Disponíveis

### JVM Metrics
- `jvm.memory.used`
- `jvm.threads.live`
- `jvm.gc.pause`

### HTTP Metrics
- `http.server.requests` (count, duration)
- `http.server.requests.active`

### Database Metrics
- `hikaricp.connections.active`
- `hikaricp.connections.idle`
- `hikaricp.connections.pending`

### Business Metrics
- `helpdesk.tickets.created`
- `helpdesk.tickets.closed`
- `helpdesk.sla.compliance`

## Troubleshooting

### Zipkin não recebe traces
1. Verifique se o container está rodando: `docker ps`
2. Verifique a conectividade: `curl http://localhost:9411/health`
3. Verifique os logs: `docker logs helpdesk-zipkin`

### Prometheus não scrape métricas
1. Verifique o endpoint: `curl http://localhost:8080/actuator/prometheus`
2. Verifique o arquivo `prometheus.yml`
3. Verifique targets no Prometheus: http://localhost:9090/targets

### Grafana não conecta ao Prometheus
1. Use `http://prometheus:9090` (não localhost)
2. Verifique a rede Docker: `docker network inspect helpdesk-network`

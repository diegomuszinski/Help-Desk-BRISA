# API Versioning Guide

## Versionamento da API

A API do HelpDesk utiliza versionamento por prefixo de URL.

### Versão Atual: v1

**Base URL**: `/v1/api`

Todas as rotas da API agora incluem o prefixo `/v1`:

```
Antes: /api/tickets
Depois: /v1/api/tickets
```

## Endpoints Versionados

### Autenticação
- `POST /v1/api/auth/login` - Login
- `POST /v1/api/auth/logout` - Logout (futuro)
- `POST /v1/api/auth/refresh` - Refresh token (futuro)

### Usuários
- `GET /v1/api/users` - Listar usuários
- `POST /v1/api/users` - Criar usuário
- `GET /v1/api/users/{id}` - Buscar usuário
- `PUT /v1/api/users/{id}` - Atualizar usuário
- `DELETE /v1/api/users/{id}` - Deletar usuário

### Tickets
- `GET /v1/api/tickets` - Listar tickets
- `POST /v1/api/tickets` - Criar ticket
- `GET /v1/api/tickets/{id}` - Buscar ticket
- `PUT /v1/api/tickets/{id}` - Atualizar ticket
- `DELETE /v1/api/tickets/{id}` - Deletar ticket
- `POST /v1/api/tickets/{id}/comments` - Adicionar comentário

### Categorias
- `GET /v1/api/categorias` - Listar categorias
- `POST /v1/api/categorias` - Criar categoria

### Prioridades
- `GET /v1/api/prioridades` - Listar prioridades
- `POST /v1/api/prioridades` - Criar prioridade

### Anexos
- `GET /v1/api/anexos/{id}` - Buscar anexo
- `POST /v1/api/anexos` - Upload de anexo

### Métricas
- `GET /v1/api/metrics` - Métricas de negócio

### Dashboard
- `GET /v1/api/dashboard` - Dados do dashboard

### Relatórios
- `GET /v1/api/reports` - Gerar relatórios

## Endpoints Não Versionados

Alguns endpoints não são versionados por serem independentes da lógica de negócio:

### Actuator (Monitoramento)
- `/actuator/health` - Health check
- `/actuator/info` - Informações da aplicação
- `/actuator/metrics` - Métricas técnicas
- `/actuator/prometheus` - Métricas para Prometheus

### Swagger/OpenAPI
- `/swagger-ui/index.html` - Interface Swagger
- `/v3/api-docs` - Documentação OpenAPI

## Como Funciona

### Configuração Automática

O versionamento é aplicado automaticamente via `ApiVersionConfig`:

```java
@Configuration
public class ApiVersionConfig implements WebMvcConfigurer {
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/v1", c -> 
            c.getPackageName().startsWith("br.com.brisabr.helpdesk_api")
        );
    }
}
```

### Controllers

Os controllers mantêm suas rotas originais:

```java
@RestController
@RequestMapping("/api/tickets")  // Sem /v1
public class TicketController {
    // Será acessível em /v1/api/tickets
}
```

## Migração de Clientes

### Frontend

Atualize a base URL no arquivo de configuração da API:

```typescript
// Antes
const API_BASE_URL = 'http://localhost:8080/api';

// Depois
const API_BASE_URL = 'http://localhost:8080/v1/api';
```

### Exemplo Vue 3 (services/api.ts):

```typescript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/v1/api',  // Adicione /v1
  headers: {
    'Content-Type': 'application/json',
  },
});
```

## Suporte a Múltiplas Versões (Futuro)

Para suportar v1 e v2 simultaneamente:

1. **Criar controllers específicos por versão**:
```java
// v1
@RestController
@RequestMapping("/v1/api/tickets")
public class TicketControllerV1 { }

// v2
@RestController
@RequestMapping("/v2/api/tickets")
public class TicketControllerV2 { }
```

2. **Ou usar conditional beans**:
```java
@Bean
@ConditionalOnProperty(name = "api.version", havingValue = "v1")
public TicketService ticketServiceV1() { }
```

3. **Deprecação de versões antigas**:
```java
@Deprecated(since = "2.0.0", forRemoval = true)
@RestController
@RequestMapping("/v1/api/tickets")
public class TicketControllerV1 { }
```

## Versionamento de Breaking Changes

### Quando criar uma nova versão:

1. **Mudanças incompatíveis no contrato da API**:
   - Remoção de campos obrigatórios
   - Mudança no tipo de dados
   - Remoção de endpoints

2. **Mudanças que NÃO requerem nova versão**:
   - Adição de novos campos opcionais
   - Adição de novos endpoints
   - Mudanças internas que não afetam o contrato

### Processo de criação de v2:

1. Copiar controllers de v1 para v2
2. Atualizar `@RequestMapping("/v2/api/...")`
3. Manter v1 ativo por período de transição
4. Adicionar header `X-API-Version-Deprecated: v1 will be removed on 2025-12-31`
5. Documentar no changelog

## Monitoramento

Use Micrometer para rastrear uso por versão:

```java
@Component
public class ApiVersionMetrics {
    private final MeterRegistry registry;
    
    public void recordApiCall(String version, String endpoint) {
        registry.counter("api.calls", 
            "version", version, 
            "endpoint", endpoint
        ).increment();
    }
}
```

## Testes

Atualize os testes para usar URLs versionadas:

```java
@Test
void shouldCreateTicket() {
    mockMvc.perform(post("/v1/api/tickets")  // Adicione /v1
        .contentType(MediaType.APPLICATION_JSON)
        .content(ticketJson))
        .andExpect(status().isCreated());
}
```

## Documentação Swagger

O Swagger é atualizado automaticamente para refletir as URLs versionadas.

Acesse: http://localhost:8080/swagger-ui/index.html

## Referências

- [REST API Versioning Best Practices](https://restfulapi.net/versioning/)
- [Spring Boot Path Matching](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-config-path-matching)
- [Semantic Versioning](https://semver.org/)

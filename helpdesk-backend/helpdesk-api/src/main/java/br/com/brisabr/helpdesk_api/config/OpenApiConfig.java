package br.com.brisabr.helpdesk_api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração do Swagger/OpenAPI para documentação automática da API.
 * 
 * Acesse a documentação em:
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Help Desk API")
                        .version("1.0.0")
                        .description("API REST para sistema de Help Desk com gerenciamento de tickets, usuários e relatórios.\n\n" +
                                "### Recursos principais:\n" +
                                "- **Autenticação JWT** com refresh tokens\n" +
                                "- **Gerenciamento de Tickets** (CRUD completo)\n" +
                                "- **Sistema de Usuários** com perfis (ADMIN, MANAGER, TECHNICIAN, USER)\n" +
                                "- **Dashboard** com estatísticas e KPIs\n" +
                                "- **Relatórios** de satisfação e SLA\n" +
                                "- **Anexos** em tickets\n" +
                                "- **Histórico** de interações\n" +
                                "- **Rate Limiting** para segurança\n" +
                                "- **Audit Logs** para rastreabilidade\n\n" +
                                "### Como usar:\n" +
                                "1. Faça login em `/api/auth/login` para obter o token JWT\n" +
                                "2. Use o botão 'Authorize' acima e cole o token\n" +
                                "3. Teste os endpoints disponíveis\n\n" +
                                "### Credenciais de teste:\n" +
                                "- **Admin**: admin@admin.net / 123456\n" +
                                "- **Manager**: sonia.lima@gestor.net / 1234546\n" +
                                "- **Técnico**: mariana@tecnico.net / 123456\n" +
                                "- **Usuário**: usuario@teste.net / 123456")
                        .contact(new Contact()
                                .name("Help Desk Team")
                                .email("suporte@helpdesk.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de Desenvolvimento"),
                        new Server()
                                .url("https://api.helpdesk.com")
                                .description("Servidor de Produção")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Insira o token JWT obtido no endpoint /api/auth/login")));
    }
}

package br.com.helpdesk.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração de versionamento da API.
 * Adiciona prefixo /v1 a todos os controllers da API.
 *
 * Resultado: /api/tickets -> /v1/api/tickets
 */
@Configuration
public class ApiVersionConfig implements WebMvcConfigurer {

    public static final String API_V1_PREFIX = "/v1";

    @Override
    public void configurePathMatch(@NonNull PathMatchConfigurer configurer) {
        // Aplica versionamento a todos os controllers nos pacotes principais
        configurer.addPathPrefix(API_V1_PREFIX, c ->
            c.getPackageName().startsWith("br.com.brisabr.helpdesk_api.controller") ||
            c.getPackageName().startsWith("br.com.brisabr.helpdesk_api.auth") ||
            c.getPackageName().startsWith("br.com.brisabr.helpdesk_api.user") ||
            c.getPackageName().startsWith("br.com.brisabr.helpdesk_api.ticket") ||
            c.getPackageName().startsWith("br.com.brisabr.helpdesk_api.metrics")
        );
    }
}

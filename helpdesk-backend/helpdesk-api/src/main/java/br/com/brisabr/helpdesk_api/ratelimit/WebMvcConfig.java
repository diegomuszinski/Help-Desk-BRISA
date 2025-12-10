package br.com.brisabr.helpdesk_api.ratelimit;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração MVC para registrar o interceptor de rate limiting.
 * Aplica o interceptor em todas as rotas da aplicação.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    public WebMvcConfig(RateLimitInterceptor rateLimitInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/v1/api/**") // Aplica rate limiting em todas as rotas versionadas
                .excludePathPatterns(
                        "/actuator/**",      // Exclui endpoints de monitoramento
                        "/swagger-ui/**",    // Exclui Swagger UI
                        "/v3/api-docs/**"    // Exclui OpenAPI docs
                );
    }
}

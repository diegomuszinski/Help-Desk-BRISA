package br.com.brisabr.helpdesk_api.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;

@Configuration
@SuppressWarnings("null")
public class WebConfig implements WebMvcConfigurer {

    @Value("${cors.allowed.origins:http://localhost:5173}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * Filtro para adicionar headers de segurança a todas as respostas.
     * Protege contra XSS, Clickjacking, MIME sniffing, etc.
     */
    @Bean
    public Filter securityHeadersFilter() {
        return new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {

                HttpServletResponse httpResponse = (HttpServletResponse) response;

                // Content-Security-Policy: Previne XSS e injeção de código
                httpResponse.setHeader("Content-Security-Policy",
                    "default-src 'self'; " +
                    "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                    "style-src 'self' 'unsafe-inline'; " +
                    "img-src 'self' data: https:; " +
                    "font-src 'self' data:; " +
                    "connect-src 'self'; " +
                    "frame-ancestors 'none'");

                // X-Frame-Options: Previne Clickjacking
                httpResponse.setHeader("X-Frame-Options", "DENY");

                // X-Content-Type-Options: Previne MIME sniffing
                httpResponse.setHeader("X-Content-Type-Options", "nosniff");

                // X-XSS-Protection: Ativa proteção XSS do navegador
                httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

                // Strict-Transport-Security: Força HTTPS (apenas em produção)
                // httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

                // Referrer-Policy: Controla informações de referrer
                httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

                // Permissions-Policy: Controla features do navegador
                httpResponse.setHeader("Permissions-Policy",
                    "geolocation=(), microphone=(), camera=()");

                chain.doFilter(request, response);
            }
        };
    }
}

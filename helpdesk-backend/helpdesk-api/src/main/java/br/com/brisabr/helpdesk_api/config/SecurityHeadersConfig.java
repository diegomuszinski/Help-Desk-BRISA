package br.com.brisabr.helpdesk_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

/**
 * Configuração de cabeçalhos de segurança para produção.
 * Implementa defesas contra XSS, clickjacking, MIME sniffing, etc.
 */
@Configuration
@EnableWebSecurity
@Order(1)
public class SecurityHeadersConfig {

    @Bean
    public SecurityFilterChain securityHeadersFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/**")
            .headers(headers -> headers
                // Content Security Policy - Defesa contra XSS
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.jsdelivr.net; " +
                        "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                        "font-src 'self' https://fonts.gstatic.com; " +
                        "img-src 'self' data: https:; " +
                        "connect-src 'self' http://localhost:* https://api.github.com; " +
                        "frame-ancestors 'none'; " +
                        "base-uri 'self'; " +
                        "form-action 'self'; " +
                        "upgrade-insecure-requests"
                    )
                )

                // X-Frame-Options - Defesa contra clickjacking
                .frameOptions(frame -> frame
                    .deny()
                )

                // X-Content-Type-Options - Previne MIME sniffing
                .contentTypeOptions(contentType -> {})

                // X-XSS-Protection - Proteção XSS adicional (browsers antigos)
                .xssProtection(xss -> xss
                    .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                )

                // HSTS - Force HTTPS
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000) // 1 ano
                    .preload(true)
                )

                // Referrer Policy - Controla informações de referência
                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )

                // Permissions Policy (Feature Policy) - Controla recursos do navegador
                .permissionsPolicy(permissions -> permissions
                    .policy("geolocation=(), microphone=(), camera=(), payment=()")
                )
            );

        return http.build();
    }
}

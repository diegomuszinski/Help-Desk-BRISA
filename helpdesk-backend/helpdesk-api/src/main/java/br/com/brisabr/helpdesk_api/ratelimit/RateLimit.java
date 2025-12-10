package br.com.brisabr.helpdesk_api.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação para aplicar rate limiting em endpoints REST.
 * Usa o algoritmo Token Bucket via Bucket4j.
 *
 * Exemplo de uso:
 * <pre>
 * &#64;RateLimit(requestsPerMinute = 10)
 * &#64;PostMapping("/api/tickets")
 * public ResponseEntity<?> createTicket(...) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * Número máximo de requisições permitidas por minuto para o endpoint.
     * Padrão: 60 requisições/minuto (1 por segundo)
     */
    int requestsPerMinute() default 60;

    /**
     * Tipo de limite a ser aplicado.
     * - PER_IP: Limite por endereço IP (padrão)
     * - PER_USER: Limite por usuário autenticado
     * - GLOBAL: Limite global para o endpoint
     */
    LimitType type() default LimitType.PER_IP;

    /**
     * Mensagem personalizada quando o limite é excedido.
     */
    String message() default "Taxa de requisições excedida. Tente novamente em alguns instantes.";

    enum LimitType {
        PER_IP,      // Limita por endereço IP
        PER_USER,    // Limita por ID do usuário autenticado
        GLOBAL       // Limita globalmente o endpoint
    }
}

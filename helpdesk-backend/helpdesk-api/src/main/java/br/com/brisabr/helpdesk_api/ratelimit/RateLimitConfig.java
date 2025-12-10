package br.com.brisabr.helpdesk_api.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuração do sistema de rate limiting usando Bucket4j + Caffeine.
 * Implementa o algoritmo Token Bucket para controle de taxa de requisições.
 */
@Configuration
public class RateLimitConfig {

    /**
     * Cache Caffeine para armazenar os buckets de rate limiting.
     * Expira após 10 minutos de inatividade para liberar memória.
     */
    @Bean
    public Cache<String, Bucket> rateLimitCache() {
        return Caffeine.newBuilder()
                .maximumSize(10000) // Máximo de 10.000 chaves em cache
                .expireAfterAccess(Duration.ofMinutes(10))
                .recordStats() // Habilita estatísticas
                .build();
    }

    /**
     * Cria um bucket padrão com limite configurável.
     *
     * @param requestsPerMinute Número de requisições permitidas por minuto
     * @return Bucket configurado
     */
    public Bucket createBucket(int requestsPerMinute) {
        // Bandwidth: capacidade máxima = requestsPerMinute com refill intervalar
        Bandwidth limit = Bandwidth.builder()
                .capacity(requestsPerMinute)
                .refillIntervally(requestsPerMinute, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}

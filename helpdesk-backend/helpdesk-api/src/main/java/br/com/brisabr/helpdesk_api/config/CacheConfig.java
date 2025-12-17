package br.com.brisabr.helpdesk_api.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuração de cache para otimizar consultas frequentes.
 *
 * Utiliza Caffeine como implementação de cache em memória.
 * Categorias e prioridades são cacheadas pois raramente mudam.
 *
 * @author HelpDesk Team
 */
@Configuration
@EnableCaching
@SuppressWarnings("null")
public class CacheConfig {

    /**
     * Configura o gerenciador de cache com Caffeine.
     *
     * Cache configurado com:
     * - Expiração após 1 hora sem acesso
     * - Máximo de 1000 entradas
     * - Registro de estatísticas
     *
     * @return CacheManager configurado
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "categorias",
                "prioridades",
                "businessMetrics"  // Cache para métricas de negócio
        );

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(1000)
                .recordStats());

        // Configuração específica para businessMetrics (TTL menor)
        cacheManager.registerCustomCache("businessMetrics",
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)  // 5 minutos
                        .maximumSize(10)
                        .recordStats()
                        .build());

        return cacheManager;
    }
}

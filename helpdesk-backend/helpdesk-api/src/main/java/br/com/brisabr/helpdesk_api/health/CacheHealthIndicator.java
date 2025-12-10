package br.com.brisabr.helpdesk_api.health;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Component;

/**
 * Health indicator para monitorar o status e estatísticas do cache Caffeine.
 * Verifica hit rate, tamanho e performance do cache.
 */
@Component
public class CacheHealthIndicator implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(CacheHealthIndicator.class);

    private final CacheManager cacheManager;

    public CacheHealthIndicator(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public Health health() {
        try {
            if (cacheManager == null) {
                return Health.unknown()
                        .withDetail("status", "Cache manager não configurado")
                        .build();
            }

            var healthBuilder = Health.up();
            int totalCaches = 0;
            long totalEntries = 0;
            double avgHitRate = 0.0;
            int cachesWithStats = 0;

            // Iterar sobre todos os caches configurados
            for (String cacheName : cacheManager.getCacheNames()) {
                var cache = cacheManager.getCache(cacheName);

                if (cache instanceof CaffeineCache caffeineCache) {
                    totalCaches++;

                    Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
                    long size = nativeCache.estimatedSize();
                    totalEntries += size;

                    // Obter estatísticas se disponíveis
                    CacheStats stats = nativeCache.stats();

                    if (stats.requestCount() > 0) {
                        cachesWithStats++;
                        double hitRate = stats.hitRate() * 100;
                        avgHitRate += hitRate;

                        healthBuilder.withDetail(cacheName, String.format(
                            "Tamanho: %d | Hit Rate: %.2f%% | Hits: %d | Misses: %d",
                            size, hitRate, stats.hitCount(), stats.missCount()
                        ));
                    } else {
                        healthBuilder.withDetail(cacheName, String.format(
                            "Tamanho: %d | Sem estatísticas ainda",
                            size
                        ));
                    }
                }
            }

            if (cachesWithStats > 0) {
                avgHitRate = avgHitRate / cachesWithStats;
            }

            // Determinar status baseado no hit rate médio
            String status;
            if (avgHitRate < 50) {
                status = "WARNING - Hit rate baixo (<50%)";
            } else if (avgHitRate < 70) {
                status = "OK - Hit rate moderado";
            } else {
                status = "Excelente - Hit rate alto (>70%)";
            }

            return healthBuilder
                    .withDetail("status", status)
                    .withDetail("totalCaches", totalCaches)
                    .withDetail("totalEntries", totalEntries)
                    .withDetail("averageHitRate", String.format("%.2f%%", avgHitRate))
                    .build();

        } catch (Exception e) {
            logger.error("Erro ao verificar saúde do cache", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
        }
    }
}

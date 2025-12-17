package br.com.brisabr.helpdesk_api.config;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import jakarta.persistence.EntityManagerFactory;

/**
 * Configuração para monitoramento de estatísticas do Hibernate.
 * Ajuda a identificar N+1 queries, cache hit/miss, e outras métricas de performance.
 *
 * Habilitar com: HIBERNATE_STATISTICS=true
 */
@Slf4j
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "spring.jpa.properties.hibernate.generate_statistics", havingValue = "true")
public class HibernateStatisticsConfig {

    private final EntityManagerFactory entityManagerFactory;

    public HibernateStatisticsConfig(EntityManagerFactory entityManagerFactory, MeterRegistry meterRegistry) {
        this.entityManagerFactory = entityManagerFactory;
        // meterRegistry não é usado diretamente, mas é necessário no construtor
    }

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> hibernateMetrics() {
        return registry -> {
            SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
            Statistics statistics = sessionFactory.getStatistics();
            statistics.setStatisticsEnabled(true);

            // Query metrics
            registry.gauge("hibernate.query.execution.count", statistics, Statistics::getQueryExecutionCount);
            registry.gauge("hibernate.query.cache.hit.count", statistics, Statistics::getQueryCacheHitCount);
            registry.gauge("hibernate.query.cache.miss.count", statistics, Statistics::getQueryCacheMissCount);
            registry.gauge("hibernate.query.cache.put.count", statistics, Statistics::getQueryCachePutCount);

            // Second-level cache metrics
            registry.gauge("hibernate.second.level.cache.hit.count", statistics, Statistics::getSecondLevelCacheHitCount);
            registry.gauge("hibernate.second.level.cache.miss.count", statistics, Statistics::getSecondLevelCacheMissCount);
            registry.gauge("hibernate.second.level.cache.put.count", statistics, Statistics::getSecondLevelCachePutCount);

            // Entity metrics
            registry.gauge("hibernate.entity.load.count", statistics, Statistics::getEntityLoadCount);
            registry.gauge("hibernate.entity.fetch.count", statistics, Statistics::getEntityFetchCount);
            registry.gauge("hibernate.entity.insert.count", statistics, Statistics::getEntityInsertCount);
            registry.gauge("hibernate.entity.update.count", statistics, Statistics::getEntityUpdateCount);
            registry.gauge("hibernate.entity.delete.count", statistics, Statistics::getEntityDeleteCount);

            // Connection metrics
            registry.gauge("hibernate.connection.obtained.count", statistics, Statistics::getConnectCount);

            // Session metrics
            registry.gauge("hibernate.session.opened.count", statistics, Statistics::getSessionOpenCount);
            registry.gauge("hibernate.session.closed.count", statistics, Statistics::getSessionCloseCount);

            // Transaction metrics
            registry.gauge("hibernate.transaction.count", statistics, Statistics::getTransactionCount);
            registry.gauge("hibernate.successful.transaction.count", statistics, Statistics::getSuccessfulTransactionCount);

            log.info("✅ Hibernate statistics metrics registered");
        };
    }

    /**
     * Log estatísticas do Hibernate a cada 5 minutos em modo DEBUG
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void logHibernateStatistics() {
        if (!log.isDebugEnabled()) {
            return;
        }

        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();

        log.debug("=== Hibernate Statistics ===");
        log.debug("Queries executed: {}", statistics.getQueryExecutionCount());
        log.debug("Query cache hit count: {}", statistics.getQueryCacheHitCount());
        log.debug("Query cache miss count: {}", statistics.getQueryCacheMissCount());
        log.debug("Query cache hit ratio: {}", calculateHitRatio(
            statistics.getQueryCacheHitCount(),
            statistics.getQueryCacheMissCount()
        ));

        log.debug("2nd level cache hit count: {}", statistics.getSecondLevelCacheHitCount());
        log.debug("2nd level cache miss count: {}", statistics.getSecondLevelCacheMissCount());
        log.debug("2nd level cache hit ratio: {}", calculateHitRatio(
            statistics.getSecondLevelCacheHitCount(),
            statistics.getSecondLevelCacheMissCount()
        ));

        log.debug("Entities loaded: {}", statistics.getEntityLoadCount());
        log.debug("Entities fetched: {}", statistics.getEntityFetchCount());
        log.debug("Entities inserted: {}", statistics.getEntityInsertCount());
        log.debug("Entities updated: {}", statistics.getEntityUpdateCount());
        log.debug("Entities deleted: {}", statistics.getEntityDeleteCount());

        log.debug("Sessions opened: {}", statistics.getSessionOpenCount());
        log.debug("Sessions closed: {}", statistics.getSessionCloseCount());
        log.debug("Transactions: {}", statistics.getTransactionCount());

        // Detect potential N+1 queries
        if (statistics.getEntityFetchCount() > statistics.getQueryExecutionCount() * 5) {
            log.warn("⚠️  Potential N+1 query detected! Entity fetches ({}) > 5x queries executed ({})",
                statistics.getEntityFetchCount(), statistics.getQueryExecutionCount());
        }

        // Detect low cache hit ratio
        double cacheHitRatio = calculateHitRatio(
            statistics.getSecondLevelCacheHitCount(),
            statistics.getSecondLevelCacheMissCount()
        );
        if (cacheHitRatio < 0.8 && statistics.getSecondLevelCacheHitCount() > 100) {
            log.warn("⚠️  Low cache hit ratio: {}%. Consider tuning cache configuration.",
                Math.round(cacheHitRatio * 100));
        }
    }

    private double calculateHitRatio(long hits, long misses) {
        long total = hits + misses;
        return total == 0 ? 0.0 : (double) hits / total;
    }

    /**
     * Endpoint para obter estatísticas do Hibernate via Actuator
     */
    @Bean
    @ConditionalOnProperty(name = "management.endpoints.web.exposure.include", havingValue = "hibernate")
    public HibernateStatisticsEndpoint hibernateStatisticsEndpoint() {
        return new HibernateStatisticsEndpoint(entityManagerFactory);
    }
}

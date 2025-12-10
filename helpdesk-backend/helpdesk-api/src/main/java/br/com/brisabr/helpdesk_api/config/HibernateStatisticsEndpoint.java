package br.com.brisabr.helpdesk_api.config;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Actuator endpoint personalizado para expor estatísticas do Hibernate.
 * Acessível via: /actuator/hibernate
 */
@Component
@Endpoint(id = "hibernate")
public class HibernateStatisticsEndpoint {

    private final EntityManagerFactory entityManagerFactory;

    public HibernateStatisticsEndpoint(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @ReadOperation
    public Map<String, Object> hibernateStatistics() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();

        Map<String, Object> stats = new HashMap<>();

        // Query statistics
        Map<String, Object> queryStats = new HashMap<>();
        queryStats.put("executionCount", statistics.getQueryExecutionCount());
        queryStats.put("cacheHitCount", statistics.getQueryCacheHitCount());
        queryStats.put("cacheMissCount", statistics.getQueryCacheMissCount());
        queryStats.put("cachePutCount", statistics.getQueryCachePutCount());
        queryStats.put("cacheHitRatio", calculateHitRatio(
            statistics.getQueryCacheHitCount(),
            statistics.getQueryCacheMissCount()
        ));
        stats.put("queries", queryStats);

        // Second-level cache statistics
        Map<String, Object> cacheStats = new HashMap<>();
        cacheStats.put("hitCount", statistics.getSecondLevelCacheHitCount());
        cacheStats.put("missCount", statistics.getSecondLevelCacheMissCount());
        cacheStats.put("putCount", statistics.getSecondLevelCachePutCount());
        cacheStats.put("hitRatio", calculateHitRatio(
            statistics.getSecondLevelCacheHitCount(),
            statistics.getSecondLevelCacheMissCount()
        ));
        stats.put("secondLevelCache", cacheStats);

        // Entity statistics
        Map<String, Object> entityStats = new HashMap<>();
        entityStats.put("loadCount", statistics.getEntityLoadCount());
        entityStats.put("fetchCount", statistics.getEntityFetchCount());
        entityStats.put("insertCount", statistics.getEntityInsertCount());
        entityStats.put("updateCount", statistics.getEntityUpdateCount());
        entityStats.put("deleteCount", statistics.getEntityDeleteCount());
        stats.put("entities", entityStats);

        // Session statistics
        Map<String, Object> sessionStats = new HashMap<>();
        sessionStats.put("openCount", statistics.getSessionOpenCount());
        sessionStats.put("closeCount", statistics.getSessionCloseCount());
        stats.put("sessions", sessionStats);

        // Connection statistics
        Map<String, Object> connectionStats = new HashMap<>();
        connectionStats.put("obtainedCount", statistics.getConnectCount());
        stats.put("connections", connectionStats);

        // Transaction statistics
        Map<String, Object> transactionStats = new HashMap<>();
        transactionStats.put("count", statistics.getTransactionCount());
        transactionStats.put("successfulCount", statistics.getSuccessfulTransactionCount());
        stats.put("transactions", transactionStats);

        // Performance warnings
        Map<String, Object> warnings = new HashMap<>();

        // Check for N+1 queries
        if (statistics.getEntityFetchCount() > statistics.getQueryExecutionCount() * 5) {
            warnings.put("nPlusOne", String.format(
                "Potential N+1 query detected! Entity fetches (%d) > 5x queries executed (%d)",
                statistics.getEntityFetchCount(), statistics.getQueryExecutionCount()
            ));
        }

        // Check cache hit ratio
        double cacheHitRatio = calculateHitRatio(
            statistics.getSecondLevelCacheHitCount(),
            statistics.getSecondLevelCacheMissCount()
        );
        if (cacheHitRatio < 0.8 && statistics.getSecondLevelCacheHitCount() > 100) {
            warnings.put("lowCacheHitRatio", String.format(
                "Low cache hit ratio: %.2f%%. Consider tuning cache configuration.",
                cacheHitRatio * 100
            ));
        }

        if (!warnings.isEmpty()) {
            stats.put("warnings", warnings);
        }

        return stats;
    }

    private double calculateHitRatio(long hits, long misses) {
        long total = hits + misses;
        return total == 0 ? 0.0 : (double) hits / total;
    }
}

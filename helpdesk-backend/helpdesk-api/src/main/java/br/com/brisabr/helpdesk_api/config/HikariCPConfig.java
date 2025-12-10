package br.com.brisabr.helpdesk_api.config;

import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Configuração avançada do HikariCP com monitoramento de métricas.
 * HikariCP é o connection pool mais rápido e eficiente para aplicações Java.
 */
@Slf4j
@Configuration
public class HikariCPConfig {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> hikariMetrics(DataSource dataSource) {
        return registry -> {
            if (dataSource instanceof HikariDataSource hikariDataSource) {
                hikariDataSource.setMetricRegistry(registry);

                log.info("✅ HikariCP metrics registered");
                log.info("HikariCP Configuration:");
                log.info("  - Pool name: {}", hikariDataSource.getPoolName());
                log.info("  - Maximum pool size: {}", hikariDataSource.getMaximumPoolSize());
                log.info("  - Minimum idle: {}", hikariDataSource.getMinimumIdle());
                log.info("  - Connection timeout: {}ms", hikariDataSource.getConnectionTimeout());
                log.info("  - Idle timeout: {}ms", hikariDataSource.getIdleTimeout());
                log.info("  - Max lifetime: {}ms", hikariDataSource.getMaxLifetime());
                log.info("  - Leak detection threshold: {}ms", hikariDataSource.getLeakDetectionThreshold());
            }
        };
    }

    /**
     * Valida configuração do HikariCP no startup
     */
    @Bean
    public HikariConfigValidator hikariConfigValidator(DataSource dataSource) {
        return new HikariConfigValidator(dataSource);
    }

    public static class HikariConfigValidator {
        public HikariConfigValidator(DataSource dataSource) {
            if (dataSource instanceof HikariDataSource hikariDataSource) {
                validateConfiguration(hikariDataSource);
            }
        }

        private void validateConfiguration(HikariDataSource dataSource) {
            // Validate pool size
            int maxPoolSize = dataSource.getMaximumPoolSize();
            int minIdle = dataSource.getMinimumIdle();

            if (maxPoolSize < 10) {
                log.warn("⚠️  HikariCP maximum pool size ({}) is low. Consider increasing for production.", maxPoolSize);
            }

            if (minIdle < 5) {
                log.warn("⚠️  HikariCP minimum idle ({}) is low. Consider increasing to avoid connection creation overhead.", minIdle);
            }

            if (minIdle > maxPoolSize / 2) {
                log.warn("⚠️  HikariCP minimum idle ({}) is more than half of maximum pool size ({}). This may waste resources.",
                    minIdle, maxPoolSize);
            }

            // Validate timeouts
            long connectionTimeout = dataSource.getConnectionTimeout();
            long idleTimeout = dataSource.getIdleTimeout();
            long maxLifetime = dataSource.getMaxLifetime();

            if (connectionTimeout > 60000) {
                log.warn("⚠️  HikariCP connection timeout ({}ms) is high. Clients may wait too long for connections.",
                    connectionTimeout);
            }

            if (idleTimeout < 300000) {
                log.warn("⚠️  HikariCP idle timeout ({}ms) is low. Connections may be recycled too frequently.",
                    idleTimeout);
            }

            if (maxLifetime < 600000) {
                log.warn("⚠️  HikariCP max lifetime ({}ms) is low. Consider increasing to reduce connection churn.",
                    maxLifetime);
            }

            // Validate leak detection
            long leakDetectionThreshold = dataSource.getLeakDetectionThreshold();
            if (leakDetectionThreshold == 0) {
                log.info("ℹ️  HikariCP leak detection is disabled. Enable in development to catch connection leaks.");
            }

            log.info("✅ HikariCP configuration validated");
        }
    }
}

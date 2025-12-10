package br.com.brisabr.helpdesk_api.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Health indicator customizado para verificar a saúde do banco de dados.
 * Verifica conectividade, número de conexões ativas e performance de queries.
 */
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseHealthIndicator.class);

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try {
            long startTime = System.currentTimeMillis();

            // Tenta executar uma query simples
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT 1")) {

                long queryTime = System.currentTimeMillis() - startTime;

                if (!resultSet.next()) {
                    return Health.down()
                            .withDetail("error", "Query não retornou resultados")
                            .build();
                }

                // Buscar estatísticas de conexões (PostgreSQL)
                String connectionStatsQuery =
                    "SELECT count(*) as active_connections FROM pg_stat_activity WHERE state = 'active'";

                int activeConnections = 0;
                try (Statement statsStatement = connection.createStatement();
                     ResultSet statsResult = statsStatement.executeQuery(connectionStatsQuery)) {
                    if (statsResult.next()) {
                        activeConnections = statsResult.getInt("active_connections");
                    }
                }

                // Determinar status baseado no tempo de resposta
                if (queryTime > 1000) {
                    return Health.down()
                            .withDetail("responseTime", queryTime + "ms")
                            .withDetail("status", "Query muito lenta")
                            .withDetail("activeConnections", activeConnections)
                            .build();
                } else if (queryTime > 500) {
                    return Health.up()
                            .withDetail("status", "WARNING - Resposta lenta")
                            .withDetail("responseTime", queryTime + "ms")
                            .withDetail("activeConnections", activeConnections)
                            .build();
                }

                return Health.up()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("responseTime", queryTime + "ms")
                        .withDetail("activeConnections", activeConnections)
                        .withDetail("status", "Conectado e responsivo")
                        .build();
            }

        } catch (Exception e) {
            logger.error("Erro ao verificar saúde do banco de dados", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
        }
    }
}

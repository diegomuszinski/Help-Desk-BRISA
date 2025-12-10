package br.com.brisabr.helpdesk_api.metrics;

import br.com.brisabr.helpdesk_api.ratelimit.RateLimit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST para exposição de métricas de negócio.
 * Acesso restrito a ADMIN e MANAGER.
 */
@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private static final Logger logger = LoggerFactory.getLogger(MetricsController.class);

    private final BusinessMetricsService metricsService;

    public MetricsController(BusinessMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * Retorna métricas completas de negócio do HelpDesk.
     *
     * Métricas incluem:
     * - Volume de tickets (abertos, em andamento, fechados)
     * - Conformidade de SLA
     * - Produtividade dos técnicos
     * - Satisfação dos clientes
     * - Tendências e distribuições
     *
     * @return BusinessMetricsDTO com todas as métricas
     */
    @RateLimit(requestsPerMinute = 30, type = RateLimit.LimitType.PER_USER)
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<BusinessMetricsDTO> getBusinessMetrics() {
        logger.info("Requisição de métricas de negócio recebida");

        BusinessMetricsDTO metrics = metricsService.calculateMetrics();

        logger.info("Métricas calculadas com sucesso - Tickets abertos: {}, SLA: {}%",
                metrics.tickets().totalOpen(),
                metrics.sla().complianceRate());

        return ResponseEntity.ok(metrics);
    }
}

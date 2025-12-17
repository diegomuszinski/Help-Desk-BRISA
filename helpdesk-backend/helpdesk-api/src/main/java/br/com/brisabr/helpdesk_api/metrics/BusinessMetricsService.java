package br.com.brisabr.helpdesk_api.metrics;

import br.com.brisabr.helpdesk_api.ticket.TicketRepository;
import br.com.brisabr.helpdesk_api.ticket.TicketStatus;
import br.com.brisabr.helpdesk_api.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço responsável por calcular métricas de negócio do HelpDesk.
 * Métricas são calculadas sob demanda e cacheadas para otimização.
 */
@Service
public class BusinessMetricsService {

    private static final Logger logger = LoggerFactory.getLogger(BusinessMetricsService.class);

    private final TicketRepository ticketRepository;

    public BusinessMetricsService(TicketRepository ticketRepository,
                                  UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        // userRepository não é usado diretamente no momento
    }

    /**
     * Calcula métricas completas de negócio.
     * Resultado é cacheado por 5 minutos.
     */
    @Cacheable(value = "businessMetrics", key = "'all'")
    public BusinessMetricsDTO calculateMetrics() {
        logger.info("Calculando métricas de negócio...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        return new BusinessMetricsDTO(
                now,
                calculateTicketMetrics(startOfToday),
                calculateSlaMetrics(),
                calculateProductivityMetrics(),
                calculateSatisfactionMetrics(),
                calculateTicketsByCategory(),
                calculateTicketsByPriority(),
                calculateTrendMetrics(sevenDaysAgo, now)
        );
    }

    private BusinessMetricsDTO.TicketMetrics calculateTicketMetrics(LocalDateTime startOfToday) {
        long totalOpen = ticketRepository.countByStatus(TicketStatus.ABERTO);
        long totalInProgress = ticketRepository.countByStatus(TicketStatus.EM_ANDAMENTO);
        long totalResolved = ticketRepository.countByStatus(TicketStatus.RESOLVIDO);
        long totalClosed = ticketRepository.countByStatus(TicketStatus.FECHADO);

        long createdToday = ticketRepository.countByDataAberturaBetween(startOfToday, LocalDateTime.now());
        long closedToday = ticketRepository.countByStatusAndDataFechamentoBetween(
                TicketStatus.FECHADO, startOfToday, LocalDateTime.now());

        // Tickets atrasados: abertos há mais de 48 horas sem resolução
        LocalDateTime overdueThreshold = LocalDateTime.now().minusHours(48);
        long overdueTickets = ticketRepository.countByStatusInAndDataAberturaLessThan(
                Arrays.asList(TicketStatus.ABERTO, TicketStatus.EM_ANDAMENTO, TicketStatus.RESOLVIDO),
                overdueThreshold);

        // Tempo médio de resolução
        Double avgResolutionMinutes = ticketRepository.calculateAverageResolutionTime();
        double avgResolutionHours = avgResolutionMinutes != null ? avgResolutionMinutes / 60.0 : 0.0;

        return new BusinessMetricsDTO.TicketMetrics(
                totalOpen,
                totalInProgress,
                totalResolved,
                totalClosed,
                createdToday,
                closedToday,
                overdueTickets,
                Math.round(avgResolutionHours * 100.0) / 100.0
        );
    }

    private BusinessMetricsDTO.SlaMetrics calculateSlaMetrics() {
        long totalTickets = ticketRepository.count();

        // SLA violado: fechados após 72 horas
        long ticketsBreachedSla = ticketRepository.countTicketsBreachingSla();
        long ticketsWithinSla = totalTickets - ticketsBreachedSla;

        // Tickets em risco crítico: abertos há mais de 60 horas e ainda não fechados
        LocalDateTime criticalThreshold = LocalDateTime.now().minusHours(60);
        long criticalSlaTickets = ticketRepository.countByStatusInAndDataAberturaLessThan(
                Arrays.asList(TicketStatus.ABERTO, TicketStatus.EM_ANDAMENTO, TicketStatus.RESOLVIDO),
                criticalThreshold);

        double complianceRate = totalTickets > 0
                ? (ticketsWithinSla * 100.0) / totalTickets
                : 100.0;

        // Tempo médio de primeira resposta (primeira atribuição ou comentário)
        Double avgResponseMinutes = ticketRepository.calculateAverageFirstResponseTime();
        double avgResponseHours = avgResponseMinutes != null ? avgResponseMinutes / 60.0 : 0.0;

        return new BusinessMetricsDTO.SlaMetrics(
                Math.round(complianceRate * 100.0) / 100.0,
                ticketsWithinSla,
                ticketsBreachedSla,
                criticalSlaTickets,
                Math.round(avgResponseHours * 100.0) / 100.0
        );
    }

    private BusinessMetricsDTO.ProductivityMetrics calculateProductivityMetrics() {
        // Técnicos ativos (com pelo menos 1 ticket atribuído)
        long activeTechnicians = ticketRepository.countDistinctTechnicians();

        // Total de tickets atribuídos
        long totalAssignedTickets = ticketRepository.countByAtribuidoIsNotNull();

        double avgTicketsPerTechnician = activeTechnicians > 0
                ? (double) totalAssignedTickets / activeTechnicians
                : 0.0;

        // Taxa de resolução por dia (últimos 7 dias)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        long resolvedLast7Days = ticketRepository.countByStatusAndDataFechamentoBetween(
                TicketStatus.FECHADO, sevenDaysAgo, LocalDateTime.now());
        double avgResolutionRatePerDay = resolvedLast7Days / 7.0;

        // Top 5 técnicos
        List<BusinessMetricsDTO.TechnicianPerformance> topPerformers =
                calculateTopPerformers();

        return new BusinessMetricsDTO.ProductivityMetrics(
                activeTechnicians,
                Math.round(avgTicketsPerTechnician * 100.0) / 100.0,
                Math.round(avgResolutionRatePerDay * 100.0) / 100.0,
                topPerformers
        );
    }

    private List<BusinessMetricsDTO.TechnicianPerformance> calculateTopPerformers() {
        List<Object[]> technicianStats = ticketRepository.getAnalystPerformance();

        return technicianStats.stream()
                .limit(5)
                .map(stats -> {
                    Long techId = (Long) stats[0];
                    String techName = (String) stats[1];
                    Long assigned = (Long) stats[2];
                    Long resolved = (Long) stats[3];
                    Double avgTime = (Double) stats[4];

                    double resolutionRate = assigned > 0
                            ? (resolved * 100.0) / assigned
                            : 0.0;

                    return new BusinessMetricsDTO.TechnicianPerformance(
                            techId,
                            techName,
                            assigned,
                            resolved,
                            Math.round(resolutionRate * 100.0) / 100.0,
                            avgTime != null ? Math.round(avgTime / 60.0 * 100.0) / 100.0 : 0.0
                    );
                })
                .collect(Collectors.toList());
    }

    private BusinessMetricsDTO.SatisfactionMetrics calculateSatisfactionMetrics() {
        // Simulação - em produção viria de tabela de avaliações
        Double avgRating = 4.2;
        long totalRatings = ticketRepository.countByStatus(TicketStatus.FECHADO);
        long positiveRatings = (long) (totalRatings * 0.75);
        long negativeRatings = (long) (totalRatings * 0.10);
        double satisfactionRate = totalRatings > 0
                ? (positiveRatings * 100.0) / totalRatings
                : 0.0;

        return new BusinessMetricsDTO.SatisfactionMetrics(
                avgRating,
                totalRatings,
                positiveRatings,
                negativeRatings,
                Math.round(satisfactionRate * 100.0) / 100.0
        );
    }

    private Map<String, Long> calculateTicketsByCategory() {
        List<Object[]> results = ticketRepository.countTicketsByCategory();
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1],
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    private Map<String, Long> calculateTicketsByPriority() {
        List<Object[]> results = ticketRepository.countTicketsByPriority();
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1],
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    private BusinessMetricsDTO.TrendMetrics calculateTrendMetrics(
            LocalDateTime start, LocalDateTime end) {

        List<BusinessMetricsDTO.DailyTrend> dailyTrends = new ArrayList<>();

        for (LocalDate date = start.toLocalDate();
             !date.isAfter(end.toLocalDate());
             date = date.plusDays(1)) {

            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            long created = ticketRepository.countByDataAberturaBetween(dayStart, dayEnd);
            long closed = ticketRepository.countByStatusAndDataFechamentoBetween(
                    TicketStatus.FECHADO, dayStart, dayEnd);
            long backlog = ticketRepository.countOpenTicketsAtDate(dayEnd);

            dailyTrends.add(new BusinessMetricsDTO.DailyTrend(
                    dayStart, created, closed, backlog
            ));
        }

        // Calcular taxa de crescimento
        long firstDayCreated = dailyTrends.isEmpty() ? 0 : dailyTrends.get(0).created();
        long lastDayCreated = dailyTrends.isEmpty() ? 0 : dailyTrends.get(dailyTrends.size() - 1).created();

        double growthRate = firstDayCreated > 0
                ? ((lastDayCreated - firstDayCreated) * 100.0) / firstDayCreated
                : 0.0;

        String trend = growthRate > 10 ? "UP" : growthRate < -10 ? "DOWN" : "STABLE";

        return new BusinessMetricsDTO.TrendMetrics(
                dailyTrends,
                Math.round(growthRate * 100.0) / 100.0,
                trend
        );
    }
}

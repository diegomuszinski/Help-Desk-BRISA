package br.com.brisabr.helpdesk_api.metrics;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO contendo métricas de negócio do sistema HelpDesk.
 * Fornece visão consolidada de performance e operação.
 */
public record BusinessMetricsDTO(

        // Timestamp da coleta
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp,

        // Métricas de tickets
        TicketMetrics tickets,

        // Métricas de SLA
        SlaMetrics sla,

        // Métricas de produtividade
        ProductivityMetrics productivity,

        // Métricas de satisfação
        SatisfactionMetrics satisfaction,

        // Distribuição por categoria
        Map<String, Long> ticketsByCategory,

        // Distribuição por prioridade
        Map<String, Long> ticketsByPriority,

        // Tendências (últimos 7 dias)
        TrendMetrics trends
) {

    public record TicketMetrics(
            long totalOpen,           // Total de tickets abertos
            long totalInProgress,     // Total em andamento
            long totalResolved,       // Total resolvidos (aguardando validação)
            long totalClosed,         // Total fechados
            long createdToday,        // Criados hoje
            long closedToday,         // Fechados hoje
            long overdueTickets,      // Tickets atrasados (SLA)
            double avgResolutionTimeHours  // Tempo médio de resolução em horas
    ) {}

    public record SlaMetrics(
            double complianceRate,        // Taxa de conformidade SLA (%)
            long ticketsWithinSla,        // Tickets dentro do SLA
            long ticketsBreachedSla,      // Tickets que violaram SLA
            long criticalSlaTickets,      // Tickets em risco crítico de SLA
            double avgResponseTimeHours   // Tempo médio de primeira resposta (horas)
    ) {}

    public record ProductivityMetrics(
            long activeTechnicians,              // Técnicos ativos (com tickets)
            double avgTicketsPerTechnician,      // Média de tickets por técnico
            double avgResolutionRatePerDay,      // Taxa média de resolução por dia
            List<TechnicianPerformance> topPerformers  // Top 5 técnicos
    ) {}

    public record TechnicianPerformance(
            Long technicianId,
            String technicianName,
            long assignedTickets,
            long resolvedTickets,
            double resolutionRate,
            double avgResolutionTimeHours
    ) {}

    public record SatisfactionMetrics(
            Double avgRating,             // Avaliação média (0-5)
            long totalRatings,            // Total de avaliações
            long positiveRatings,         // Avaliações positivas (>= 4)
            long negativeRatings,         // Avaliações negativas (<= 2)
            double satisfactionRate       // Taxa de satisfação (%)
    ) {}

    public record TrendMetrics(
            List<DailyTrend> daily,       // Tendência diária (últimos 7 dias)
            double growthRate,            // Taxa de crescimento de tickets (%)
            String trend                  // "UP", "DOWN", "STABLE"
    ) {}

    public record DailyTrend(
            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDateTime date,
            long created,
            long closed,
            long backlog
    ) {}
}

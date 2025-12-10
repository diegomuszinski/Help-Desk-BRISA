package br.com.brisabr.helpdesk_api.examples;

/**
 * ARQUIVO DE EXEMPLO EDUCACIONAL - Demonstra recursos do Java 21.
 *
 * Este arquivo demonstra pattern matching, sealed interfaces e record patterns.
 * Não é usado em produção, serve apenas como referência de implementação.
 *
 * Se o VS Code mostrar erros, ignore - o arquivo compila corretamente com Java 21.
 *
 * Sealed interface para tipos de notificação (Java 17+).
 *
 * Sealed interfaces/classes restringem quais classes podem implementá-las,
 * permitindo que o compilador faça exaustive checking em pattern matching.
 */
public sealed interface NotificationEvent
    permits TicketCreatedEvent, TicketAssignedEvent, TicketClosedEvent, TicketReopenedEvent {

    Long ticketId();
    String message();
}

/**
 * Event records usando Pattern Matching (Java 21+)
 */
record TicketCreatedEvent(Long ticketId, String solicitante, String categoria) implements NotificationEvent {
    @Override
    public String message() {
        return String.format("Novo chamado criado por %s - Categoria: %s", solicitante, categoria);
    }
}

record TicketAssignedEvent(Long ticketId, String tecnico, String prioridade) implements NotificationEvent {
    @Override
    public String message() {
        return String.format("Chamado atribuído para %s - Prioridade: %s", tecnico, prioridade);
    }
}

record TicketClosedEvent(Long ticketId, String tecnico, String solucao) implements NotificationEvent {
    @Override
    public String message() {
        return String.format("Chamado resolvido por %s", tecnico);
    }
}

record TicketReopenedEvent(Long ticketId, String motivo, String solicitante) implements NotificationEvent {
    @Override
    public String message() {
        return String.format("Chamado reaberto por %s - Motivo: %s", solicitante, motivo);
    }
}

/**
 * Exemplo de Pattern Matching for Switch (Java 21+)
 */
class NotificationService {

    /**
     * Pattern Matching com type patterns e guarded patterns (Java 21+)
     *
     * Benefícios:
     * - Exhaustive checking: compilador garante todos os casos cobertos
     * - Type casting automático
     * - Guarded patterns com when clauses
     * - Record patterns: desconstrução de records
     */
    public String formatNotification(NotificationEvent event) {
        return switch (event) {
            // Record pattern: desconstrói o record automaticamente
            case TicketCreatedEvent(var id, var solicitante, var categoria)
                when "Crítica".equals(categoria) ->
                    String.format("🔴 URGENTE: Chamado #%d criado por %s", id, solicitante);

            case TicketCreatedEvent e ->
                String.format("Novo chamado #%d: %s", e.ticketId(), e.message());

            case TicketAssignedEvent(var id, var tecnico, var prioridade)
                when "Alta".equals(prioridade) || "Crítica".equals(prioridade) ->
                    String.format("⚠️ Chamado #%d de alta prioridade atribuído para %s", id, tecnico);

            case TicketAssignedEvent e ->
                String.format("Chamado #%d: %s", e.ticketId(), e.message());

            case TicketClosedEvent e ->
                String.format("✅ Chamado #%d resolvido: %s", e.ticketId(), e.message());

            case TicketReopenedEvent e ->
                String.format("🔄 Chamado #%d reaberto: %s", e.ticketId(), e.message());

            // Não precisa de default pois sealed interface garante exhaustiveness
        };
    }

    /**
     * Exemplo de switch expression com null handling (Java 21+)
     */
    public String getEventPriority(NotificationEvent event) {
        return switch (event) {
            case null -> "UNKNOWN";
            case TicketCreatedEvent e when "Crítica".equals(e.categoria()) -> "CRITICAL";
            case TicketCreatedEvent e when "Alta".equals(e.categoria()) -> "HIGH";
            case TicketAssignedEvent e when "Crítica".equals(e.prioridade()) -> "CRITICAL";
            case TicketAssignedEvent e when "Alta".equals(e.prioridade()) -> "HIGH";
            case TicketReopenedEvent e -> "MEDIUM";
            default -> "NORMAL";
        };
    }
}

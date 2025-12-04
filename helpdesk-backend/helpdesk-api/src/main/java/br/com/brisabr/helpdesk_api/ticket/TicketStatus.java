package br.com.brisabr.helpdesk_api.ticket;

/**
 * Enum que representa os possíveis status de um ticket no sistema.
 * 
 * Fluxo padrão:
 * ABERTO → EM_ANDAMENTO → RESOLVIDO → (pode voltar para ABERTO via reopen)
 * 
 * Status legados mantidos para compatibilidade:
 * - ENCERRADO
 * - FECHADO
 */
public enum TicketStatus {
    /**
     * Ticket foi criado e está aguardando atribuição a um técnico.
     */
    ABERTO("Aberto"),
    
    /**
     * Ticket foi atribuído a um técnico e está sendo trabalhado.
     */
    EM_ANDAMENTO("Em Andamento"),
    
    /**
     * Ticket foi resolvido pelo técnico e aguarda validação.
     */
    RESOLVIDO("Resolvido"),
    
    /**
     * Ticket foi encerrado (legado, mantido para compatibilidade).
     */
    ENCERRADO("Encerrado"),
    
    /**
     * Ticket foi fechado (legado, mantido para compatibilidade).
     */
    FECHADO("Fechado");
    
    private final String displayName;
    
    TicketStatus(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Retorna o nome de exibição do status.
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Verifica se o ticket está em um estado finalizado.
     */
    public boolean isFinalizado() {
        return this == RESOLVIDO || this == ENCERRADO || this == FECHADO;
    }
    
    /**
     * Verifica se o ticket pode ser atribuído/capturado.
     */
    public boolean podeSerAtribuido() {
        return this == ABERTO;
    }
    
    /**
     * Converte string para enum, aceitando tanto o nome quanto o displayName.
     */
    public static TicketStatus fromString(String status) {
        if (status == null) {
            return null;
        }
        
        // Tentar pelo nome do enum
        try {
            return TicketStatus.valueOf(status.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            // Tentar pelo displayName
            for (TicketStatus ts : TicketStatus.values()) {
                if (ts.displayName.equalsIgnoreCase(status)) {
                    return ts;
                }
            }
            throw new IllegalArgumentException("Status inválido: " + status);
        }
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}

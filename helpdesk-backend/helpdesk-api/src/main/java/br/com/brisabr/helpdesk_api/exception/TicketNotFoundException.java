package br.com.brisabr.helpdesk_api.exception;

/**
 * Exceção lançada quando um ticket não é encontrado no sistema.
 */
public class TicketNotFoundException extends RuntimeException {
    
    public TicketNotFoundException(Long id) {
        super("Chamado não encontrado com o ID: " + id);
    }
    
    public TicketNotFoundException(String message) {
        super(message);
    }
    
    public TicketNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

package br.com.brisabr.helpdesk_api.exception;

/**
 * Exceção lançada quando uma operação é tentada em um ticket com estado inválido.
 * Por exemplo: tentar reabrir um ticket que não está fechado.
 */
public class InvalidTicketStateException extends RuntimeException {
    
    public InvalidTicketStateException(String message) {
        super(message);
    }
    
    public InvalidTicketStateException(String message, Throwable cause) {
        super(message, cause);
    }
}

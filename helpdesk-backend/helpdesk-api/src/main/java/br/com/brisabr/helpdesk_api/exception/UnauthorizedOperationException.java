package br.com.brisabr.helpdesk_api.exception;

/**
 * Exceção lançada quando um usuário tenta realizar uma operação não autorizada.
 * Diferente de AccessDeniedException que é para recursos, esta é para operações de negócio.
 */
public class UnauthorizedOperationException extends RuntimeException {
    
    public UnauthorizedOperationException(String message) {
        super(message);
    }
    
    public UnauthorizedOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}

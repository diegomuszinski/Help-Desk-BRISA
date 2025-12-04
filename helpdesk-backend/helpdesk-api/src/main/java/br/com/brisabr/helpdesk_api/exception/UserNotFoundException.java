package br.com.brisabr.helpdesk_api.exception;

/**
 * Exceção lançada quando um usuário não é encontrado no sistema.
 */
public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(Long id) {
        super("Usuário não encontrado com o ID: " + id);
    }
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

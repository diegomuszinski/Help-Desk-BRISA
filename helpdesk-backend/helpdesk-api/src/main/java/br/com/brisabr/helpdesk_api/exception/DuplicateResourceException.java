package br.com.brisabr.helpdesk_api.exception;

/**
 * Exceção lançada quando tenta-se criar um recurso que já existe.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}

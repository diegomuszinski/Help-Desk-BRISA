package br.com.brisabr.helpdesk_api.exception;

/**
 * Exceção lançada quando um recurso solicitado não é encontrado.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

package br.com.brisabr.helpdesk_api.exception;

/**
 * Exceção lançada quando um anexo não é encontrado no sistema.
 */
public class AttachmentNotFoundException extends RuntimeException {
    
    public AttachmentNotFoundException(Long id) {
        super("Anexo não encontrado com o ID: " + id);
    }
    
    public AttachmentNotFoundException(String message) {
        super(message);
    }
    
    public AttachmentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

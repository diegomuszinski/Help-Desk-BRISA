package br.com.brisabr.helpdesk_api.exception;

/**
 * Exceção lançada quando um refresh token é inválido ou expirado.
 */
public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException(String message) {
        super(message);
    }

    public InvalidRefreshTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}

package br.com.brisabr.helpdesk_api.auth;

/**
 * Record para resposta de autenticação (Java 21+).
 * 
 * Records são ideais para DTOs imutáveis como respostas de API.
 * Eliminam boilerplate de getters, equals, hashCode e toString.
 */
public record AuthResponseRecord(
    String accessToken,
    String refreshToken,
    String type,
    Long expiresIn,
    String userEmail,
    String userName,
    String userRole
) {
    /**
     * Constructor padrão com tipo Bearer
     */
    public AuthResponseRecord(
        String accessToken, 
        String refreshToken, 
        Long expiresIn,
        String userEmail,
        String userName,
        String userRole
    ) {
        this(accessToken, refreshToken, "Bearer", expiresIn, userEmail, userName, userRole);
    }
    
    /**
     * Compact constructor com validação
     */
    public AuthResponseRecord {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("Access token não pode ser vazio");
        }
        if (type == null || type.isBlank()) {
            type = "Bearer";
        }
    }
}

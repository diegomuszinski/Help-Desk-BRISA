package br.com.brisabr.helpdesk_api.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO contendo par de tokens: access token (JWT) + refresh token.
 */
@Data
@AllArgsConstructor
public class TokenPairDTO {
    private String accessToken;
    private String refreshToken;
}

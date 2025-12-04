package br.com.brisabr.helpdesk_api.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequestDTO {
    
    @NotBlank(message = "Refresh token é obrigatório")
    private String refreshToken;
}

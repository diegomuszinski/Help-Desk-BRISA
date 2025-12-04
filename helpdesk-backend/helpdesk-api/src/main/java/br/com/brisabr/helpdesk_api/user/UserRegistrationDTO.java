package br.com.brisabr.helpdesk_api.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationDTO {
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String nome;
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    private String email;
    
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 12, max = 100, message = "Senha deve ter entre 12 e 100 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{12,}$",
        message = "Senha deve conter pelo menos: 1 letra maiúscula, 1 minúscula, 1 número e 1 caractere especial (@$!%*?&#)"
    )
    private String senha;
    
    @NotBlank(message = "Perfil é obrigatório")
    @Pattern(regexp = "ADMIN|MANAGER|TECHNICIAN|USER", message = "Perfil deve ser ADMIN, MANAGER, TECHNICIAN ou USER")
    private String perfil; 
}
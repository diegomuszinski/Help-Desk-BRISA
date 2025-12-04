package br.com.brisabr.helpdesk_api.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentCreateDTO {
    
    @NotBlank(message = "Comentário é obrigatório")
    @Size(min = 1, max = 2000, message = "Comentário deve ter entre 1 e 2000 caracteres")
    private String comentario;
}
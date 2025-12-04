package br.com.brisabr.helpdesk_api.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketCreateDTO {
    
    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 10, max = 5000, message = "Descrição deve ter entre 10 e 5000 caracteres")
    private String description;
    
    @NotBlank(message = "Categoria é obrigatória")
    @Size(min = 3, max = 50, message = "Categoria deve ter entre 3 e 50 caracteres")
    private String category;
    
    @NotBlank(message = "Prioridade é obrigatória")
    @Pattern(regexp = "BAIXA|MEDIA|ALTA|URGENTE", message = "Prioridade deve ser BAIXA, MEDIA, ALTA ou URGENTE")
    private String priority;
    
}
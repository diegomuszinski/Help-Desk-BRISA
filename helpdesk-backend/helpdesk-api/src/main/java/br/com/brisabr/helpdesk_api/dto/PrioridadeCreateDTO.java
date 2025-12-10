package br.com.brisabr.helpdesk_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para criação de prioridade.
 *
 * @param nome Nome da prioridade
 */
public record PrioridadeCreateDTO(
    @NotBlank(message = "Nome da prioridade é obrigatório")
    @Size(min = 3, max = 50, message = "Nome deve ter entre 3 e 50 caracteres")
    String nome
) {}

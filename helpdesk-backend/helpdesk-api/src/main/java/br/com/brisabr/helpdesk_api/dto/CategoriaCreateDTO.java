package br.com.brisabr.helpdesk_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para criação de categoria.
 *
 * @param nome Nome da categoria
 */
public record CategoriaCreateDTO(
    @NotBlank(message = "Nome da categoria é obrigatório")
    @Size(min = 3, max = 50, message = "Nome deve ter entre 3 e 50 caracteres")
    String nome
) {}

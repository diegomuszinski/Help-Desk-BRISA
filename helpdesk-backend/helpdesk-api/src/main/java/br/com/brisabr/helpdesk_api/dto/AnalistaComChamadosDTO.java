package br.com.brisabr.helpdesk_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalistaComChamadosDTO {
    private String nomeAnalista;
    private long totalChamados;
}
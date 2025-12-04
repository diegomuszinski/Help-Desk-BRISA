package br.com.brisabr.helpdesk_api.dto;

public class RelatorioCategoriaDTO {
    private String categoria;
    private Double tempoMedioHoras;

    public RelatorioCategoriaDTO(String categoria, Double tempoMedioHoras) {
        this.categoria = categoria;
        this.tempoMedioHoras = tempoMedioHoras;
    }

    // Getters e Setters
    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Double getTempoMedioHoras() {
        return tempoMedioHoras;
    }

    public void setTempoMedioHoras(Double tempoMedioHoras) {
        this.tempoMedioHoras = tempoMedioHoras;
    }
}
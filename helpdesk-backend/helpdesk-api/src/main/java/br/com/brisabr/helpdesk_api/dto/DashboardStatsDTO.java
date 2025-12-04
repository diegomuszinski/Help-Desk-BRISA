package br.com.brisabr.helpdesk_api.dto;

import br.com.brisabr.helpdesk_api.ticket.TicketResponseDTO;

import java.util.List;

public class DashboardStatsDTO {

    private long chamadosNaFila;
    private List<RelatorioAnalistaDTO> chamadosPorAnalista;
    private List<TicketResponseDTO> chamadosSlaViolado;

    // Getters e Setters
    public long getChamadosNaFila() {
        return chamadosNaFila;
    }

    public void setChamadosNaFila(long chamadosNaFila) {
        this.chamadosNaFila = chamadosNaFila;
    }

    public List<RelatorioAnalistaDTO> getChamadosPorAnalista() {
        return chamadosPorAnalista;
    }

    public void setChamadosPorAnalista(List<RelatorioAnalistaDTO> chamadosPorAnalista) {
        this.chamadosPorAnalista = chamadosPorAnalista;
    }

    public List<TicketResponseDTO> getChamadosSlaViolado() {
        return chamadosSlaViolado;
    }

    public void setChamadosSlaViolado(List<TicketResponseDTO> chamadosSlaViolado) {
        this.chamadosSlaViolado = chamadosSlaViolado;
    }
}
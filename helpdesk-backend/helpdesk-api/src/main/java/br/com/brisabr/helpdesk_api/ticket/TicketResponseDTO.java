package br.com.brisabr.helpdesk_api.ticket;

import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class TicketResponseDTO {
    private final Long id;
    private final String numeroChamado;
    private final String descricao;
    private final String categoria;
    private final String prioridade;
    private final String status;
    private final LocalDateTime dataAbertura;
    private final LocalDateTime dataFechamento;
    private final String solucao;
    private final boolean foiReaberto;
    private final String nomeSolicitante;
    private final String nomeTecnicoAtribuido;
    private final List<HistoricoItemDTO> historico;
    private final LocalDateTime slaDeadline;
    
    
    private final List<AnexoChamadoDTO> anexos;

    public TicketResponseDTO(Ticket ticket) {
        this.id = ticket.getId();
        this.numeroChamado = ticket.getNumeroChamado();
        this.descricao = ticket.getDescricao();
        this.categoria = ticket.getCategoria();
        this.prioridade = ticket.getPrioridade();
        this.status = ticket.getStatus();
        this.dataAbertura = ticket.getDataAbertura();
        this.dataFechamento = ticket.getDataFechamento();
        this.solucao = ticket.getSolucao();
        this.foiReaberto = ticket.isFoiReaberto();
        this.nomeSolicitante = (ticket.getSolicitante() != null) ? ticket.getSolicitante().getNome() : "N/A";
        this.nomeTecnicoAtribuido = (ticket.getAtribuido() != null) ? ticket.getAtribuido().getNome() : null;
        this.historico = (ticket.getHistorico() != null) 
            ? ticket.getHistorico().stream().map(HistoricoItemDTO::new).collect(Collectors.toList()) 
            : List.of();
        this.slaDeadline = Ticket.calculateSlaDeadline(ticket.getDataAbertura(), ticket.getPrioridade());
        
        
        this.anexos = (ticket.getAnexos() != null)
            ? ticket.getAnexos().stream().map(AnexoChamadoDTO::new).collect(Collectors.toList())
            : List.of();
    }
}
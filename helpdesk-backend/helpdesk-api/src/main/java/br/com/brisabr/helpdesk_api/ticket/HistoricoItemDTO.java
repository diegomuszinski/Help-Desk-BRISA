package br.com.brisabr.helpdesk_api.ticket;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HistoricoItemDTO {
    private String autor;
    private String comentario;
    private LocalDateTime dataOcorrencia;

    public HistoricoItemDTO(HistoricoChamado historico) {
        this.autor = historico.getAutor() != null ? historico.getAutor().getNome() : "Sistema";
        this.comentario = historico.getComentario();
        this.dataOcorrencia = historico.getDataOcorrencia();
    }
}
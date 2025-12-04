package br.com.brisabr.helpdesk_api.ticket;

import java.time.LocalDateTime;

/**
 * Record para representar item de histórico.
 * 
 * Records (Java 16+) são classes imutáveis implícitas, ideais para DTOs.
 * Benefícios:
 * - Código mais conciso (sem getters/setters/equals/hashCode)
 * - Imutabilidade garantida
 * - Serialização JSON automática
 * - Pattern matching (Java 21+)
 */
public record HistoricoItemRecord(
    Long id,
    LocalDateTime dataOcorrencia,
    String comentario,
    String nomeAutor
) {
    /**
     * Constructor from entity
     */
    public HistoricoItemRecord(HistoricoChamado historico) {
        this(
            historico.getId(),
            historico.getDataOcorrencia(),
            historico.getComentario(),
            historico.getAutor() != null ? historico.getAutor().getNome() : "Sistema"
        );
    }
    
    /**
     * Compact constructor com validação
     */
    public HistoricoItemRecord {
        if (comentario == null || comentario.isBlank()) {
            throw new IllegalArgumentException("Comentário não pode ser vazio");
        }
    }
}

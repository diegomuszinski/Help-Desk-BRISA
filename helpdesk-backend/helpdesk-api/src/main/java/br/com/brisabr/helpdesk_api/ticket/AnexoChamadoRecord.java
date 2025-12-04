package br.com.brisabr.helpdesk_api.ticket;

import java.time.ZonedDateTime;

/**
 * Record para representar anexo de chamado.
 * 
 * Usando Record do Java 21 para DTO imutável.
 */
public record AnexoChamadoRecord(
    Long id,
    String nomeArquivo,
    String tipoArquivo,
    ZonedDateTime dataUpload
) {
    /**
     * Constructor from entity
     */
    public AnexoChamadoRecord(AnexoChamado anexo) {
        this(
            anexo.getId(),
            anexo.getNomeArquivo(),
            anexo.getTipoArquivo(),
            anexo.getDataUpload()
        );
    }
    
    /**
     * Compact constructor com validação
     */
    public AnexoChamadoRecord {
        if (nomeArquivo == null || nomeArquivo.isBlank()) {
            throw new IllegalArgumentException("Nome do arquivo não pode ser vazio");
        }
    }
}

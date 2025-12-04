package br.com.brisabr.helpdesk_api.ticket;

public class AnexoChamadoDTO {

    private Long id;
    private String nomeArquivo;
    private String tipoArquivo;

    public AnexoChamadoDTO(AnexoChamado anexo) {
        this.id = anexo.getId();
        this.nomeArquivo = anexo.getNomeArquivo();
        this.tipoArquivo = anexo.getTipoArquivo();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public String getTipoArquivo() {
        return tipoArquivo;
    }
}

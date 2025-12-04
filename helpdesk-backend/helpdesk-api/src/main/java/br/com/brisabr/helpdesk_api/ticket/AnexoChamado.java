package br.com.brisabr.helpdesk_api.ticket;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "anexos_chamados")
public class AnexoChamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_arquivo", nullable = false)
    private String nomeArquivo;

    @Column(name = "tipo_arquivo", nullable = false)
    private String tipoArquivo;

    @Lob
    @Column(name = "dados", nullable = false, columnDefinition = "TEXT")
    private String dados;

    @Column(name = "data_upload", nullable = false)
    private ZonedDateTime dataUpload;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chamado", nullable = false)
    private Ticket ticket;

    @PrePersist
    protected void onCreate() {
        dataUpload = ZonedDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNomeArquivo() { return nomeArquivo; }
    public void setNomeArquivo(String nomeArquivo) { this.nomeArquivo = nomeArquivo; }
    public String getTipoArquivo() { return tipoArquivo; }
    public void setTipoArquivo(String tipoArquivo) { this.tipoArquivo = tipoArquivo; }
    public String getDados() { return dados; }
    public void setDados(String dados) { this.dados = dados; }
    public ZonedDateTime getDataUpload() { return dataUpload; }
    public void setDataUpload(ZonedDateTime dataUpload) { this.dataUpload = dataUpload; }
    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }
}
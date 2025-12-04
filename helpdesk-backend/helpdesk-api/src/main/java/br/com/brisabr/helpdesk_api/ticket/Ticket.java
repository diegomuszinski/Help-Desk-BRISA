package br.com.brisabr.helpdesk_api.ticket;

import br.com.brisabr.helpdesk_api.dto.RelatorioCategoriaDTO;
import br.com.brisabr.helpdesk_api.user.User;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NamedNativeQuery(
    name = "Ticket.getTempoMedioPorCategoria",
    query = "SELECT t.categoria as categoria, AVG(EXTRACT(EPOCH FROM (t.data_fechamento - t.data_abertura))) / 3600.0 as tempo_medio_horas FROM chamados t WHERE t.data_fechamento IS NOT NULL AND (:ano IS NULL OR EXTRACT(YEAR FROM t.data_fechamento) = :ano) AND (:mes IS NULL OR EXTRACT(MONTH FROM t.data_fechamento) = :mes) GROUP BY t.categoria",
    resultSetMapping = "CategoriaReportMapping"
)
@SqlResultSetMapping(
    name = "CategoriaReportMapping",
    classes = @ConstructorResult(
        targetClass = RelatorioCategoriaDTO.class,
        columns = {
            @ColumnResult(name = "categoria", type = String.class),
            @ColumnResult(name = "tempo_medio_horas", type = Double.class)
        }
    )
)
@Entity
@Table(name = "chamados")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_chamado", unique = true, nullable = false)
    private String numeroChamado;

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false)
    private String prioridade;

    @Column(nullable = false)
    private String status;

    @CreationTimestamp
    @Column(name = "data_abertura", updatable = false)
    private LocalDateTime dataAbertura;

    @Column(name = "data_fechamento")
    private LocalDateTime dataFechamento;
    
    @Column(length = 1000)
    private String solucao;

    @Column(name = "foi_reaberto")
    private boolean foiReaberto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_solicitante", nullable = false)
    private User solicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tecnico_atribuido")
    private User atribuido;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("dataOcorrencia DESC")
    @JsonManagedReference
    private List<HistoricoChamado> historico = new ArrayList<>();
    
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnexoChamado> anexos = new ArrayList<>();

    public static LocalDateTime calculateSlaDeadline(LocalDateTime openedAt, String priority) {
        if (openedAt == null || priority == null) return LocalDateTime.now().plusYears(1);
        return switch (priority.toLowerCase()) {
            case "crítica" -> openedAt.plusHours(2);
            case "alta", "alto" -> openedAt.plusHours(8);
            case "baixa", "baixo" -> openedAt.plusDays(2);
            default -> openedAt.plusHours(24);
        };
    }
}
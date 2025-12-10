package br.com.brisabr.helpdesk_api.ticket;

import br.com.brisabr.helpdesk_api.dto.RelatorioAnalistaDTO;
import br.com.brisabr.helpdesk_api.dto.RelatorioCategoriaDTO;
import br.com.brisabr.helpdesk_api.dto.RelatorioMensalDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    @Query(value = "SELECT COUNT(*) FROM chamados WHERE EXTRACT(YEAR FROM data_abertura) = :year", nativeQuery = true)
    long countByYear(@Param("year") int year);

    List<Ticket> findAllBySolicitanteId(Long solicitanteId);

    @Query(value = "SELECT COUNT(*) FROM chamados WHERE status = :status AND id_tecnico_atribuido IS NULL", nativeQuery = true)
    long countByStatusAndAtribuidoIsNull(@Param("status") String status);

    @Query(name = "Ticket.getTempoMedioPorCategoria", nativeQuery = true)
    List<RelatorioCategoriaDTO> getTempoMedioPorCategoria(@Param("ano") Integer ano, @Param("mes") Integer mes);

    @Query(value = "SELECT u.nome as nomeAnalista, COUNT(t.id) as totalChamados " +
                   "FROM chamados t JOIN usuarios u ON t.id_tecnico_atribuido = u.id " +
                   "WHERE (:ano IS NULL OR EXTRACT(YEAR FROM t.data_abertura) = :ano) " +
                   "AND (:mes IS NULL OR EXTRACT(MONTH FROM t.data_abertura) = :mes) " +
                   "GROUP BY u.nome", nativeQuery = true)
    List<RelatorioAnalistaDTO> getChamadosPorAnalista(@Param("ano") Integer ano, @Param("mes") Integer mes);

    @Query(value = "SELECT EXTRACT(MONTH FROM t.data_abertura) as mes, COUNT(t.id) as totalChamados " +
                   "FROM chamados t " +
                   "WHERE EXTRACT(YEAR FROM t.data_abertura) = :ano " +
                   "GROUP BY mes ORDER BY mes", nativeQuery = true)
    List<RelatorioMensalDTO> getChamadosPorMes(@Param("ano") Integer ano);


    @Query("SELECT t FROM Ticket t LEFT JOIN FETCH t.anexos WHERE t.id = :id")
    Optional<Ticket> findByIdWithAnexos(@Param("id") Long id);

    // ========== Métodos para Business Metrics ==========

    long countByStatus(TicketStatus status);

    long countByDataAberturaBetween(LocalDateTime start, LocalDateTime end);

    long countByStatusAndDataFechamentoBetween(TicketStatus status, LocalDateTime start, LocalDateTime end);

    long countByStatusInAndDataAberturaLessThan(List<TicketStatus> statuses, LocalDateTime date);

    @Query("SELECT COUNT(DISTINCT t.atribuido.id) FROM Ticket t WHERE t.atribuido IS NOT NULL")
    long countDistinctTechnicians();

    long countByAtribuidoIsNotNull();

    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (data_fechamento - data_abertura)) / 60) FROM chamados " +
           "WHERE status = 'FECHADO' AND data_fechamento IS NOT NULL", nativeQuery = true)
    Double calculateAverageResolutionTime();

    @Query(value = "SELECT COUNT(*) FROM chamados " +
           "WHERE status = 'FECHADO' AND EXTRACT(EPOCH FROM (data_fechamento - data_abertura)) / 3600 > 72", nativeQuery = true)
    long countTicketsBreachingSla();

    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (" +
           "(SELECT MIN(h.data_registro) FROM historico_chamado h WHERE h.id_chamado = c.id) - c.data_abertura)) / 60) " +
           "FROM chamados c WHERE EXISTS (SELECT 1 FROM historico_chamado h WHERE h.id_chamado = c.id)", nativeQuery = true)
    Double calculateAverageFirstResponseTime();

    @Query(value = "SELECT u.id, u.nome, COUNT(c.id), " +
           "SUM(CASE WHEN c.status = 'FECHADO' THEN 1 ELSE 0 END), " +
           "AVG(CASE WHEN c.status = 'FECHADO' THEN EXTRACT(EPOCH FROM (c.data_fechamento - c.data_abertura)) / 60 END) " +
           "FROM chamados c JOIN usuarios u ON c.id_tecnico_atribuido = u.id " +
           "WHERE c.id_tecnico_atribuido IS NOT NULL " +
           "GROUP BY u.id, u.nome " +
           "ORDER BY COUNT(c.id) DESC", nativeQuery = true)
    List<Object[]> getAnalystPerformance();

    @Query("SELECT t.categoria, COUNT(t) FROM Ticket t GROUP BY t.categoria ORDER BY COUNT(t) DESC")
    List<Object[]> countTicketsByCategory();

    @Query("SELECT t.prioridade, COUNT(t) FROM Ticket t GROUP BY t.prioridade ORDER BY COUNT(t) DESC")
    List<Object[]> countTicketsByPriority();

    @Query(value = "SELECT COUNT(*) FROM chamados WHERE status IN ('ABERTO', 'EM_ANDAMENTO', 'RESOLVIDO') " +
           "AND data_abertura < :date", nativeQuery = true)
    long countOpenTicketsAtDate(@Param("date") LocalDateTime date);
}

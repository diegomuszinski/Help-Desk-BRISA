package br.com.brisabr.helpdesk_api.ticket;

import br.com.brisabr.helpdesk_api.dto.RelatorioAnalistaDTO;
import br.com.brisabr.helpdesk_api.dto.RelatorioCategoriaDTO;
import br.com.brisabr.helpdesk_api.dto.RelatorioMensalDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
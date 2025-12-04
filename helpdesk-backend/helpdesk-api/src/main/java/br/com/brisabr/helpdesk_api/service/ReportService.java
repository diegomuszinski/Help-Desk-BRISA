package br.com.brisabr.helpdesk_api.service;

import br.com.brisabr.helpdesk_api.dto.RelatorioAnalistaDTO;
import br.com.brisabr.helpdesk_api.dto.RelatorioCategoriaDTO;
import br.com.brisabr.helpdesk_api.dto.RelatorioMensalDTO;
import br.com.brisabr.helpdesk_api.ticket.Ticket;
import br.com.brisabr.helpdesk_api.ticket.TicketRepository;
import br.com.brisabr.helpdesk_api.ticket.TicketSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

/**
 * Service para geração de relatórios e analytics do HelpDesk.
 * 
 * Fornece estatísticas agregadas sobre chamados, desempenho de analistas
 * e tempos médios por categoria.
 */
@Service
public class ReportService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    private final TicketRepository ticketRepository;

    public ReportService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    /**
     * Obtém estatísticas de chamados por analista.
     * 
     * @param ano Ano para filtrar (null = todos)
     * @param mes Mês para filtrar (null = todos)
     * @return Lista com estatísticas por analista
     */
    public List<RelatorioAnalistaDTO> getChamadosPorAnalista(Integer ano, Integer mes) {
        logger.info("Gerando relatório de chamados por analista - Ano: {}, Mês: {}", ano, mes);
        List<RelatorioAnalistaDTO> resultado = ticketRepository.getChamadosPorAnalista(ano, mes);
        logger.debug("Relatório gerado com {} analistas", resultado.size());
        return resultado;
    }

    /**
     * Calcula tempo médio de resolução por categoria.
     * 
     * @param ano Ano para filtrar (null = todos)
     * @param mes Mês para filtrar (null = todos)
     * @return Lista com tempo médio por categoria
     */
    public List<RelatorioCategoriaDTO> getTempoMedioPorCategoria(Integer ano, Integer mes) {
        logger.info("Gerando relatório de tempo médio por categoria - Ano: {}, Mês: {}", ano, mes);
        List<RelatorioCategoriaDTO> resultado = ticketRepository.getTempoMedioPorCategoria(ano, mes);
        logger.debug("Relatório gerado com {} categorias", resultado.size());
        return resultado;
    }

    /**
     * Obtém quantidade de chamados agrupados por mês.
     * 
     * @param ano Ano para filtrar (null = todos)
     * @return Lista com quantidade de chamados por mês
     */
    public List<RelatorioMensalDTO> getChamadosPorMes(Integer ano) {
        logger.info("Gerando relatório mensal de chamados - Ano: {}", ano);
        List<RelatorioMensalDTO> resultado = ticketRepository.getChamadosPorMes(ano);
        logger.debug("Relatório gerado com {} meses", resultado.size());
        return resultado;
    }
    
    /**
     * Gera relatório detalhado de chamados com filtros avançados.
     * 
     * @param dataInicial Data inicial do período
     * @param dataFinal Data final do período
     * @param tipoData Tipo de data (abertura/fechamento)
     * @param status Status dos chamados
     * @param categoria Categoria dos chamados
     * @param unidade Unidade
     * @param local Local
     * @param solicitanteId ID do solicitante
     * @param tecnicoId ID do técnico
     * @param ordenarPor Campo para ordenação
     * @return Lista de tickets que atendem aos critérios
     */
    public List<Ticket> getDetailedReport(
            LocalDate dataInicial, LocalDate dataFinal, String tipoData, String status,
            String categoria, String unidade, String local, Long solicitanteId, Long tecnicoId, String ordenarPor) {
        
        logger.info("Gerando relatório detalhado - Período: {} a {}, Status: {}, Categoria: {}", 
                    dataInicial, dataFinal, status, categoria);
        
        Specification<Ticket> spec = TicketSpecification.withFilters(
            dataInicial, dataFinal, tipoData, status, categoria, unidade, local, solicitanteId, tecnicoId
        );

        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        if (ordenarPor != null && !ordenarPor.isEmpty()) {
            if ("numero".equalsIgnoreCase(ordenarPor)) {
                sort = Sort.by(Sort.Direction.ASC, "id");
            }
            if ("status".equalsIgnoreCase(ordenarPor)) {
                sort = Sort.by(Sort.Direction.ASC, "status");
            }
            if ("data_abertura".equalsIgnoreCase(ordenarPor)) {
                sort = Sort.by(Sort.Direction.DESC, "dataAbertura");
            }
        }

        return ticketRepository.findAll(spec, sort);
    }
}
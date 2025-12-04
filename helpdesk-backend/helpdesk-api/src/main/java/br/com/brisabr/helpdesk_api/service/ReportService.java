package br.com.brisabr.helpdesk_api.service;

import br.com.brisabr.helpdesk_api.dto.RelatorioAnalistaDTO;
import br.com.brisabr.helpdesk_api.dto.RelatorioCategoriaDTO;
import br.com.brisabr.helpdesk_api.dto.RelatorioMensalDTO;
import br.com.brisabr.helpdesk_api.ticket.Ticket;
import br.com.brisabr.helpdesk_api.ticket.TicketRepository;
import br.com.brisabr.helpdesk_api.ticket.TicketSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private TicketRepository ticketRepository;

    public List<RelatorioAnalistaDTO> getChamadosPorAnalista(Integer ano, Integer mes) {
        return ticketRepository.getChamadosPorAnalista(ano, mes);
    }

    public List<RelatorioCategoriaDTO> getTempoMedioPorCategoria(Integer ano, Integer mes) {
        return ticketRepository.getTempoMedioPorCategoria(ano, mes);
    }

    public List<RelatorioMensalDTO> getChamadosPorMes(Integer ano) {
        return ticketRepository.getChamadosPorMes(ano);
    }
    
    public List<Ticket> getDetailedReport(
            LocalDate dataInicial, LocalDate dataFinal, String tipoData, String status,
            String categoria, String unidade, String local, Long solicitanteId, Long tecnicoId, String ordenarPor) {
        
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
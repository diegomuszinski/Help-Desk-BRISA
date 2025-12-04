package br.com.brisabr.helpdesk_api.controller;

import br.com.brisabr.helpdesk_api.dto.RelatorioAnalistaDTO;
import br.com.brisabr.helpdesk_api.dto.RelatorioCategoriaDTO;
import br.com.brisabr.helpdesk_api.dto.RelatorioMensalDTO;
import br.com.brisabr.helpdesk_api.service.ReportService;
import br.com.brisabr.helpdesk_api.ticket.Ticket;
import br.com.brisabr.helpdesk_api.ticket.TicketResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/by-analyst")
    public ResponseEntity<List<RelatorioAnalistaDTO>> getChamadosPorAnalista(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(reportService.getChamadosPorAnalista(year, month));
    }

    @GetMapping("/by-category")
    public ResponseEntity<List<RelatorioCategoriaDTO>> getTempoMedioPorCategoria(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(reportService.getTempoMedioPorCategoria(year, month));
    }

    @GetMapping("/by-month")
    public ResponseEntity<List<RelatorioMensalDTO>> getChamadosPorMes(
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(reportService.getChamadosPorMes(year));
    }

    
    @GetMapping("/detailed")
    public ResponseEntity<List<TicketResponseDTO>> getDetailedReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(defaultValue = "abertura") String tipoData,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String unidade,
            @RequestParam(required = false) String local,
            @RequestParam(required = false) Long solicitanteId,
            @RequestParam(required = false) Long tecnicoId,
            @RequestParam(defaultValue = "numero") String ordenarPor
    ) {
        List<Ticket> tickets = reportService.getDetailedReport(
                dataInicial, dataFinal, tipoData, status, categoria,
                unidade, local, solicitanteId, tecnicoId, ordenarPor
        );
        List<TicketResponseDTO> response = tickets.stream().map(TicketResponseDTO::new).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
package br.com.brisabr.helpdesk_api.controller;

import br.com.brisabr.helpdesk_api.dto.DashboardStatsDTO;
import br.com.brisabr.helpdesk_api.ticket.TicketService; 
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST para dashboard e estatísticas do sistema.
 * 
 * Endpoints restritos a ADMIN e MANAGER.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final TicketService ticketService;

    public DashboardController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * Obtém estatísticas gerais do dashboard.
     * 
     * Retorna métricas agregadas como total de chamados, chamados abertos,
     * em andamento, resolvidos, tempo médio de resolução, etc.
     * 
     * @return Estatísticas do dashboard
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(ticketService.getDashboardStats());
    }
}
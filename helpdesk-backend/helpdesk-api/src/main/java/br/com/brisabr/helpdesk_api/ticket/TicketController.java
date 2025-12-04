package br.com.brisabr.helpdesk_api.ticket;

import br.com.brisabr.helpdesk_api.user.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @PostMapping("/{id}/reopen")
    public ResponseEntity<TicketResponseDTO> reopenTicket(@PathVariable Long id, @RequestBody @Valid TicketReopenDTO data, @AuthenticationPrincipal User user) {
        TicketResponseDTO updatedTicket = ticketService.reopenTicket(id, data, user);
        return ResponseEntity.ok(updatedTicket);
    }

    /**
     * Lista todos os tickets com paginação.
     * 
     * @param pageable Parâmetros de paginação (page, size, sort)
     * @param user Usuário autenticado
     * @return Página de tickets conforme permissão do usuário
     * 
     * Exemplos:
     * - GET /api/tickets?page=0&size=10
     * - GET /api/tickets?page=0&size=20&sort=dataAbertura,desc
     * - GET /api/tickets?page=1&size=15&sort=prioridade.nome,asc&sort=dataAbertura,desc
     */
    @GetMapping("/paginated")
    public ResponseEntity<Page<TicketResponseDTO>> getAllTicketsPaginated(
            @PageableDefault(size = 20, sort = "dataAbertura", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ticketService.getAllTicketsPaginated(pageable, user));
    }

    /**
     * Lista todos os tickets (sem paginação - mantido para compatibilidade).
     * 
     * @deprecated Use {@link #getAllTicketsPaginated(Pageable, User)} para melhor performance
     */
    @GetMapping
    @Deprecated
    public ResponseEntity<List<TicketResponseDTO>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.findTicketById(id));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<TicketResponseDTO> createTicket(
            @RequestPart("ticket") @Valid TicketCreateDTO data,
            @RequestPart(value = "anexos", required = false) List<MultipartFile> anexos,
            @AuthenticationPrincipal User solicitante) {
        try {
            Ticket savedTicket = ticketService.createTicket(data, solicitante, anexos);
            return ResponseEntity.created(URI.create("/api/tickets/" + savedTicket.getId())).body(new TicketResponseDTO(savedTicket));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<HistoricoItemDTO> addComment(@PathVariable Long id, @RequestBody @Valid CommentCreateDTO data, @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.addComment(id, data, user));
    }

    @PostMapping("/{id}/assign-self")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN', 'MANAGER')")
    public ResponseEntity<TicketResponseDTO> assignToSelf(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ticketService.assignTicketToSelf(id, user));
    }

    @PostMapping("/{ticketId}/assign/{technicianId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TicketResponseDTO> assignToTechnician(
            @PathVariable Long ticketId,
            @PathVariable Long technicianId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ticketService.assignTicketToTechnician(ticketId, technicianId, user));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN', 'MANAGER')")
    public ResponseEntity<TicketResponseDTO> closeTicket(@PathVariable Long id, @RequestBody @Valid CloseTicketDTO data, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ticketService.closeTicket(id, data, user));
    }
}
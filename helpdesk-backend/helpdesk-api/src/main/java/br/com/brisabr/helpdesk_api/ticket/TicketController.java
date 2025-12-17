package br.com.brisabr.helpdesk_api.ticket;

import br.com.brisabr.helpdesk_api.ratelimit.RateLimit;
import br.com.brisabr.helpdesk_api.user.User;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * Controller REST para gerenciamento de tickets/chamados.
 *
 * Endpoints principais:
 * - GET /api/tickets - Lista tickets com paginação
 * - GET /api/tickets/{id} - Busca ticket por ID
 * - POST /api/tickets - Cria novo ticket
 * - POST /api/tickets/{id}/comments - Adiciona comentário
 * - POST /api/tickets/{id}/assign-self - Atribui ticket para si mesmo
 * - POST /api/tickets/{id}/assign/{technicianId} - Atribui para técnico
 * - POST /api/tickets/{id}/close - Fecha ticket
 * - POST /api/tickets/{id}/reopen - Reabre ticket
 *
 * @author HelpDesk Team
 */
@RestController
@RequestMapping("/api/tickets")
@SuppressWarnings("null")
public class TicketController {

    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * Reabre um ticket fechado.
     *
     * @param id ID do ticket
     * @param data Dados para reabertura (motivo)
     * @param user Usuário autenticado
     * @return Ticket reaberto
     */
    @PostMapping("/{id}/reopen")
    public ResponseEntity<TicketResponseDTO> reopenTicket(
            @PathVariable Long id,
            @RequestBody @Valid TicketReopenDTO data,
            @AuthenticationPrincipal User user) {
        logger.info("Reabertura de ticket solicitada: ID={}, usuário={}", id, user.getEmail());
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
    @GetMapping
    public ResponseEntity<Page<TicketResponseDTO>> getAllTicketsPaginated(
            @PageableDefault(size = 20, sort = "dataAbertura", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User user) {
        logger.debug("Listando tickets paginados: page={}, size={}, usuário={}",
                    pageable.getPageNumber(), pageable.getPageSize(), user.getEmail());
        return ResponseEntity.ok(ticketService.getAllTicketsPaginated(pageable, user));
    }

    /**
     * Busca um ticket específico por ID.
     *
     * @param id ID do ticket
     * @return Dados completos do ticket
     */
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> getTicketById(@PathVariable Long id) {
        logger.debug("Buscando ticket por ID: {}", id);
        return ResponseEntity.ok(ticketService.findTicketById(id));
    }

    /**
     * Cria um novo ticket com anexos opcionais.
     *
     * @param data Dados do ticket
     * @param anexos Lista de arquivos anexos (opcional)
     * @param solicitante Usuário solicitante
     * @return Ticket criado com status 201
     */
    @RateLimit(requestsPerMinute = 30, type = RateLimit.LimitType.PER_IP)
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<TicketResponseDTO> createTicket(
            @RequestPart("ticket") @Valid TicketCreateDTO data,
            @RequestPart(value = "anexos", required = false) List<MultipartFile> anexos,
            @AuthenticationPrincipal User solicitante) {
        try {
            int anexosCount = anexos != null ? anexos.size() : 0;
            logger.info("Criando ticket: categoria={}, prioridade={}, anexos={}, solicitante={}",
                       data.getCategory(), data.getPriority(), anexosCount, solicitante.getEmail());

            Ticket savedTicket = ticketService.createTicket(data, solicitante, anexos);
            return ResponseEntity.created(URI.create("/api/tickets/" + savedTicket.getId()))
                                .body(new TicketResponseDTO(savedTicket));
        } catch (IOException e) {
            logger.error("Erro ao processar anexos do ticket", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Adiciona um comentário ao histórico do ticket.
     *
     * @param id ID do ticket
     * @param data Dados do comentário
     * @param user Usuário autenticado
     * @return Entrada de histórico criada
     */
    @RateLimit(requestsPerMinute = 60, type = RateLimit.LimitType.PER_USER)
    @PostMapping("/{id}/comments")
    public ResponseEntity<HistoricoItemDTO> addComment(
            @PathVariable Long id,
            @RequestBody @Valid CommentCreateDTO data,
            @AuthenticationPrincipal User user) {
        logger.info("Adicionando comentário ao ticket: ID={}, usuário={}", id, user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.addComment(id, data, user));
    }

    /**
     * Atribui o ticket para o próprio usuário autenticado.
     * Requer permissão de TECHNICIAN, ADMIN ou MANAGER.
     *
     * @param id ID do ticket
     * @param user Usuário autenticado
     * @return Ticket atualizado
     */
    @PostMapping("/{id}/assign-self")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN', 'MANAGER')")
    public ResponseEntity<TicketResponseDTO> assignToSelf(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        logger.info("Auto-atribuição de ticket: ID={}, técnico={}", id, user.getEmail());
        return ResponseEntity.ok(ticketService.assignTicketToSelf(id, user));
    }

    /**
     * Atribui o ticket para um técnico específico.
     * Requer permissão de ADMIN ou MANAGER.
     *
     * @param ticketId ID do ticket
     * @param technicianId ID do técnico
     * @param user Usuário autenticado (gerente/admin)
     * @return Ticket atualizado
     */
    @PostMapping("/{ticketId}/assign/{technicianId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TicketResponseDTO> assignToTechnician(
            @PathVariable Long ticketId,
            @PathVariable Long technicianId,
            @AuthenticationPrincipal User user) {
        logger.info("Atribuição de ticket: ID={}, técnico={}, atribuidor={}",
                   ticketId, technicianId, user.getEmail());
        return ResponseEntity.ok(ticketService.assignTicketToTechnician(ticketId, technicianId, user));
    }

    /**
     * Fecha um ticket com solução.
     * Requer permissão de TECHNICIAN, ADMIN ou MANAGER.
     *
     * @param id ID do ticket
     * @param data Dados de fechamento (solução)
     * @param user Usuário autenticado
     * @return Ticket fechado
     */
    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN', 'MANAGER')")
    public ResponseEntity<TicketResponseDTO> closeTicket(
            @PathVariable Long id,
            @RequestBody @Valid CloseTicketDTO data,
            @AuthenticationPrincipal User user) {
        logger.info("Fechamento de ticket: ID={}, técnico={}", id, user.getEmail());
        return ResponseEntity.ok(ticketService.closeTicket(id, data, user));
    }
}

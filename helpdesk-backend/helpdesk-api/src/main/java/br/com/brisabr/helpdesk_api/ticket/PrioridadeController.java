package br.com.brisabr.helpdesk_api.ticket;

import br.com.brisabr.helpdesk_api.dto.PrioridadeCreateDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Controller para gerenciamento de prioridades de tickets.
 *
 * Endpoints disponíveis:
 * - GET /api/prioridades - Lista todas as prioridades
 * - POST /api/prioridades - Cria nova prioridade (ADMIN)
 *
 * @author HelpDesk Team
 */
@RestController
@RequestMapping("/api/prioridades")
public class PrioridadeController {

    private final PrioridadeService prioridadeService;

    public PrioridadeController(PrioridadeService prioridadeService) {
        this.prioridadeService = prioridadeService;
    }

    /**
     * Lista todas as prioridades disponíveis.
     *
     * @return Lista de prioridades
     */
    @GetMapping
    public ResponseEntity<List<Prioridade>> getAllPrioridades() {
        return ResponseEntity.ok(prioridadeService.findAll());
    }

    /**
     * Cria uma nova prioridade.
     * Requer permissão de ADMIN.
     *
     * @param dto Dados da prioridade
     * @return Prioridade criada com status 201
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Prioridade> createPrioridade(@RequestBody @Valid PrioridadeCreateDTO dto) {
        Prioridade saved = prioridadeService.create(dto);
        return ResponseEntity.created(URI.create("/api/prioridades/" + saved.getId())).body(saved);
    }
}

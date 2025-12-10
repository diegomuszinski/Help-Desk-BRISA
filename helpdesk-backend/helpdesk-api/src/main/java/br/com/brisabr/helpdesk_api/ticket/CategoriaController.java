package br.com.brisabr.helpdesk_api.ticket;

import br.com.brisabr.helpdesk_api.dto.CategoriaCreateDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Controller para gerenciamento de categorias de tickets.
 *
 * Endpoints disponíveis:
 * - GET /api/categorias - Lista todas as categorias
 * - POST /api/categorias - Cria nova categoria (ADMIN)
 *
 * @author HelpDesk Team
 */
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    /**
     * Lista todas as categorias disponíveis.
     *
     * @return Lista de categorias
     */
    @GetMapping
    public ResponseEntity<List<Categoria>> getAllCategorias() {
        return ResponseEntity.ok(categoriaService.findAll());
    }

    /**
     * Cria uma nova categoria.
     * Requer permissão de ADMIN.
     *
     * @param dto Dados da categoria
     * @return Categoria criada com status 201
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Categoria> createCategoria(@RequestBody @Valid CategoriaCreateDTO dto) {
        Categoria saved = categoriaService.create(dto);
        return ResponseEntity.created(URI.create("/api/categorias/" + saved.getId())).body(saved);
    }
}

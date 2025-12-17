package br.com.brisabr.helpdesk_api.ticket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de categorias de tickets.
 *
 * Responsável pela lógica de negócio relacionada a categorias,
 * incluindo criação, listagem e validações.
 *
 * @author HelpDesk Team
 */
@Service
@SuppressWarnings("null")
public class CategoriaService {

    private static final Logger logger = LoggerFactory.getLogger(CategoriaService.class);

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    /**
     * Lista todas as categorias disponíveis.
     * Resultado é cacheado para melhor performance.
     *
     * @return Lista de todas as categorias
     */
    @Cacheable("categorias")
    @Transactional(readOnly = true)
    public List<Categoria> findAll() {
        logger.debug("Listando todas as categorias (cache miss)");
        return categoriaRepository.findAll();
    }

    /**
     * Cria uma nova categoria a partir de DTO.
     * Limpa o cache após criação.
     *
     * @param dto Dados da categoria
     * @return Categoria criada com ID gerado
     * @throws RuntimeException se já existir categoria com o mesmo nome
     */
    @CacheEvict(value = "categorias", allEntries = true)
    @Transactional
    public Categoria create(br.com.brisabr.helpdesk_api.dto.CategoriaCreateDTO dto) {
        logger.info("Criando nova categoria: {}", dto.nome());

        // Verificar duplicidade
        if (categoriaRepository.existsByNome(dto.nome())) {
            logger.warn("Tentativa de criar categoria duplicada: {}", dto.nome());
            throw new RuntimeException("Categoria com nome '" + dto.nome() + "' já existe");
        }

        Categoria categoria = new Categoria();
        categoria.setNome(dto.nome());

        Categoria saved = categoriaRepository.save(categoria);
        logger.info("Categoria criada com sucesso: {} (ID: {})", saved.getNome(), saved.getId());

        return saved;
    }    /**
     * Busca categoria por ID.
     *
     * @param id ID da categoria
     * @return Categoria encontrada
     * @throws RuntimeException se categoria não for encontrada
     */
    @Transactional(readOnly = true)
    public Categoria findById(Long id) {
        logger.debug("Buscando categoria por ID: {}", id);
        return categoriaRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Categoria não encontrada: {}", id);
                    return new RuntimeException("Categoria não encontrada: " + id);
                });
    }
}

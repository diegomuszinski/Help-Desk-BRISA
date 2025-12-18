package br.com.brisabr.helpdesk_api.ticket;

import br.com.brisabr.helpdesk_api.exception.DuplicateResourceException;
import br.com.brisabr.helpdesk_api.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de prioridades de tickets.
 *
 * Responsável pela lógica de negócio relacionada a prioridades,
 * incluindo criação, listagem e validações.
 *
 * @author HelpDesk Team
 */
@Service
@SuppressWarnings("null")
public class PrioridadeService {

    private static final Logger logger = LoggerFactory.getLogger(PrioridadeService.class);

    private final PrioridadeRepository prioridadeRepository;

    public PrioridadeService(PrioridadeRepository prioridadeRepository) {
        this.prioridadeRepository = prioridadeRepository;
    }

    /**
     * Lista todas as prioridades disponíveis.
     * Resultado é cacheado para melhor performance.
     *
     * @return Lista de todas as prioridades
     */
    @Cacheable("prioridades")
    @Transactional(readOnly = true)
    public List<Prioridade> findAll() {
        logger.debug("Listando todas as prioridades (cache miss)");
        return prioridadeRepository.findAll();
    }

    /**
     * Cria uma nova prioridade a partir de DTO.
     * Limpa o cache após criação.
     *
     * @param dto Dados da prioridade
     * @return Prioridade criada com ID gerado
     * @throws RuntimeException se já existir prioridade com o mesmo nome
     */
    @CacheEvict(value = "prioridades", allEntries = true)
    @Transactional
    public Prioridade create(br.com.brisabr.helpdesk_api.dto.PrioridadeCreateDTO dto) {
        logger.info("Criando nova prioridade: {}", dto.nome());

        // Verificar duplicidade
        if (prioridadeRepository.existsByNome(dto.nome())) {
            logger.warn("Tentativa de criar prioridade duplicada: {}", dto.nome());
            throw new DuplicateResourceException("Prioridade com nome '" + dto.nome() + "' já existe");
        }

        Prioridade prioridade = new Prioridade();
        prioridade.setNome(dto.nome());

        Prioridade saved = prioridadeRepository.save(prioridade);
        logger.info("Prioridade criada com sucesso: {} (ID: {})", saved.getNome(), saved.getId());

        return saved;
    }    /**
     * Busca prioridade por ID.
     *
     * @param id ID da prioridade
     * @return Prioridade encontrada
     * @throws RuntimeException se prioridade não for encontrada
     */
    @Transactional(readOnly = true)
    public Prioridade findById(Long id) {
        logger.debug("Buscando prioridade por ID: {}", id);
        return prioridadeRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Prioridade não encontrada: {}", id);
                    return new ResourceNotFoundException("Prioridade não encontrada: " + id);
                });
    }
}

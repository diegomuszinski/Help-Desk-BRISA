package br.com.brisabr.helpdesk_api.ticket;

import br.com.brisabr.helpdesk_api.dto.DashboardStatsDTO;
import br.com.brisabr.helpdesk_api.user.User;
import br.com.brisabr.helpdesk_api.user.UserRepository;
import br.com.brisabr.helpdesk_api.util.FileValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private HistoricoChamadoRepository historicoChamadoRepository;

    @Autowired
    private AnexoChamadoRepository anexoChamadoRepository;

    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FileValidator fileValidator;

    @Transactional(readOnly = true)
    public AnexoChamado getAnexoById(Long id) {
        return anexoChamadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Anexo não encontrado com o ID: " + id));
    }

    @Transactional
    public Ticket createTicket(TicketCreateDTO data, User solicitante, List<MultipartFile> anexos) throws IOException {
        // Validar arquivos ANTES de processar
        fileValidator.validateFiles(anexos);
        
        Ticket newTicket = new Ticket();
        long countThisYear = ticketRepository.countByYear(Year.now().getValue());
        String numeroChamado = Year.now().getValue() + "-" + "%03d".formatted(countThisYear + 1);

        newTicket.setNumeroChamado(numeroChamado);
        newTicket.setDescricao(data.getDescription());
        newTicket.setCategoria(data.getCategory());
        newTicket.setPrioridade(data.getPriority());
        newTicket.setStatus("Aberto");
        newTicket.setSolicitante(solicitante);
        newTicket.setDataAbertura(LocalDateTime.now());
        if (anexos != null && !anexos.isEmpty()) {
            for (MultipartFile anexo : anexos) {
                if (anexo.isEmpty()) continue;
                AnexoChamado anexoChamado = new AnexoChamado();
                anexoChamado.setNomeArquivo(anexo.getOriginalFilename());
                anexoChamado.setTipoArquivo(anexo.getContentType());
                anexoChamado.setDados(Base64.getEncoder().encodeToString(anexo.getBytes()));
                anexoChamado.setTicket(newTicket);
                newTicket.getAnexos().add(anexoChamado);
            }
        }

        Ticket savedTicket = ticketRepository.saveAndFlush(newTicket);
        createHistoryEntry(savedTicket, solicitante, "Chamado criado.");

        return ticketRepository.findByIdWithAnexos(savedTicket.getId()).orElse(savedTicket);
    }

    @Transactional(readOnly = true)
    public TicketResponseDTO findTicketById(Long id) {
        Ticket ticket = ticketRepository.findByIdWithAnexos(id)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado com o ID: " + id));
        return new TicketResponseDTO(ticket);
    }

    @Transactional
    public TicketResponseDTO reopenTicket(Long ticketId, TicketReopenDTO data, User currentUser) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Chamado não encontrado"));
        if (!Objects.equals(ticket.getSolicitante().getId(), currentUser.getId())) {
            throw new AccessDeniedException("Apenas o solicitante do chamado pode reabri-lo.");
        }
        if (!List.of("Resolvido", "Encerrado", "Fechado").contains(ticket.getStatus())) {
            throw new RuntimeException("Apenas chamados finalizados podem ser reabertos.");
        }
        ticket.setStatus("Aberto");
        ticket.setFoiReaberto(true);
        ticket.setDataFechamento(null);
        ticket.setSolucao(null);
        Ticket updatedTicket = ticketRepository.save(ticket);
        createHistoryEntry(updatedTicket, currentUser, "Chamado reaberto. Motivo: " + data.getMotivo());
        return new TicketResponseDTO(ticket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponseDTO> getAllTickets() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        List<Ticket> tickets;
        String perfil = user.getPerfil().toLowerCase(); // Converter para minúsculo para comparação
        switch (perfil) {
            case "admin":
            case "manager":
            case "technician":
                tickets = ticketRepository.findAll();
                break;
            case "user":
                tickets = ticketRepository.findAllBySolicitanteId(user.getId());
                break;
            default:
                tickets = List.of();
                break;
        }
        
        return tickets.stream().map(TicketResponseDTO::new).collect(Collectors.toList());
    }

    @Transactional
    public HistoricoItemDTO addComment(Long ticketId, CommentCreateDTO data, User autor) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Chamado não encontrado"));
        HistoricoChamado novoHistorico = new HistoricoChamado();
        novoHistorico.setTicket(ticket);
        novoHistorico.setAutor(autor);
        novoHistorico.setComentario(data.getComentario());
        HistoricoChamado historicoSalvo = historicoChamadoRepository.save(novoHistorico);
        return new HistoricoItemDTO(historicoSalvo);
    }

    @Transactional
    public TicketResponseDTO assignTicketToSelf(Long ticketId, User currentUser) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Chamado não encontrado"));
        if (!"Aberto".equals(ticket.getStatus())) {
            throw new RuntimeException("Este chamado não está mais aberto para captura.");
        }
        ticket.setAtribuido(currentUser);
        ticket.setStatus("Em Andamento");
        Ticket updatedTicket = ticketRepository.save(ticket);
        createHistoryEntry(updatedTicket, currentUser, "Chamado atribuído a " + currentUser.getNome() + ".");
        return new TicketResponseDTO(ticket);
    }
    
    
    @Transactional
    public TicketResponseDTO assignTicketToTechnician(Long ticketId, Long technicianId, User currentUser) {
        // 1. Validação de permissão
        if (!"admin".equals(currentUser.getPerfil()) && !"manager".equals(currentUser.getPerfil())) {
            throw new AccessDeniedException("Apenas administradores ou gestores podem atribuir chamados.");
        }

        
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado"));

        
        if (!"Aberto".equals(ticket.getStatus())) {
            throw new RuntimeException("Este chamado não está mais aberto para atribuição.");
        }

        
        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("Técnico não encontrado"));

        
        ticket.setAtribuido(technician);
        ticket.setStatus("Em Andamento");
        Ticket updatedTicket = ticketRepository.save(ticket);

        
        String assignerName = currentUser.getNome();
        createHistoryEntry(updatedTicket, currentUser, "Chamado atribuído para " + technician.getNome() + " por " + assignerName + ".");

        return new TicketResponseDTO(updatedTicket);
    }

    @Transactional
    public TicketResponseDTO closeTicket(Long ticketId, CloseTicketDTO data, User currentUser) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Chamado não encontrado"));
        boolean isAdminOrManager = "admin".equals(currentUser.getPerfil()) || "manager".equals(currentUser.getPerfil());
        boolean isOwner = ticket.getAtribuido() != null && Objects.equals(ticket.getAtribuido().getId(), currentUser.getId());
        if (!isAdminOrManager && !isOwner) {
            throw new AccessDeniedException("Apenas o técnico responsável ou um gestor pode encerrar o chamado.");
        }
        ticket.setSolucao(data.getSolucao());
        ticket.setStatus("Resolvido");
        ticket.setDataFechamento(LocalDateTime.now());
        Ticket updatedTicket = ticketRepository.save(ticket);
        createHistoryEntry(updatedTicket, currentUser, "Chamado Resolvido. Solução: " + data.getSolucao());
        return new TicketResponseDTO(ticket);
    }

    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        stats.setChamadosNaFila(ticketRepository.countByStatusAndAtribuidoIsNull("Aberto"));
        stats.setChamadosPorAnalista(ticketRepository.getChamadosPorAnalista(null, null));

        List<Ticket> activeTickets = ticketRepository.findAll().stream()
                .filter(ticket -> List.of("Aberto", "Em Andamento").contains(ticket.getStatus()))
                .toList();
        LocalDateTime now = LocalDateTime.now();
        List<TicketResponseDTO> slaViolatedTickets = activeTickets.stream()
                .filter(ticket -> {
                    LocalDateTime slaDeadline = Ticket.calculateSlaDeadline(ticket.getDataAbertura(), ticket.getPrioridade());
                    return now.isAfter(slaDeadline);
                })
                .map(TicketResponseDTO::new)
                .collect(Collectors.toList());
        stats.setChamadosSlaViolado(slaViolatedTickets);
        return stats;
    }

    private void createHistoryEntry(Ticket ticket, User autor, String comentario) {
        HistoricoChamado historico = new HistoricoChamado();
        historico.setTicket(ticket);
        historico.setAutor(autor);
        historico.setComentario(comentario);
        historicoChamadoRepository.save(historico);
    }
}
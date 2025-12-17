package br.com.brisabr.helpdesk_api.ticket;

import br.com.brisabr.helpdesk_api.exception.AttachmentNotFoundException;
import br.com.brisabr.helpdesk_api.exception.InvalidTicketStateException;
import br.com.brisabr.helpdesk_api.exception.TicketNotFoundException;
import br.com.brisabr.helpdesk_api.exception.UnauthorizedOperationException;
import br.com.brisabr.helpdesk_api.user.User;
import br.com.brisabr.helpdesk_api.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para TicketService.
 *
 * Utiliza Mockito para isolar as dependências e testar apenas a lógica do serviço.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TicketService - Testes Unitários")
@SuppressWarnings("null")
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private HistoricoChamadoRepository historicoChamadoRepository;

    @Mock
    private AnexoChamadoRepository anexoChamadoRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TicketService ticketService;

    private User adminUser;
    private User technicianUser;
    private User normalUser;
    private Ticket openTicket;
    private Ticket resolvedTicket;

    @BeforeEach
    void setUp() {
        // Setup usuários
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setNome("Admin");
        adminUser.setEmail("admin@test.com");
        adminUser.setPerfil("admin");

        technicianUser = new User();
        technicianUser.setId(2L);
        technicianUser.setNome("Técnico");
        technicianUser.setEmail("tech@test.com");
        technicianUser.setPerfil("technician");

        normalUser = new User();
        normalUser.setId(3L);
        normalUser.setNome("Usuário");
        normalUser.setEmail("user@test.com");
        normalUser.setPerfil("user");

        // Setup tickets
        openTicket = new Ticket();
        openTicket.setId(1L);
        openTicket.setStatus("Aberto");
        openTicket.setSolicitante(normalUser);

        resolvedTicket = new Ticket();
        resolvedTicket.setId(2L);
        resolvedTicket.setStatus("Resolvido");
        resolvedTicket.setSolicitante(normalUser);
    }

    @Test
    @DisplayName("Deve encontrar ticket por ID com sucesso")
    void shouldFindTicketById() {
        // Given
        when(ticketRepository.findByIdWithAnexos(1L)).thenReturn(Optional.of(openTicket));

        // When
        TicketResponseDTO result = ticketService.findTicketById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(ticketRepository).findByIdWithAnexos(1L);
    }

    @Test
    @DisplayName("Deve lançar TicketNotFoundException quando ticket não existe")
    void shouldThrowTicketNotFoundExceptionWhenTicketDoesNotExist() {
        // Given
        when(ticketRepository.findByIdWithAnexos(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ticketService.findTicketById(999L))
                .isInstanceOf(TicketNotFoundException.class)
                .hasMessageContaining("999");

        verify(ticketRepository).findByIdWithAnexos(999L);
    }

    @Test
    @DisplayName("Deve atribuir ticket para si mesmo com sucesso")
    void shouldAssignTicketToSelf() {
        // Given
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(openTicket);

        // When
        TicketResponseDTO result = ticketService.assignTicketToSelf(1L, technicianUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(openTicket.getAtribuido()).isEqualTo(technicianUser);
        assertThat(openTicket.getStatus()).isEqualTo("Em Andamento");
        verify(ticketRepository).findById(1L);
        verify(ticketRepository).save(openTicket);
        verify(historicoChamadoRepository).save(any(HistoricoChamado.class));
    }

    @Test
    @DisplayName("Deve lançar InvalidTicketStateException ao tentar atribuir ticket que não está aberto")
    void shouldThrowInvalidTicketStateExceptionWhenAssignNonOpenTicket() {
        // Given
        resolvedTicket.setStatus("Resolvido");
        when(ticketRepository.findById(2L)).thenReturn(Optional.of(resolvedTicket));

        // When & Then
        assertThatThrownBy(() -> ticketService.assignTicketToSelf(2L, technicianUser))
                .isInstanceOf(InvalidTicketStateException.class)
                .hasMessageContaining("não está mais aberto");

        verify(ticketRepository).findById(2L);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve reabrir ticket finalizado com sucesso")
    void shouldReopenResolvedTicket() {
        // Given
        TicketReopenDTO reopenData = new TicketReopenDTO();
        reopenData.setMotivo("Problema não foi resolvido");

        when(ticketRepository.findById(2L)).thenReturn(Optional.of(resolvedTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(resolvedTicket);

        // When
        TicketResponseDTO result = ticketService.reopenTicket(2L, reopenData, normalUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(resolvedTicket.getStatus()).isEqualTo("Aberto");
        assertThat(resolvedTicket.isFoiReaberto()).isTrue();
        verify(ticketRepository).findById(2L);
        verify(ticketRepository).save(resolvedTicket);
        verify(historicoChamadoRepository).save(any(HistoricoChamado.class));
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedOperationException quando usuário não é o solicitante")
    void shouldThrowUnauthorizedOperationExceptionWhenUserIsNotRequester() {
        // Given
        TicketReopenDTO reopenData = new TicketReopenDTO();
        reopenData.setMotivo("Motivo");

        when(ticketRepository.findById(2L)).thenReturn(Optional.of(resolvedTicket));

        // When & Then
        assertThatThrownBy(() -> ticketService.reopenTicket(2L, reopenData, technicianUser))
                .isInstanceOf(UnauthorizedOperationException.class)
                .hasMessageContaining("solicitante");

        verify(ticketRepository).findById(2L);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar InvalidTicketStateException ao reabrir ticket que não está finalizado")
    void shouldThrowInvalidTicketStateExceptionWhenReopenNonFinalizedTicket() {
        // Given
        TicketReopenDTO reopenData = new TicketReopenDTO();
        reopenData.setMotivo("Motivo");

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));

        // When & Then
        assertThatThrownBy(() -> ticketService.reopenTicket(1L, reopenData, normalUser))
                .isInstanceOf(InvalidTicketStateException.class)
                .hasMessageContaining("finalizados");

        verify(ticketRepository).findById(1L);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar anexo por ID com sucesso")
    void shouldGetAnexoById() {
        // Given
        AnexoChamado anexo = new AnexoChamado();
        anexo.setId(1L);
        anexo.setNomeArquivo("test.pdf");

        when(anexoChamadoRepository.findById(1L)).thenReturn(Optional.of(anexo));

        // When
        AnexoChamado result = ticketService.getAnexoById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNomeArquivo()).isEqualTo("test.pdf");
        verify(anexoChamadoRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar AttachmentNotFoundException quando anexo não existe")
    void shouldThrowAttachmentNotFoundExceptionWhenAttachmentDoesNotExist() {
        // Given
        when(anexoChamadoRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ticketService.getAnexoById(999L))
                .isInstanceOf(AttachmentNotFoundException.class)
                .hasMessageContaining("999");

        verify(anexoChamadoRepository).findById(999L);
    }

    @Test
    @DisplayName("Deve adicionar comentário ao ticket com sucesso")
    void shouldAddCommentToTicket() {
        // Given
        CommentCreateDTO commentData = new CommentCreateDTO();
        commentData.setComentario("Comentário de teste");

        HistoricoChamado historico = new HistoricoChamado();
        historico.setId(1L);
        historico.setComentario("Comentário de teste");
        historico.setAutor(adminUser);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));
        when(historicoChamadoRepository.save(any(HistoricoChamado.class))).thenReturn(historico);

        // When
        HistoricoItemDTO result = ticketService.addComment(1L, commentData, adminUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getComentario()).isEqualTo("Comentário de teste");
        verify(ticketRepository).findById(1L);
        verify(historicoChamadoRepository).save(any(HistoricoChamado.class));
    }

    @Test
    @DisplayName("Deve fechar ticket com sucesso quando usuário é o responsável")
    void shouldCloseTicketWhenUserIsOwner() {
        // Given
        CloseTicketDTO closeData = new CloseTicketDTO();
        closeData.setSolucao("Problema resolvido");

        openTicket.setAtribuido(technicianUser);
        openTicket.setStatus("Em Andamento");

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(openTicket);

        // When
        TicketResponseDTO result = ticketService.closeTicket(1L, closeData, technicianUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(openTicket.getStatus()).isEqualTo("Resolvido");
        assertThat(openTicket.getSolucao()).isEqualTo("Problema resolvido");
        assertThat(openTicket.getDataFechamento()).isNotNull();
        verify(ticketRepository).findById(1L);
        verify(ticketRepository).save(openTicket);
    }

    @Test
    @DisplayName("Deve permitir admin fechar qualquer ticket")
    void shouldAllowAdminToCloseAnyTicket() {
        // Given
        CloseTicketDTO closeData = new CloseTicketDTO();
        closeData.setSolucao("Resolvido pelo admin");

        openTicket.setAtribuido(technicianUser);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(openTicket);

        // When
        TicketResponseDTO result = ticketService.closeTicket(1L, closeData, adminUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(openTicket.getStatus()).isEqualTo("Resolvido");
        verify(ticketRepository).save(openTicket);
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedOperationException quando usuário não pode fechar ticket")
    void shouldThrowUnauthorizedOperationExceptionWhenUserCannotCloseTicket() {
        // Given
        CloseTicketDTO closeData = new CloseTicketDTO();
        closeData.setSolucao("Tentando fechar");

        openTicket.setAtribuido(technicianUser);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));

        // When & Then
        assertThatThrownBy(() -> ticketService.closeTicket(1L, closeData, normalUser))
                .isInstanceOf(UnauthorizedOperationException.class)
                .hasMessageContaining("responsável ou um gestor");

        verify(ticketRepository).findById(1L);
        verify(ticketRepository, never()).save(any());
    }
}

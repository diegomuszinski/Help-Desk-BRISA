package br.com.brisabr.helpdesk_api.user;

import br.com.brisabr.helpdesk_api.ratelimit.RateLimit;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller REST para gerenciamento de usuários.
 *
 * Endpoints protegidos por permissões específicas.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Registra um novo usuário no sistema.
     *
     * Apenas administradores podem criar novos usuários.
     * A senha fornecida será automaticamente criptografada.
     *
     * @param registrationDTO Dados do novo usuário
     * @return Usuário criado
     * @throws IllegalArgumentException se email já existe
     */
    @RateLimit(requestsPerMinute = 20, type = RateLimit.LimitType.PER_IP)
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public User registerNewUser(@RequestBody @Valid UserRegistrationDTO registrationDTO) {
        return userService.registerUser(registrationDTO);
    }

    /**
     * Lista todos os técnicos disponíveis.
     *
     * Utilizado para atribuição de chamados.
     * Acesso permitido para ADMIN e MANAGER.
     *
     * @return Lista simplificada de técnicos (ID e nome)
     */
    @GetMapping("/technicians")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<List<UserSimpleDTO>> getTechnicians() {
        List<User> technicians = userService.findTechnicians();
        List<UserSimpleDTO> dtos = technicians.stream()
                .map(user -> new UserSimpleDTO(user.getId(), user.getNome()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}

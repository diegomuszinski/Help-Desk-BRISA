package br.com.brisabr.helpdesk_api.user;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public User registerNewUser(@RequestBody @Valid UserRegistrationDTO registrationDTO) {
        return userService.registerUser(registrationDTO);
    }

    
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
package br.com.brisabr.helpdesk_api.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para gerenciamento de usuários.
 *
 * Responsável por registro de novos usuários e consultas.
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra um novo usuário no sistema.
     *
     * A senha é automaticamente criptografada com BCrypt antes de salvar.
     *
     * @param registrationDTO Dados do usuário a ser registrado
     * @return Usuário criado
     * @throws IllegalArgumentException se email já está em uso
     */
    @Transactional
    public User registerUser(UserRegistrationDTO registrationDTO) {
        logger.info("Registrando novo usuário: {} - Perfil: {}",
                    registrationDTO.getEmail(), registrationDTO.getPerfil());

        // Validar email único
        if (userRepository.findByEmail(registrationDTO.getEmail()) != null) {
            logger.warn("Tentativa de registro com email já existente: {}", registrationDTO.getEmail());
            throw new IllegalArgumentException("Email já está em uso");
        }

        User newUser = new User();
        newUser.setNome(registrationDTO.getNome());
        newUser.setEmail(registrationDTO.getEmail());
        newUser.setSenha(passwordEncoder.encode(registrationDTO.getSenha()));
        newUser.setPerfil(registrationDTO.getPerfil());

        User savedUser = userRepository.save(newUser);
        logger.info("Usuário criado com sucesso: ID {}", savedUser.getId());
        return savedUser;
    }

    /**
     * Busca todos os usuários com perfil de técnico.
     *
     * @return Lista de técnicos
     */
    public List<User> findTechnicians() {
        logger.debug("Buscando lista de técnicos");
        List<User> technicians = userRepository.findByPerfil("technician");
        logger.debug("Encontrados {} técnicos", technicians.size());
        return technicians;
    }
}

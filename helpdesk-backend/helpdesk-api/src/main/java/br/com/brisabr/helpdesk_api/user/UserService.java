package br.com.brisabr.helpdesk_api.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(UserRegistrationDTO registrationDTO) {
        User newUser = new User();
        newUser.setNome(registrationDTO.getNome());
        newUser.setEmail(registrationDTO.getEmail());
        newUser.setSenha(passwordEncoder.encode(registrationDTO.getSenha()));
        newUser.setPerfil(registrationDTO.getPerfil());
        return userRepository.save(newUser);
    }

    public List<User> findTechnicians() {
        return userRepository.findByPerfil("technician");
    }
}
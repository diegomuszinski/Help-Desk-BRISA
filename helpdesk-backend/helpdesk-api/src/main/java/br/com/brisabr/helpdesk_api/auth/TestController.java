package br.com.brisabr.helpdesk_api.auth;

import br.com.brisabr.helpdesk_api.user.User;
import br.com.brisabr.helpdesk_api.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller de teste para desenvolvimento.
 * ATENÇÃO: Este controller só está disponível em ambiente de DESENVOLVIMENTO.
 * Em produção, este endpoint NÃO estará disponível.
 */
@RestController
@RequestMapping("/api/test")
@Profile("dev")  // ← Só ativo em desenvolvimento
public class TestController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping("/bcrypt")
    public Map<String, Object> testBCrypt(@RequestParam String email, @RequestParam String senha) {
        Map<String, Object> result = new HashMap<>();
        
        User user = (User) userRepository.findByEmail(email);
        
        if (user == null) {
            result.put("error", "Usuário não encontrado");
            return result;
        }
        
        String hashDoBanco = user.getSenha();
        boolean matches = passwordEncoder.matches(senha, hashDoBanco);
        
        result.put("email", email);
        result.put("senhaTestada", senha);
        result.put("hashDoBanco", hashDoBanco);
        result.put("hashLength", hashDoBanco.length());
        result.put("matches", matches);
        result.put("novoHash", passwordEncoder.encode(senha));
        
        return result;
    }
    
    @PostMapping("/reset-all-passwords")
    public Map<String, Object> resetAllPasswords() {
        Map<String, Object> result = new HashMap<>();
        
        // Senha padrão: 123456
        String senhaPadrao = "123456";
        String hashNovo = passwordEncoder.encode(senhaPadrao);
        
        // Atualizar todos os usuários
        User admin = (User) userRepository.findByEmail("admin@admin.net");
        User manager = (User) userRepository.findByEmail("sonia.lima@gestor.net");
        User tech = (User) userRepository.findByEmail("mariana@tecnico.net");
        User user = (User) userRepository.findByEmail("usuario@teste.net");
        
        int updated = 0;
        
        if (admin != null) {
            admin.setSenha(hashNovo);
            userRepository.save(admin);
            updated++;
        }
        
        if (manager != null) {
            manager.setSenha(hashNovo);
            userRepository.save(manager);
            updated++;
        }
        
        if (tech != null) {
            tech.setSenha(hashNovo);
            userRepository.save(tech);
            updated++;
        }
        
        if (user != null) {
            user.setSenha(hashNovo);
            userRepository.save(user);
            updated++;
        }
        
        result.put("success", true);
        result.put("message", "Senhas resetadas para: 123456");
        result.put("usuariosAtualizados", updated);
        result.put("hashGerado", hashNovo);
        
        return result;
    }
}

package br.com.brisabr.helpdesk_api.auth;

import br.com.brisabr.helpdesk_api.audit.AuditService;
import br.com.brisabr.helpdesk_api.ratelimit.RateLimiter;
import br.com.brisabr.helpdesk_api.user.User;
import br.com.brisabr.helpdesk_api.user.UserRegistrationDTO;
import br.com.brisabr.helpdesk_api.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private TokenService tokenService;

    @Autowired
    private RefreshTokenService refreshTokenService;
    
    @Autowired
    private RateLimiter rateLimiter;
    
    @Autowired
    private AuditService auditService;
    
    @Autowired
    private UserService userService;

    /**
     * Login com geração de access token + refresh token
     * Rate limited: 5 tentativas por minuto
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDTO data, HttpServletRequest request){
        // Verificar rate limit
        if (!rateLimiter.isAllowed(request)) {
            int remaining = rateLimiter.getRemainingAttempts(request);
            logger.warn("Rate limit excedido para login do email: {}", data.getEmail());
            auditService.logLoginFailure(data.getEmail(), "Rate limit excedido");
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Muitas tentativas de login. Tente novamente em 1 minuto. Tentativas restantes: " + remaining);
        }
        
        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(data.getEmail(), data.getSenha());
            var auth = this.authenticationManager.authenticate(usernamePassword);
            User user = (User) auth.getPrincipal();
            
            // Login bem-sucedido - resetar contador
            rateLimiter.resetAttempts(request);
            
            // Gerar access token (JWT)
            var accessToken = tokenService.generateToken(user);
            
            // Gerar refresh token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
            
            // Registrar auditoria
            auditService.logLogin(user);
            
            logger.info("Login realizado com sucesso: {}", user.getEmail());
            return ResponseEntity.ok(new TokenPairDTO(accessToken, refreshToken.getToken()));
        } catch (BadCredentialsException e) {
            // Registrar falha de login
            auditService.logLoginFailure(data.getEmail(), "Credenciais inválidas");
            throw e;
        }
    }

    /**
     * Renovar access token usando refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenPairDTO> refreshToken(@RequestBody @Valid RefreshTokenRequestDTO request) {
        // Validar refresh token
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());
        User user = refreshToken.getUser();
        
        // Gerar novo access token
        String newAccessToken = tokenService.generateToken(user);
        
        // Gerar novo refresh token (rotation)
        refreshTokenService.revokeRefreshToken(refreshToken.getToken());
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);
        
        logger.info("Tokens renovados para usuário: {}", user.getEmail());
        return ResponseEntity.ok(new TokenPairDTO(newAccessToken, newRefreshToken.getToken()));
    }

    /**
     * Logout (revoga refresh token)
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestBody RefreshTokenRequestDTO request,
            @AuthenticationPrincipal User user) {
        
        refreshTokenService.revokeRefreshToken(request.getRefreshToken());
        
        if (user != null) {
            auditService.logLogout(user);
            logger.info("Logout realizado: {}", user.getEmail());
        }
        
        return ResponseEntity.ok("Logout realizado com sucesso");
    }

    /**
     * Logout de todos os dispositivos (revoga todos os refresh tokens do usuário)
     */
    @PostMapping("/logout-all")
    public ResponseEntity<String> logoutAll(@AuthenticationPrincipal User user) {
        refreshTokenService.revokeAllUserTokens(user);
        auditService.logLogout(user);
        logger.info("Logout de todos os dispositivos: {}", user.getEmail());
        return ResponseEntity.ok("Logout realizado em todos os dispositivos");
    }
    
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid UserRegistrationDTO data) {
        try {
            userService.registerUser(data);
            return ResponseEntity.ok("Usuário registrado com sucesso!");
        } catch (RuntimeException e) {
            
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
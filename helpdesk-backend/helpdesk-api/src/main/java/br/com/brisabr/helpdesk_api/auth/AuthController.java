package br.com.brisabr.helpdesk_api.auth;

import br.com.brisabr.helpdesk_api.audit.AuditService;
import br.com.brisabr.helpdesk_api.ratelimit.RateLimiter;
import br.com.brisabr.helpdesk_api.user.User;
import br.com.brisabr.helpdesk_api.user.UserRegistrationDTO;
import br.com.brisabr.helpdesk_api.user.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final RateLimiter rateLimiter;
    private final AuditService auditService;
    private final UserService userService;

    public AuthController(
            AuthenticationManager authenticationManager,
            TokenService tokenService,
            RefreshTokenService refreshTokenService,
            RateLimiter rateLimiter,
            AuditService auditService,
            UserService userService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
        this.rateLimiter = rateLimiter;
        this.auditService = auditService;
        this.userService = userService;
    }

    /**
     * Login com geração de access token + refresh token
     * Tokens são armazenados em cookies HttpOnly para maior segurança
     * Rate limited: 5 tentativas por minuto
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody @Valid LoginRequestDTO data,
            HttpServletRequest request,
            HttpServletResponse response){
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

            // Definir cookies HttpOnly
            setAuthCookies(response, accessToken, refreshToken.getToken());

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
     * Tokens são armazenados em cookies HttpOnly
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenPairDTO> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {
        // Obter refreshToken do cookie
        String refreshTokenValue = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshTokenValue = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshTokenValue == null) {
            logger.warn("Tentativa de refresh sem cookie de refresh token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Validar refresh token
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenValue);
        User user = refreshToken.getUser();

        // Gerar novo access token
        String newAccessToken = tokenService.generateToken(user);

        // Gerar novo refresh token (rotation)
        refreshTokenService.revokeRefreshToken(refreshToken.getToken());
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        // Atualizar cookies
        setAuthCookies(response, newAccessToken, newRefreshToken.getToken());

        logger.info("Tokens renovados para usuário: {}", user.getEmail());
        return ResponseEntity.ok(new TokenPairDTO(newAccessToken, newRefreshToken.getToken()));
    }

    /**
     * Logout (revoga refresh token e limpa cookies)
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            HttpServletRequest request,
            @AuthenticationPrincipal User user,
            HttpServletResponse response) {

        // Obter refreshToken do cookie
        String refreshTokenValue = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshTokenValue = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshTokenValue != null) {
            refreshTokenService.revokeRefreshToken(refreshTokenValue);
        }

        // Limpar cookies
        clearAuthCookies(response);

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
    public ResponseEntity<String> logoutAll(
            @AuthenticationPrincipal User user,
            HttpServletResponse response) {
        refreshTokenService.revokeAllUserTokens(user);

        // Limpar cookies
        clearAuthCookies(response);

        auditService.logLogout(user);
        logger.info("Logout de todos os dispositivos: {}", user.getEmail());
        return ResponseEntity.ok("Logout realizado em todos os dispositivos");
    }

    /**
     * Define cookies HttpOnly para access e refresh tokens
     */
    private void setAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        // Access token cookie (2 horas)
        Cookie accessCookie = new Cookie("accessToken", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(false); // Mudar para true em produção com HTTPS
        accessCookie.setPath("/");
        accessCookie.setMaxAge(2 * 60 * 60); // 2 horas
        response.addCookie(accessCookie);

        // Refresh token cookie (7 dias)
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false); // Mudar para true em produção com HTTPS
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 dias
        response.addCookie(refreshCookie);
    }

    /**
     * Limpa cookies de autenticação
     */
    private void clearAuthCookies(HttpServletResponse response) {
        Cookie accessCookie = new Cookie("accessToken", null);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(false);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);
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

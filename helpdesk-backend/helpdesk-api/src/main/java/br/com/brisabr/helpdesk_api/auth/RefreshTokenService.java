package br.com.brisabr.helpdesk_api.auth;

import br.com.brisabr.helpdesk_api.exception.InvalidRefreshTokenException;
import br.com.brisabr.helpdesk_api.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Serviço para gerenciar tokens de atualização (refresh tokens).
 * Refresh tokens permitem renovar JWT sem re-autenticação.
 */
@Service
public class RefreshTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    // Refresh token válido por 7 dias
    private static final int REFRESH_TOKEN_VALIDITY_DAYS = 7;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Cria um novo refresh token para o usuário.
     *
     * @param user Usuário para o qual o token será criado
     * @return Novo refresh token
     */
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setCreatedAt(LocalDateTime.now());
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS));
        refreshToken.setRevoked(false);

        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        logger.info("Refresh token criado para usuário: {}", user.getEmail());
        return saved;
    }

    /**
     * Valida um refresh token.
     *
     * @param token Token a ser validado
     * @return RefreshToken válido
     * @throws InvalidRefreshTokenException se o token for inválido ou expirado
     */
    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenAndRevokedFalse(token)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token inválido ou revogado"));

        if (refreshToken.isExpired()) {
            logger.warn("Tentativa de usar refresh token expirado: {}", token.substring(0, 8));
            throw new InvalidRefreshTokenException("Refresh token expirado");
        }

        logger.debug("Refresh token validado para usuário: {}", refreshToken.getUser().getEmail());
        return refreshToken;
    }

    /**
     * Revoga um refresh token específico.
     *
     * @param token Token a ser revogado
     */
    @Transactional
    public void revokeRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            rt.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(rt);
            logger.info("Refresh token revogado: {}", token.substring(0, 8));
        });
    }

    /**
     * Revoga todos os refresh tokens de um usuário.
     * Útil ao fazer logout de todos os dispositivos.
     *
     * @param user Usuário cujos tokens serão revogados
     */
    @Transactional
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.revokeAllUserTokens(user, LocalDateTime.now());
        logger.info("Todos os refresh tokens revogados para usuário: {}", user.getEmail());
    }

    /**
     * Limpa tokens expirados do banco (executa diariamente às 3h).
     * Usa query DELETE nativa para melhor performance.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();

        try {
            refreshTokenRepository.deleteExpiredTokens(now);
            logger.info("Limpeza de refresh tokens expirados executada com sucesso");
        } catch (Exception e) {
            logger.error("Erro ao limpar tokens expirados: {}", e.getMessage(), e);
        }
    }
}

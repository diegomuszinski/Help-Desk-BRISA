package br.com.brisabr.helpdesk_api.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementa rate limiting (limitação de taxa) para prevenir força bruta.
 * Usa Caffeine cache para rastrear tentativas por IP.
 */
@Component
public class RateLimiter {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimiter.class);
    
    // Máximo de tentativas por janela de tempo
    private static final int MAX_ATTEMPTS = 5;
    
    // Janela de tempo (1 minuto)
    private static final Duration WINDOW_DURATION = Duration.ofMinutes(1);
    
    // Cache: IP -> contador de tentativas
    private final Cache<String, AtomicInteger> attemptsCache;
    
    public RateLimiter() {
        this.attemptsCache = Caffeine.newBuilder()
                .expireAfterWrite(WINDOW_DURATION)
                .maximumSize(10_000)
                .build();
    }
    
    /**
     * Verifica se o IP está dentro do limite de tentativas.
     * 
     * @param request Requisição HTTP
     * @return true se permitido, false se bloqueado
     */
    public boolean isAllowed(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        
        AtomicInteger attempts = attemptsCache.get(clientIp, k -> new AtomicInteger(0));
        int currentAttempts = attempts.incrementAndGet();
        
        if (currentAttempts > MAX_ATTEMPTS) {
            logger.warn("Rate limit excedido para IP: {} ({} tentativas)", clientIp, currentAttempts);
            return false;
        }
        
        logger.debug("Tentativa {} de {} para IP: {}", currentAttempts, MAX_ATTEMPTS, clientIp);
        return true;
    }
    
    /**
     * Reseta o contador de tentativas para um IP (após login bem-sucedido).
     * 
     * @param request Requisição HTTP
     */
    public void resetAttempts(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        attemptsCache.invalidate(clientIp);
        logger.debug("Contador resetado para IP: {}", clientIp);
    }
    
    /**
     * Obtém tentativas restantes para um IP.
     * 
     * @param request Requisição HTTP
     * @return Número de tentativas restantes
     */
    public int getRemainingAttempts(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        AtomicInteger attempts = attemptsCache.getIfPresent(clientIp);
        
        if (attempts == null) {
            return MAX_ATTEMPTS;
        }
        
        return Math.max(0, MAX_ATTEMPTS - attempts.get());
    }
    
    /**
     * Extrai IP real do cliente (considera proxies e load balancers).
     * 
     * @param request Requisição HTTP
     * @return IP do cliente
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}

package br.com.brisabr.helpdesk_api.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * Interceptor que aplica rate limiting baseado na anotação @RateLimit.
 * Intercepta requisições HTTP e verifica se o limite foi excedido.
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private final Cache<String, Bucket> rateLimitCache;
    private final RateLimitConfig rateLimitConfig;

    public RateLimitInterceptor(Cache<String, Bucket> rateLimitCache,
                                RateLimitConfig rateLimitConfig) {
        this.rateLimitCache = rateLimitCache;
        this.rateLimitConfig = rateLimitConfig;
    }    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler)
            throws Exception {

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true; // Não é um método de controller, permite
        }

        // Verifica se o método tem anotação @RateLimit
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        if (rateLimit == null) {
            return true; // Sem rate limit configurado, permite
        }

        // Gera chave única baseada no tipo de limite
        String key = generateKey(request, rateLimit.type());

        // Obtém ou cria bucket para esta chave
        Bucket bucket = rateLimitCache.get(key, k ->
                rateLimitConfig.createBucket(rateLimit.requestsPerMinute()));

        // Tenta consumir 1 token
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);        if (probe.isConsumed()) {
            // Requisição permitida - adiciona headers informativos
            response.addHeader("X-RateLimit-Limit", String.valueOf(rateLimit.requestsPerMinute()));
            response.addHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            response.addHeader("X-RateLimit-Reset",
                    String.valueOf(TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill())));

            logger.debug("Rate limit check passed for key: {} - Remaining tokens: {}",
                    key, probe.getRemainingTokens());

            return true;
        } else {
            // Limite excedido - bloqueia requisição
            long waitTime = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.addHeader("X-RateLimit-Limit", String.valueOf(rateLimit.requestsPerMinute()));
            response.addHeader("X-RateLimit-Remaining", "0");
            response.addHeader("X-RateLimit-Reset", String.valueOf(waitTime));
            response.addHeader("Retry-After", String.valueOf(waitTime));

            String errorMessage = String.format(
                    "{\"error\":\"%s\",\"retryAfter\":\"%d segundos\"}",
                    rateLimit.message(), waitTime);

            response.getWriter().write(errorMessage);

            logger.warn("Rate limit exceeded for key: {} - Retry after {} seconds", key, waitTime);

            return false;
        }
    }

    /**
     * Gera chave única para identificar o bucket de rate limiting.
     *
     * @param request Requisição HTTP
     * @param type Tipo de limite (PER_IP, PER_USER, GLOBAL)
     * @return Chave única para o bucket
     */
    private String generateKey(HttpServletRequest request, RateLimit.LimitType type) {
        String endpoint = request.getRequestURI();

        return switch (type) {
            case PER_IP -> {
                String ip = getClientIp(request);
                yield String.format("ip:%s:%s", ip, endpoint);
            }
            case PER_USER -> {
                String userId = getUserId();
                yield String.format("user:%s:%s", userId, endpoint);
            }
            case GLOBAL -> {
                yield String.format("global:%s", endpoint);
            }
        };
    }

    /**
     * Obtém o endereço IP real do cliente, considerando proxies e load balancers.
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

    /**
     * Obtém o ID do usuário autenticado do contexto de segurança.
     */
    private String getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "anonymous";
    }
}

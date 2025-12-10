package br.com.helpdesk.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that adds contextual information to MDC (Mapped Diagnostic Context)
 * for structured logging. This information is included in all log entries
 * and makes it easier to trace requests and debug issues.
 */
@Slf4j
@Component
public class LoggingContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        try {
            // Add request ID for tracing
            String requestId = request.getHeader("X-Request-ID");
            if (requestId == null || requestId.isEmpty()) {
                requestId = UUID.randomUUID().toString();
            }
            MDC.put("requestId", requestId);
            response.setHeader("X-Request-ID", requestId);

            // Add user information
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                MDC.put("userId", authentication.getName());
                MDC.put("username", authentication.getName());
            }

            // Add request information
            MDC.put("ipAddress", getClientIpAddress(request));
            MDC.put("userAgent", request.getHeader("User-Agent"));
            MDC.put("endpoint", request.getRequestURI());
            MDC.put("method", request.getMethod());

            // Process request
            filterChain.doFilter(request, response);

            // Calculate and log duration
            long duration = System.currentTimeMillis() - startTime;
            MDC.put("duration", String.valueOf(duration));
            MDC.put("statusCode", String.valueOf(response.getStatus()));

            // Log request completion
            if (response.getStatus() >= 400) {
                log.warn("Request completed with error: {} {} - Status: {} - Duration: {}ms",
                        request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
            } else {
                log.info("Request completed: {} {} - Status: {} - Duration: {}ms",
                        request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
            }

        } finally {
            // Always clear MDC to prevent memory leaks
            MDC.clear();
        }
    }

    /**
     * Extracts the real client IP address considering proxies and load balancers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For can contain multiple IPs, take the first one
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        // Don't filter actuator endpoints to reduce noise
        String path = request.getRequestURI();
        return path.startsWith("/actuator/");
    }
}

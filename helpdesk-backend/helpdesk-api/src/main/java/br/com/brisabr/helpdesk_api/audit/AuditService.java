package br.com.brisabr.helpdesk_api.audit;

import br.com.brisabr.helpdesk_api.user.User;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Serviço para registrar eventos de auditoria.
 * Registra ações sensíveis como login, logout, criação/exclusão de usuários, etc.
 */
@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Registra um evento de auditoria (assíncrono para não bloquear).
     *
     * @param user Usuário que realizou a ação (pode ser null)
     * @param action Tipo de ação (LOGIN, LOGOUT, CREATE_USER, etc.)
     * @param details Detalhes adicionais
     * @param status Status (SUCCESS, FAILURE)
     */
    @Async
    @Transactional
    public void logAudit(User user, String action, String details, String status) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUser(user);
            auditLog.setAction(action);
            auditLog.setDetails(details);
            auditLog.setStatus(status);

            // Capturar informações da requisição HTTP
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                auditLog.setIpAddress(getClientIp(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            }

            auditLogRepository.save(auditLog);

            logger.info("Auditoria registrada: {} - {} - {}", action, status, user != null ? user.getEmail() : "N/A");
        } catch (Exception e) {
            logger.error("Erro ao registrar auditoria: {}", e.getMessage(), e);
        }
    }

    /**
     * Registra login bem-sucedido.
     */
    public void logLogin(User user) {
        logAudit(user, "LOGIN", "Login realizado", "SUCCESS");
    }

    /**
     * Registra tentativa de login falhada.
     */
    public void logLoginFailure(String email, String reason) {
        logAudit(null, "LOGIN_FAILURE", "Email: " + email + " - Motivo: " + reason, "FAILURE");
    }

    /**
     * Registra logout.
     */
    public void logLogout(User user) {
        logAudit(user, "LOGOUT", "Logout realizado", "SUCCESS");
    }

    /**
     * Registra criação de usuário.
     */
    public void logUserCreation(User admin, User newUser) {
        logAudit(admin, "CREATE_USER", "Usuário criado: " + newUser.getEmail() + " (Perfil: " + newUser.getPerfil() + ")", "SUCCESS");
    }

    /**
     * Registra mudança de senha.
     */
    public void logPasswordChange(User user) {
        logAudit(user, "PASSWORD_CHANGE", "Senha alterada", "SUCCESS");
    }

    /**
     * Registra exclusão de usuário.
     */
    public void logUserDeletion(User admin, Long deletedUserId) {
        logAudit(admin, "DELETE_USER", "Usuário deletado: ID " + deletedUserId, "SUCCESS");
    }

    /**
     * Registra acesso negado.
     */
    public void logAccessDenied(User user, String resource) {
        logAudit(user, "ACCESS_DENIED", "Tentativa de acesso negado: " + resource, "FAILURE");
    }

    /**
     * Extrai IP real do cliente (considera proxies).
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

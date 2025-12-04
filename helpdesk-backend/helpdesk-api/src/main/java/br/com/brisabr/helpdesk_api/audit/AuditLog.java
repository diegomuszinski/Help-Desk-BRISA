package br.com.brisabr.helpdesk_api.audit;

import br.com.brisabr.helpdesk_api.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Registro de auditoria de ações sensíveis no sistema.
 * Armazena quem fez o quê, quando e onde (IP).
 */
@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(nullable = false, length = 100)
    private String action; // LOGIN, LOGOUT, CREATE_USER, DELETE_USER, etc.
    
    @Column(columnDefinition = "TEXT")
    private String details; // Detalhes adicionais em JSON ou texto
    
    @Column(length = 45)
    private String ipAddress;
    
    @Column(length = 500)
    private String userAgent;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(length = 20)
    private String status; // SUCCESS, FAILURE
    
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}

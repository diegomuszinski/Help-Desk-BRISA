package br.com.brisabr.helpdesk_api.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);
    
    List<AuditLog> findByActionOrderByTimestampDesc(String action);
    
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp >= :from AND a.timestamp <= :to ORDER BY a.timestamp DESC")
    List<AuditLog> findByTimestampBetween(LocalDateTime from, LocalDateTime to);
    
    List<AuditLog> findByIpAddressOrderByTimestampDesc(String ipAddress);
}

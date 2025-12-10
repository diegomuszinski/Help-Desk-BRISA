package br.com.brisabr.helpdesk_api.ticket;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PrioridadeRepository extends JpaRepository<Prioridade, Long> {
    boolean existsByNome(String nome);
}

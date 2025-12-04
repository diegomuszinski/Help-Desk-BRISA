package br.com.brisabr.helpdesk_api.ticket;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TicketSpecification {

    public static Specification<Ticket> withFilters(
            LocalDate dataInicial, LocalDate dataFinal, String tipoData,
            String status, String categoria, String unidade, String local,
            Long solicitanteId, Long tecnicoId) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            
            if (dataInicial != null && dataFinal != null && tipoData != null) {
                String dateField = tipoData.equalsIgnoreCase("fechamento") ? "dataFechamento" : "dataAbertura";
                predicates.add(criteriaBuilder.between(root.get(dateField), dataInicial.atStartOfDay(), dataFinal.plusDays(1).atStartOfDay()));
            }

            
            if (status != null && !status.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (categoria != null && !categoria.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("categoria"), categoria));
            }
            if (unidade != null && !unidade.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("unidade"), unidade));
            }
            if (local != null && !local.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("local"), local));
            }

            
            if (solicitanteId != null) {
                predicates.add(criteriaBuilder.equal(root.get("solicitante").get("id"), solicitanteId));
            }
            if (tecnicoId != null) {
                predicates.add(criteriaBuilder.equal(root.get("atribuido").get("id"), tecnicoId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
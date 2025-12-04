package br.com.brisabr.helpdesk_api.ticket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prioridades")
public class PrioridadeController {

    @Autowired
    private PrioridadeRepository prioridadeRepository;

    @GetMapping
    public ResponseEntity<List<Prioridade>> getAllPrioridades() {
        return ResponseEntity.ok(prioridadeRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Prioridade> createPrioridade(@RequestBody Prioridade prioridade) {
        return ResponseEntity.ok(prioridadeRepository.save(prioridade));
    }
}
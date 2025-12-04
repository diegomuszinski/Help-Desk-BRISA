package br.com.brisabr.helpdesk_api.equipe;

import br.com.brisabr.helpdesk_api.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "equipes")
@Data
public class Equipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_equipe", unique = true, nullable = false)
    private String nomeEquipe;

    
    @OneToOne
    @JoinColumn(name = "id_gestor")
    private User gestor;

    
    @OneToMany(mappedBy = "equipe")
    @JsonIgnore 
    private List<User> membros;
}
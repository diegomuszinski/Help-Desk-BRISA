package br.com.brisabr.helpdesk_api.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    UserDetails findByEmail(String email);

    @Query(value = "SELECT * FROM usuarios WHERE perfil = :perfil", nativeQuery = true)
    List<User> findByPerfil(@Param("perfil") String perfil);
}
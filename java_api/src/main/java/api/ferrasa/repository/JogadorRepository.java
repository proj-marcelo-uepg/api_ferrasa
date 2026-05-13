package api.ferrasa.repository;

import api.ferrasa.model.Jogador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JogadorRepository extends JpaRepository<Jogador, Long> {
    boolean existsByEmail(String email);
}
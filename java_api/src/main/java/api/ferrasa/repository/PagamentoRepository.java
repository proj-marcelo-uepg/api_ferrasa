package api.ferrasa.repository;

import api.ferrasa.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    List<Pagamento> findByJogadorCod_jogador(Long codJogador);
    boolean existsByJogadorCod_jogadorAndAnoAndMes(Long codJogador, Integer ano, Integer mes);
}
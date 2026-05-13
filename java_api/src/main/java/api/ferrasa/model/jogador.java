package api.ferrasa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "jogo")
@Getter @Setter
public class Jogador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cod_jogador;

    @Column(length = 60, nullable = false)
    private String nome;

    @Column(length = 60, nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private LocalDate datanasc;

    @OneToMany(mappedBy = "jogador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pagamento> pagamentos = new ArrayList<>();
}
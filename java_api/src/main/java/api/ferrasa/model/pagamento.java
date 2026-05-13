package api.ferrasa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pagamento",
       uniqueConstraints = @UniqueConstraint(columnNames = {"cod_jogador", "ano", "mes"}))
@Getter @Setter
public class Pagamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cod_pagamento;

    @Column(nullable = false)
    private Integer ano;

    @Column(nullable = false)
    private Integer mes;

    @Column(nullable = false, precision = 10, scale = 2)
    private Double valor;

    @ManyToOne
    @JoinColumn(name = "cod_jogador", nullable = false)
    private Jogador jogador;
}
package api.ferrasa.service;

import api.ferrasa.model.Jogador;
import api.ferrasa.model.Pagamento;
import api.ferrasa.repository.JogadorRepository;
import api.ferrasa.repository.PagamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PagamentoService {
    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private JogadorRepository jogadorRepository;

    public record PagamentoComNomeDTO(Long cod_pagamento, Integer ano, Integer mes,
                                      Double valor, Long cod_jogador, String nome_jogador) {}

    public List<?> listarTodos(Long jogadorId) {
        if (jogadorId != null) {
            return pagamentoRepository.findByJogadorCod_jogador(jogadorId).stream()
                .map(p -> new PagamentoComNomeDTO(p.getCod_pagamento(), p.getAno(), p.getMes(),
                        p.getValor(), p.getJogador().getCod_jogador(), p.getJogador().getNome()))
                .toList();
        }
        return pagamentoRepository.findAll().stream()
            .map(p -> new PagamentoComNomeDTO(p.getCod_pagamento(), p.getAno(), p.getMes(),
                    p.getValor(), p.getJogador().getCod_jogador(), p.getJogador().getNome()))
            .toList();
    }

    public PagamentoComNomeDTO criar(Pagamento pagamento) {
        if (pagamento.getAno() == null || pagamento.getMes() == null ||
            pagamento.getValor() == null || pagamento.getJogador() == null ||
            pagamento.getJogador().getCod_jogador() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ano, mes, valor e cod_jogador são obrigatórios");
        }

        Long codJogador = pagamento.getJogador().getCod_jogador();
        Jogador jogador = jogadorRepository.findById(codJogador)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jogador não encontrado"));

        if (pagamentoRepository.existsByJogadorCod_jogadorAndAnoAndMes(codJogador, pagamento.getAno(), pagamento.getMes())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Pagamento já existe para este jogador neste mês/ano");
        }

        pagamento.setJogador(jogador);
        Pagamento salvo = pagamentoRepository.save(pagamento);
        return new PagamentoComNomeDTO(salvo.getCod_pagamento(), salvo.getAno(), salvo.getMes(),
                salvo.getValor(), salvo.getJogador().getCod_jogador(), salvo.getJogador().getNome());
    }

    public PagamentoComNomeDTO buscarPorId(Long id) {
        Pagamento pag = pagamentoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pagamento não encontrado"));
        return new PagamentoComNomeDTO(pag.getCod_pagamento(), pag.getAno(), pag.getMes(),
                pag.getValor(), pag.getJogador().getCod_jogador(), pag.getJogador().getNome());
    }

    public void deletar(Long id) {
        if (!pagamentoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pagamento não encontrado");
        }
        pagamentoRepository.deleteById(id);
    }
}
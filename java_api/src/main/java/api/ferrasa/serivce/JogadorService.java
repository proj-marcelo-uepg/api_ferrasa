package api.ferrasa.service;

import api.ferrasa.model.Jogador;
import api.ferrasa.repository.JogadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class JogadorService {
    @Autowired
    private JogadorRepository jogadorRepository;

    public List<Jogador> listarTodos() {
        return jogadorRepository.findAll();
    }

    public Jogador buscarPorId(Long id) {
        return jogadorRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jogador não encontrado"));
    }

    public Jogador criar(Jogador jogador) {
        if (jogador.getNome() == null || jogador.getEmail() == null || jogador.getDatanasc() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome, email e datanasc são obrigatórios");
        }
        if (jogadorRepository.existsByEmail(jogador.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
        }
        return jogadorRepository.save(jogador);
    }

    public Jogador atualizar(Long id, Jogador novosDados) {
        Jogador existente = buscarPorId(id);
        if (novosDados.getEmail() != null && !novosDados.getEmail().equals(existente.getEmail())) {
            if (jogadorRepository.existsByEmail(novosDados.getEmail())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado para outro jogador");
            }
            existente.setEmail(novosDados.getEmail());
        }
        if (novosDados.getNome() != null) existente.setNome(novosDados.getNome());
        if (novosDados.getDatanasc() != null) existente.setDatanasc(novosDados.getDatanasc());
        return jogadorRepository.save(existente);
    }

    public void deletar(Long id) {
        Jogador jogador = buscarPorId(id);
        jogadorRepository.delete(jogador);
    }
}
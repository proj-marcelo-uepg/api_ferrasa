package api.ferrasa.controller;

import api.ferrasa.model.Jogador;
import api.ferrasa.service.JogadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jogadores")
public class JogadorController {
    @Autowired
    private JogadorService jogadorService;

    @GetMapping
    public List<Jogador> listar() {
        return jogadorService.listarTodos();
    }

    @PostMapping
    public ResponseEntity<Jogador> criar(@RequestBody Jogador jogador) {
        Jogador novo = jogadorService.criar(jogador);
        return ResponseEntity.status(HttpStatus.CREATED).body(novo);
    }

    @GetMapping("/{id}")
    public Jogador buscar(@PathVariable Long id) {
        return jogadorService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Jogador atualizar(@PathVariable Long id, @RequestBody Jogador jogador) {
        return jogadorService.atualizar(id, jogador);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        jogadorService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
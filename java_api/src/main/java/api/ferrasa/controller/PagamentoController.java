package api.ferrasa.controller;

import api.ferrasa.model.Pagamento;
import api.ferrasa.service.PagamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pagamentos")
public class PagamentoController {
    @Autowired
    private PagamentoService pagamentoService;

    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(required = false) Long jogadorId) {
        return ResponseEntity.ok(pagamentoService.listarTodos(jogadorId));
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Pagamento pagamento) {
        Object resposta = pagamentoService.criar(pagamento);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(pagamentoService.buscarPorId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        pagamentoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
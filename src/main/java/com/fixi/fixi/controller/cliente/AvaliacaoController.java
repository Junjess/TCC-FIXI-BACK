package com.fixi.fixi.controller.cliente;

import com.fixi.fixi.dto.request.AvaliacaoRequest;
import com.fixi.fixi.dto.response.AvaliacaoResponseDTO;
import com.fixi.fixi.service.AvaliacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/avaliacoes")
@CrossOrigin(origins = "http://localhost:3000")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @PostMapping
    public ResponseEntity<AvaliacaoResponseDTO> salvar(@RequestBody AvaliacaoRequest dto) {
        return ResponseEntity.ok(avaliacaoService.salvarAvaliacao(dto));
    }

    @GetMapping("/prestador/{id}")
    public ResponseEntity<List<AvaliacaoResponseDTO>> listarPorPrestador(@PathVariable Long id) {
        return ResponseEntity.ok(avaliacaoService.listarAvaliacoesPorPrestador(id));
    }
}

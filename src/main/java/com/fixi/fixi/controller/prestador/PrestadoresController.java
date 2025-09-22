package com.fixi.fixi.controller.prestador;

import com.fixi.fixi.dto.request.PrestadorUpdateDTO;
import com.fixi.fixi.dto.response.PrestadorResponseDTO;
import com.fixi.fixi.model.Prestador;
import com.fixi.fixi.repository.PrestadorRepository;
import com.fixi.fixi.service.PrestadorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/prestadores")
@CrossOrigin(origins = "http://localhost:3000")
public class PrestadoresController {

    private final PrestadorService prestadorService;
    private PrestadorRepository prestadorRepository;

    public PrestadoresController(PrestadorService prestadorService, PrestadorRepository prestadorRepository) {
        this.prestadorService = prestadorService;
        this.prestadorRepository = prestadorRepository;
    }

    @PutMapping("/atualizar/{id}")
    public PrestadorResponseDTO atualizarPrestador(
            @PathVariable Long id,
            @RequestBody PrestadorUpdateDTO dto) {
        return prestadorService.atualizarPrestador(id, dto);
    }

    @PostMapping("/{id}/foto")
    public ResponseEntity<PrestadorResponseDTO> atualizarFoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return prestadorRepository.findById(id)
                .map(prestador -> {
                    try {
                        prestador.setFoto(file.getBytes());
                        Prestador salvo = prestadorRepository.save(prestador);
                        return ResponseEntity.ok(prestadorService.toDTO(salvo));
                    } catch (IOException e) {
                        throw new RuntimeException("Erro ao processar a foto", e);
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
}

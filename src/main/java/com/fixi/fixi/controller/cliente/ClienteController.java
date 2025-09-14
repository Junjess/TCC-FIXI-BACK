package com.fixi.fixi.controller.cliente;

import com.fixi.fixi.dto.response.ClienteResponseDTO;
import com.fixi.fixi.model.Cliente;
import com.fixi.fixi.repository.ClienteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

@RestController
@RequestMapping("/clientes")
@CrossOrigin(origins = "http://localhost:3000")
public class ClienteController {

    private final ClienteRepository clienteRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public ClienteController(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarCliente(
            @PathVariable Long id,
            @RequestBody Cliente clienteAtualizado
    ) {
        return clienteRepository.findById(id)
                .map(cliente -> {
                    // 🔹 valida se o email já está em uso
                    if (clienteAtualizado.getEmail() != null) {
                        Cliente existente = clienteRepository.findByEmail(clienteAtualizado.getEmail());
                        if (existente != null && !existente.getId().equals(id)) {
                            return ResponseEntity.badRequest().body("E-mail já está em uso por outro cliente.");
                        }
                        cliente.setEmail(clienteAtualizado.getEmail());
                    }

                    if (clienteAtualizado.getNome() != null) {
                        cliente.setNome(clienteAtualizado.getNome());
                    }
                    if (clienteAtualizado.getTelefone() != null) {
                        cliente.setTelefone(clienteAtualizado.getTelefone());
                    }
                    if (clienteAtualizado.getCidade() != null) {
                        cliente.setCidade(clienteAtualizado.getCidade());
                    }
                    if (clienteAtualizado.getEstado() != null) {
                        cliente.setEstado(clienteAtualizado.getEstado());
                    }

                    // 🔹 só atualiza senha se realmente foi enviada
                    if (clienteAtualizado.getSenha() != null && !clienteAtualizado.getSenha().isBlank()) {
                        cliente.setSenha(passwordEncoder.encode(clienteAtualizado.getSenha()));
                    }

                    Cliente salvo = clienteRepository.save(cliente);

                    return ResponseEntity.ok(new ClienteResponseDTO(
                            salvo.getId(),
                            salvo.getNome(),
                            salvo.getEmail(),
                            salvo.getTelefone(),
                            salvo.getCidade(),
                            salvo.getEstado(),
                            salvo.getFoto()
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/foto")
    public ResponseEntity<?> atualizarFoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        return clienteRepository.findById(id)
                .map(cliente -> {
                    try {
                        if (file.isEmpty()) {
                            return ResponseEntity.badRequest().body("Arquivo vazio");
                        }

                        // 🔹 converte para Base64 e salva
                        String base64 = Base64.getEncoder().encodeToString(file.getBytes());
                        cliente.setFoto(base64);
                        Cliente salvo = clienteRepository.save(cliente);

                        return ResponseEntity.ok(new ClienteResponseDTO(
                                salvo.getId(),
                                salvo.getNome(),
                                salvo.getEmail(),
                                salvo.getTelefone(),
                                salvo.getCidade(),
                                salvo.getEstado(),
                                salvo.getFoto()
                        ));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return ResponseEntity.internalServerError()
                                .body("Erro ao salvar foto: " + e.getMessage());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
}

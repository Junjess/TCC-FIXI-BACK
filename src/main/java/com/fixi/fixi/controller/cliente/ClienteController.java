package com.fixi.fixi.controller.cliente;

import com.fixi.fixi.dto.response.ClienteResponseDTO;
import com.fixi.fixi.model.Cliente;
import com.fixi.fixi.repository.ClienteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
                            salvo.getFoto() != null ? Base64.getEncoder().encodeToString(salvo.getFoto()) : null
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/foto")
    public ResponseEntity<ClienteResponseDTO> atualizarFoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        try {
            cliente.setFoto(file.getBytes()); // salva o binário no banco
            clienteRepository.save(cliente);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar a foto", e);
        }

        return ResponseEntity.ok(new ClienteResponseDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail(),
                cliente.getTelefone(),
                cliente.getCidade(),
                cliente.getEstado(),
                cliente.getFoto() != null ? Base64.getEncoder().encodeToString(cliente.getFoto()) : null
        ));
    }


    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> getFoto(@PathVariable Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        if (cliente.getFoto() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header("Content-Type", "image/jpeg") // ou image/png dependendo do upload
                .body(cliente.getFoto());
    }
}

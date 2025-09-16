package com.fixi.fixi.service;

import com.fixi.fixi.dto.response.CategoriaDescricaoDTO;
import com.fixi.fixi.dto.response.PrestadorResponseDTO;
import com.fixi.fixi.model.Prestador;
import com.fixi.fixi.repository.AvaliacaoRepository;
import com.fixi.fixi.repository.PrestadorRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
public class PrestadorService {

    private final PrestadorRepository prestadorRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    public PrestadorService(PrestadorRepository prestadorRepository, AvaliacaoRepository avaliacaoRepository) {
        this.prestadorRepository = prestadorRepository;
        this.avaliacaoRepository = avaliacaoRepository;
    }

    @Transactional
    public PrestadorResponseDTO atualizarPrestador(Long id, Prestador prestadorAtualizado) {
        Prestador prestador = prestadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado"));

        // valida email duplicado
        if (prestadorAtualizado.getEmail() != null) {
            Prestador existente = prestadorRepository.findByEmail(prestadorAtualizado.getEmail());
            if (existente != null && !existente.getId().equals(id)) {
                throw new RuntimeException("E-mail já está em uso por outro prestador.");
            }
            prestador.setEmail(prestadorAtualizado.getEmail());
        }

        if (prestadorAtualizado.getNome() != null) prestador.setNome(prestadorAtualizado.getNome());
        if (prestadorAtualizado.getTelefone() != null) prestador.setTelefone(prestadorAtualizado.getTelefone());
        if (prestadorAtualizado.getCidade() != null) prestador.setCidade(prestadorAtualizado.getCidade());
        if (prestadorAtualizado.getEstado() != null) prestador.setEstado(prestadorAtualizado.getEstado());
        if (prestadorAtualizado.getCep() != null) prestador.setCep(prestadorAtualizado.getCep());

        if (prestadorAtualizado.getSenha() != null && !prestadorAtualizado.getSenha().isBlank()) {
            prestador.setSenha(passwordEncoder.encode(prestadorAtualizado.getSenha()));
        }

        Prestador salvo = prestadorRepository.save(prestador);

        return toDTO(salvo);
    }

    public PrestadorResponseDTO toDTO(Prestador p) {
        List<CategoriaDescricaoDTO> categorias = p.getCategorias().stream()
                .map(pc -> new CategoriaDescricaoDTO(
                        pc.getCategoria().getId().toString(),
                        pc.getCategoria().getNome()
                ))
                .toList();

        double media = calcularMediaAvaliacoes(p.getId());

        String fotoBase64 = p.getFoto() != null
                ? Base64.getEncoder().encodeToString(p.getFoto())
                : null;

        return new PrestadorResponseDTO(
                p.getId(),
                p.getNome(),
                p.getEmail(),
                p.getTelefone(),
                p.getCidade(),
                p.getEstado(),
                p.getCep(),
                fotoBase64,
                media,
                categorias
        );
    }


    private double calcularMediaAvaliacoes(Long prestadorId) {
        var avaliacoes = avaliacaoRepository.findByAgendamentoPrestadorId(prestadorId);
        return avaliacoes.stream()
                .mapToDouble(a -> a.getNota() != null ? a.getNota() : 0.0)
                .average()
                .orElse(0.0);
    }

}
package com.fixi.fixi.service;

import com.fixi.fixi.dto.request.PrestadorUpdateDTO;
import com.fixi.fixi.dto.response.CategoriaDescricaoDTO;
import com.fixi.fixi.dto.response.PrestadorResponseDTO;
import com.fixi.fixi.model.Categoria;
import com.fixi.fixi.model.Prestador;
import com.fixi.fixi.model.PrestadorCategoria;
import com.fixi.fixi.repository.AvaliacaoRepository;
import com.fixi.fixi.repository.CategoriaRepository;
import com.fixi.fixi.repository.PrestadorCategoriaRepository;
import com.fixi.fixi.repository.PrestadorRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;

@Service
public class PrestadorService {

    private final PrestadorRepository prestadorRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private AvaliacaoRepository avaliacaoRepository;
    @Autowired
    private PrestadorCategoriaRepository prestadorCategoriaRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;

    public PrestadorService(PrestadorRepository prestadorRepository, AvaliacaoRepository avaliacaoRepository) {
        this.prestadorRepository = prestadorRepository;
        this.avaliacaoRepository = avaliacaoRepository;
    }

    @Transactional
    public PrestadorResponseDTO atualizarPrestador(Long id, PrestadorUpdateDTO dto) {
        Prestador prestador = prestadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado"));

        // Dados básicos
        if (dto.getEmail() != null) {
            Prestador existente = prestadorRepository.findByEmail(dto.getEmail());
            if (existente != null && !existente.getId().equals(id)) {
                throw new RuntimeException("E-mail já está em uso por outro prestador.");
            }
            prestador.setEmail(dto.getEmail());
        }

        if (dto.getNome() != null) prestador.setNome(dto.getNome());
        if (dto.getTelefone() != null) prestador.setTelefone(dto.getTelefone());
        if (dto.getCidade() != null) prestador.setCidade(dto.getCidade());
        if (dto.getEstado() != null) prestador.setEstado(dto.getEstado());
        if (dto.getCep() != null) prestador.setCep(dto.getCep());

        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            prestador.setSenha(passwordEncoder.encode(dto.getSenha()));
        }

        // Atualizar categorias
        if (dto.getCategorias() != null) {
            prestadorCategoriaRepository.deleteByPrestador_Id(prestador.getId());

            for (PrestadorUpdateDTO.CategoriaDTO categoriaReq : dto.getCategorias()) {
                Categoria categoria = categoriaRepository.findById(categoriaReq.getId())
                        .orElseThrow(() -> new RuntimeException("Categoria não encontrada: " + categoriaReq.getId()));

                PrestadorCategoria pc = new PrestadorCategoria();
                pc.setPrestador(prestador);
                pc.setCategoria(categoria);

                prestadorCategoriaRepository.save(pc);
            }
        }

        prestadorRepository.save(prestador);
        prestadorRepository.flush(); //garante sincronização com o banco

        // recarrega do banco com categorias atualizadas
        Prestador reloaded = prestadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Erro ao recarregar prestador"));

        return toDTO(reloaded);
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
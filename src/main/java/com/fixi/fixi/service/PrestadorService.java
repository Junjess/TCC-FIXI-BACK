package com.fixi.fixi.service;

import com.fixi.fixi.dto.request.PrestadorUpdateDTO;
import com.fixi.fixi.dto.response.AvaliacaoResponseDTO;
import com.fixi.fixi.dto.response.CategoriaDescricaoDTO;
import com.fixi.fixi.dto.response.PrestadorDetalhesResponseDTO;
import com.fixi.fixi.dto.response.PrestadorResponseDTO;
import com.fixi.fixi.model.*;
import com.fixi.fixi.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrestadorService {

    private final PrestadorRepository prestadorRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AvaliacaoPlataformaRepository avaliacaoPlataformaRepository;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;
    @Autowired
    private PrestadorCategoriaRepository prestadorCategoriaRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;

    @Transactional
    public PrestadorResponseDTO atualizarPrestador(Long id, PrestadorUpdateDTO dto) {
        System.out.println("Id:" + id);
        Prestador prestador = prestadorRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Prestador não encontrado."
                        )
                );

        // Dados básicos
        if (dto.getEmail() != null) {
            Prestador existente = prestadorRepository.findByEmail(dto.getEmail());
            if (existente != null && !existente.getId().equals(id)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "E-mail já está em uso por outro prestador."
                );
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
        if (dto.getSobre() != null && !dto.getSobre().isBlank()) {
            prestador.setSobre(dto.getSobre());
        }

        // Atualizar categorias
        if (dto.getCategorias() != null) {
            prestadorCategoriaRepository.deleteByPrestador_Id(prestador.getId());

            for (PrestadorUpdateDTO.CategoriaDTO categoriaReq : dto.getCategorias()) {
                Categoria categoria = categoriaRepository.findById(categoriaReq.getId())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Categoria não encontrada: " + categoriaReq.getId()
                                )
                        );

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
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Erro ao recarregar prestador."
                        )
                );

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
                categorias,
                p.getSobre()
        );
    }

    private double calcularMediaAvaliacoes(Long prestadorId) {
        var avaliacoes = avaliacaoRepository.findByAgendamentoPrestadorIdAndTipo(prestadorId, AvaliacaoTipo.CLIENTE_AVALIA_PRESTADOR);
        return avaliacoes.stream()
                .mapToDouble(a -> a.getNota() != null ? a.getNota() : 0.0)
                .average()
                .orElse(0.0);
    }

    public PrestadorDetalhesResponseDTO buscarPorId(Long id) {
        Prestador prestador = prestadorRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Prestador não encontrado."
                        )
                );

        // Categorias
        List<CategoriaDescricaoDTO> categorias = prestador.getCategorias().stream()
                .map(pc -> new CategoriaDescricaoDTO(
                        pc.getCategoria().getId().toString(),
                        pc.getCategoria().getNome()
                ))
                .toList();

        // Média de avaliações de clientes
        double mediaAvaliacao = calcularMediaAvaliacoes(prestador.getId());

        // Avaliações (DTO seguro)
        List<AvaliacaoResponseDTO> avaliacoes = avaliacaoRepository.findByAgendamentoPrestadorIdAndTipo(prestador.getId(),AvaliacaoTipo.CLIENTE_AVALIA_PRESTADOR)
                .stream()
                .map(a -> new AvaliacaoResponseDTO(
                        a.getNota(),
                        a.getAgendamento().getCliente().getNome(),
                        a.getDescricao()
                ))
                .toList();

        // Foto como Base64 (se tiver)
        String fotoBase64 = prestador.getFoto() != null
                ? Base64.getEncoder().encodeToString(prestador.getFoto())
                : null;

        // Nota da plataforma (aqui você pode depois trocar pelo cálculo real da Avaliação IA)
        Double notaPlataforma = avaliacaoPlataformaRepository
                .findByPrestadorOrderByPeriodoReferenciaDescDataGeracaoDesc(prestador)
                .stream()
                .findFirst()
                .map(AvaliacaoPlataforma::getNotaFinal)
                .orElse(0.0);

        return new PrestadorDetalhesResponseDTO(
                prestador.getId(),
                prestador.getNome(),
                prestador.getTelefone(),
                fotoBase64,
                prestador.getCidade(),
                prestador.getEstado(),
                categorias,
                mediaAvaliacao,
                avaliacoes,
                notaPlataforma,
                prestador.getSobre()
        );
    }
}
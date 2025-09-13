package com.fixi.fixi.service;

import com.fixi.fixi.dto.response.AvaliacaoResponseDTO;
import com.fixi.fixi.dto.response.BuscaPrestadoresRespostaDTO;
import com.fixi.fixi.dto.response.PrestadorDetalhesResponseDTO;
import com.fixi.fixi.model.Avaliacao;
import com.fixi.fixi.model.Prestador;
import com.fixi.fixi.repository.AvaliacaoRepository;
import com.fixi.fixi.repository.BuscaPrestadoresRepository;
import com.fixi.fixi.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BuscaPrestadoresService {

    private final ClienteRepository clienteRepository;
    private final BuscaPrestadoresRepository buscaRepo;
    private final AvaliacaoRepository avaliacaoRepository;

    public BuscaPrestadoresService(ClienteRepository clienteRepository,
                                   BuscaPrestadoresRepository buscaRepo,
                                   AvaliacaoRepository avaliacaoRepository) {
        this.clienteRepository = clienteRepository;
        this.buscaRepo = buscaRepo;
        this.avaliacaoRepository = avaliacaoRepository;
    }

    public List<BuscaPrestadoresRespostaDTO> listarPrestadoresFiltrados(Long idCliente, String q, List<Long> categoriasIds) {
        var cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        String estadoCliente = cliente.getEstado();
        boolean categoriasVazia = (categoriasIds == null || categoriasIds.isEmpty());
        List<Long> categoriasParam = categoriasVazia ? List.of(-1L) : categoriasIds;
        String qNorm = (q == null || q.isBlank()) ? null : q.trim();

        // 1) Busca apenas os prestadores filtrados (sem média)
        List<Prestador> prestadores = buscaRepo.findFiltradosSemMedia(
                estadoCliente, qNorm, categoriasParam, categoriasVazia
        );

        // 2) Para cada prestador, buscar avaliações e calcular a média
        return prestadores.stream().map(p -> {
            List<Avaliacao> avaliacoes = avaliacaoRepository.findByAgendamentoPrestadorId(p.getId());

            Double media = avaliacoes.isEmpty()
                    ? 0.0
                    : avaliacoes.stream()
                    .mapToDouble(Avaliacao::getNota)
                    .average()
                    .orElse(0.0);

            String categoriaNome = (p.getCategoria() != null && p.getCategoria().getNome() != null)
                    ? p.getCategoria().getNome()
                    : "Sem categoria";

            return new BuscaPrestadoresRespostaDTO(
                    p.getId(),
                    p.getNome(),
                    p.getTelefone(),
                    p.getFoto(),
                    p.getCidade(),
                    p.getEstado(),
                    p.getDescricao(),
                    categoriaNome,
                    media
            );
        }).collect(Collectors.toList());
    }

    public PrestadorDetalhesResponseDTO buscarPrestadorPorId(Long id) {
        Prestador prestador = buscaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado"));

        // Busca todas as avaliações do prestador
        List<Avaliacao> avaliacoes = avaliacaoRepository.findByAgendamentoPrestadorId(prestador.getId());

        // Converte para DTO
        List<AvaliacaoResponseDTO> avaliacoesDTO = avaliacoes.stream()
                .map(a -> new AvaliacaoResponseDTO(
                        a.getNota(),                 // Double
                        a.getAgendamento().getCliente().getNome(),
                        a.getDescricao()
                ))
                .collect(Collectors.toList());

        // Calcula a média
        Double media = avaliacoes.isEmpty()
                ? 0.0
                : avaliacoes.stream()
                .mapToDouble(Avaliacao::getNota)
                .average()
                .orElse(0.0);

        // Retorna os dados completos
        return new PrestadorDetalhesResponseDTO(
                prestador.getId(),
                prestador.getNome(),
                prestador.getTelefone(),
                prestador.getFoto(),
                prestador.getCidade(),
                prestador.getEstado(),
                prestador.getDescricao(),
                prestador.getCategoria() != null ? prestador.getCategoria().getNome() : "Sem categoria",
                media,
                avaliacoesDTO
        );
    }

}

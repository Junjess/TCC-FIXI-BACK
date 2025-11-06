package com.fixi.fixi.service;

import com.fixi.fixi.dto.response.AvaliacaoResponseDTO;
import com.fixi.fixi.dto.response.BuscaPrestadoresRespostaDTO;
import com.fixi.fixi.dto.response.CategoriaDescricaoDTO;
import com.fixi.fixi.dto.response.PrestadorDetalhesResponseDTO;
import com.fixi.fixi.model.Avaliacao;
import com.fixi.fixi.model.AvaliacaoPlataforma;
import com.fixi.fixi.model.AvaliacaoTipo;
import com.fixi.fixi.model.Prestador;
import com.fixi.fixi.repository.AvaliacaoPlataformaRepository;
import com.fixi.fixi.repository.AvaliacaoRepository;
import com.fixi.fixi.repository.BuscaPrestadoresRepository;
// ❌ import do ClienteRepository e ResponseStatusException não são mais necessários
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuscaPrestadoresService {

    private final BuscaPrestadoresRepository buscaRepo;
    private final AvaliacaoPlataformaRepository avaliacaoPlataformaRepository;
    private final AvaliacaoRepository avaliacaoRepository;

    public List<BuscaPrestadoresRespostaDTO> listarPrestadoresFiltrados(
            Long idCliente,
            String q,
            String categoriasCsv,
            String cidade,
            String estado
    ) {
        String qNorm = normalize(q);
        String cidadeNorm = normalize(cidade);
        String estadoNorm = normalizeUF(estado);

        List<Long> categoriasIds = parseCategorias(categoriasCsv);
        boolean categoriasVazia = (categoriasIds == null || categoriasIds.isEmpty());

        List<Prestador> prestadores = buscaRepo.findFiltradosSemMedia(
                cidadeNorm,
                estadoNorm,
                qNorm,
                categoriasVazia ? List.of(-1L) : categoriasIds, // evita IN () quando vazio
                categoriasVazia
        );

        return prestadores.stream().map(p -> {
            // média CLIENTE -> PRESTADOR
            var avaliacoesClientes = avaliacaoRepository
                    .findByAgendamentoPrestadorIdAndTipo(p.getId(), AvaliacaoTipo.CLIENTE_AVALIA_PRESTADOR);

            double mediaClientes = avaliacoesClientes.isEmpty()
                    ? 0.0
                    : avaliacoesClientes.stream()
                    .map(a -> Optional.ofNullable(a.getNota()).orElse(0.0))
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);

            var categoriasDTO = p.getCategorias().stream()
                    .map(pc -> new CategoriaDescricaoDTO(
                            pc.getCategoria().getNome(),
                            pc.getDescricao()
                    ))
                    .toList();

            String fotoBase64 = p.getFoto() != null
                    ? Base64.getEncoder().encodeToString(p.getFoto())
                    : null;

            Double notaPlataforma = avaliacaoPlataformaRepository
                    .findByPrestadorOrderByPeriodoReferenciaDescDataGeracaoDesc(p)
                    .stream()
                    .findFirst()
                    .map(AvaliacaoPlataforma::getNotaFinal)
                    .orElse(0.0);

            return new BuscaPrestadoresRespostaDTO(
                    p.getId(),
                    p.getNome(),
                    p.getTelefone(),
                    fotoBase64,
                    p.getCidade(),
                    p.getEstado(),
                    categoriasDTO,
                    mediaClientes,
                    notaPlataforma,
                    p.getSobre()
            );
        }).collect(Collectors.toList());
    }

    public PrestadorDetalhesResponseDTO buscarPrestadorPorId(Long id) {
        var prestador = buscaRepo.findByIdFetchCategorias(id)
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado.")); // mantenha sua exceção se preferir

        var avaliacoesClientes = avaliacaoRepository
                .findByAgendamentoPrestadorIdAndTipo(prestador.getId(), AvaliacaoTipo.CLIENTE_AVALIA_PRESTADOR);

        var avaliacoesDTO = avaliacoesClientes.stream()
                .map(a -> new AvaliacaoResponseDTO(
                        a.getNota(),
                        a.getAgendamento().getCliente().getNome(),
                        a.getDescricao()
                ))
                .toList();

        double mediaClientes = avaliacoesClientes.isEmpty()
                ? 0.0
                : avaliacoesClientes.stream().mapToDouble(a -> a.getNota() != null ? a.getNota() : 0.0).average().orElse(0.0);

        var categoriasDTO = prestador.getCategorias().stream()
                .map(pc -> new CategoriaDescricaoDTO(
                        pc.getCategoria().getNome(),
                        pc.getDescricao()
                ))
                .toList();

        String fotoBase64 = prestador.getFoto() != null
                ? Base64.getEncoder().encodeToString(prestador.getFoto())
                : null;

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
                categoriasDTO,
                mediaClientes,
                avaliacoesDTO,
                notaPlataforma,
                prestador.getSobre()
        );
    }

    // helpers
    private String normalize(String v) {
        return (v == null || v.isBlank()) ? null : v.trim();
    }
    private String normalizeUF(String v) {
        return (v == null || v.isBlank()) ? null : v.trim().toUpperCase();
    }
    private List<Long> parseCategorias(String csv) {
        if (csv == null || csv.isBlank()) return null;
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .distinct()
                .toList();
    }
}

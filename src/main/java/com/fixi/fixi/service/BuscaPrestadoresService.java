package com.fixi.fixi.service;

import com.fixi.fixi.dto.response.AvaliacaoResponseDTO;
import com.fixi.fixi.dto.response.BuscaPrestadoresRespostaDTO;
import com.fixi.fixi.dto.response.CategoriaDescricaoDTO;
import com.fixi.fixi.dto.response.PrestadorDetalhesResponseDTO;
import com.fixi.fixi.model.Avaliacao;
import com.fixi.fixi.model.AvaliacaoPlataforma;
import com.fixi.fixi.model.Prestador;
import com.fixi.fixi.repository.AvaliacaoPlataformaRepository;
import com.fixi.fixi.repository.AvaliacaoRepository;
import com.fixi.fixi.repository.BuscaPrestadoresRepository;
import com.fixi.fixi.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuscaPrestadoresService {

    private final ClienteRepository clienteRepository;
    private final BuscaPrestadoresRepository buscaRepo;
    private final AvaliacaoPlataformaRepository avaliacaoPlataformaRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final AvaliacaoPlataformaService avaliacaoPlataformaService;

    public List<BuscaPrestadoresRespostaDTO> listarPrestadoresFiltrados(
            Long idCliente,
            String q,
            List<Long> categoriasIds,
            String cidade,
            String estado
    ) {
        // 🔹 Se não veio cidade/estado no request, pega do cliente logado
        var cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        String cidadeFiltro = (cidade != null && !cidade.isBlank()) ? cidade : cliente.getCidade();
        String estadoFiltro = (estado != null && !estado.isBlank()) ? estado : cliente.getEstado();

        boolean categoriasVazia = (categoriasIds == null || categoriasIds.isEmpty());
        List<Long> categoriasParam = categoriasVazia ? List.of(-1L) : categoriasIds;
        String qNorm = (q == null || q.isBlank()) ? null : q.trim();

        // 1) Busca apenas os prestadores filtrados (sem média)
        List<Prestador> prestadores = buscaRepo.findFiltradosSemMedia(
                cidadeFiltro,
                estadoFiltro,
                qNorm,
                categoriasParam,
                categoriasVazia
        );

        // 2) Calcula média das avaliações de clientes
        return prestadores.stream().map(p -> {
            List<Avaliacao> avaliacoes = avaliacaoRepository.findByAgendamentoPrestadorId(p.getId());

            Double mediaClientes = avaliacoes.isEmpty()
                    ? 0.0
                    : avaliacoes.stream()
                    .mapToDouble(Avaliacao::getNota)
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
        Prestador prestador = buscaRepo.findByIdFetchCategorias(id)
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado"));

        List<Avaliacao> avaliacoes = avaliacaoRepository.findByAgendamentoPrestadorId(prestador.getId());

        List<AvaliacaoResponseDTO> avaliacoesDTO = avaliacoes.stream()
                .map(a -> new AvaliacaoResponseDTO(
                        a.getNota(),
                        a.getAgendamento().getCliente().getNome(),
                        a.getDescricao()
                ))
                .toList();

        Double mediaClientes = avaliacoes.isEmpty()
                ? 0.0
                : avaliacoes.stream()
                .mapToDouble(Avaliacao::getNota)
                .average()
                .orElse(0.0);

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
}

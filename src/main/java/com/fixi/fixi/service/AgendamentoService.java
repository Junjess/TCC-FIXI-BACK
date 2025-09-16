package com.fixi.fixi.service;

import com.fixi.fixi.dto.response.AgendamentoRespostaDTO;
import com.fixi.fixi.dto.response.AgendamentoSolicitacaoResponseDTO;
import com.fixi.fixi.model.Agendamento;
import com.fixi.fixi.model.Periodo;
import com.fixi.fixi.model.StatusAgendamento;
import com.fixi.fixi.repository.AgendamentoRepository;
import com.fixi.fixi.repository.CategoriaRepository;
import com.fixi.fixi.repository.ClienteRepository;
import com.fixi.fixi.repository.PrestadorRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final PrestadorRepository prestadorRepository;
    private final CategoriaRepository categoriaRepository;

    public AgendamentoService(
            AgendamentoRepository agendamentoRepository,
            ClienteRepository clienteRepository,
            PrestadorRepository prestadorRepository,
            CategoriaRepository categoriaRepository
    ) {
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
        this.prestadorRepository = prestadorRepository;
        this.categoriaRepository = categoriaRepository;
    }

    /**
     * Lista agendamentos de um cliente.
     */
    @Transactional
    public List<AgendamentoRespostaDTO> listarPorCliente(Long clienteId) {
        return agendamentoRepository.findHistoricoByClienteId(clienteId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Cliente cancela agendamento.
     */
    @Transactional
    public void cancelarAgendamentoCliente(Long agendamentoId, Long clienteId) {
        cancelarAgendamento(agendamentoId, clienteId, false);
    }

    /**
     * Prestador cancela agendamento.
     */
    @Transactional
    public void cancelarAgendamentoPrestador(Long agendamentoId, Long prestadorId) {
        cancelarAgendamento(agendamentoId, prestadorId, true);
    }

    /**
     * Método genérico que aplica as regras de cancelamento.
     */
    private void cancelarAgendamento(Long agendamentoId, Long usuarioId, boolean isPrestador) {
        Agendamento ag = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        if (isPrestador && !ag.getPrestador().getId().equals(usuarioId)) {
            throw new RuntimeException("Prestador não autorizado a cancelar este agendamento");
        }
        if (!isPrestador && !ag.getCliente().getId().equals(usuarioId)) {
            throw new RuntimeException("Cliente não autorizado a cancelar este agendamento");
        }

        ag.setStatus(StatusAgendamento.CANCELADO);
        ag.setCanceladoPor(isPrestador ? "PRESTADOR" : "CLIENTE");

        agendamentoRepository.save(ag);
    }

    /**
     * Lista a agenda de um prestador em um intervalo de datas.
     */
    @Transactional
    public List<AgendamentoRespostaDTO> listarPorPrestador(Long prestadorId, LocalDate from, LocalDate to) {
        return agendamentoRepository.findByPrestadorIdAndDataAgendamentoBetween(prestadorId, from, to)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Solicita um novo agendamento (cliente → prestador).
     */
    @Transactional
    public AgendamentoRespostaDTO solicitarAgendamento(
            Long prestadorId,
            Long clienteId,
            String nomeCategoria,
            LocalDate data,
            Periodo periodo
    ) {
        var prestador = prestadorRepository.findById(prestadorId)
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado"));
        var cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        var categoria = categoriaRepository.findByNome(nomeCategoria);
        if (categoria == null) {
            throw new RuntimeException("Categoria não encontrada");
        }

        // verifica se o prestador possui a categoria
        boolean prestadorPossuiCategoria = prestador.getCategorias().stream()
                .anyMatch(pc -> pc.getCategoria().getNome().equalsIgnoreCase(nomeCategoria));
        if (!prestadorPossuiCategoria) {
            throw new RuntimeException("Prestador não atende a categoria selecionada.");
        }

        // verifica disponibilidade
        boolean prestadorOcupado = agendamentoRepository.existsByPrestadorIdAndDataAgendamentoAndPeriodoAndStatusIn(
                prestadorId,
                data,
                periodo,
                List.of(StatusAgendamento.PENDENTE, StatusAgendamento.ACEITO)
        );
        if (prestadorOcupado) {
            throw new RuntimeException("Prestador já possui agendamento nesse período.");
        }

        Agendamento ag = new Agendamento();
        ag.setPrestador(prestador);
        ag.setCliente(cliente);
        ag.setCategoria(categoria);
        ag.setDataAgendamento(data);
        ag.setPeriodo(periodo);
        ag.setStatus(StatusAgendamento.PENDENTE);
        ag.setDataSolicitacao(LocalDateTime.now());

        var salvo = agendamentoRepository.save(ag);

        return toDTO(salvo);
    }

    /**
     * Lista apenas agendamentos aceitos de um prestador.
     */
    @Transactional
    public List<AgendamentoRespostaDTO> listarAceitosPorPrestador(Long prestadorId) {
        return agendamentoRepository.findAceitosByPrestadorId(prestadorId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Converte Agendamento → DTO
     */
    private AgendamentoRespostaDTO toDTO(Agendamento a) {
        var categorias = a.getPrestador().getCategorias().stream()
                .map(pc -> pc.getCategoria().getNome())
                .toList();

        String fotoBase64 = a.getPrestador().getFoto() != null
                ? Base64.getEncoder().encodeToString(a.getPrestador().getFoto())
                : null;

        return new AgendamentoRespostaDTO(
                a.getId(),

                // 🔹 Prestador
                a.getPrestador().getId(),
                a.getPrestador().getNome(),
                a.getPrestador().getTelefone(),
                fotoBase64,
                a.getPrestador().getCidade(),
                a.getPrestador().getEstado(),
                categorias,

                // 🔹 Cliente
                a.getCliente().getId(),
                a.getCliente().getNome(),
                a.getCliente().getTelefone(),
                Arrays.toString(a.getCliente().getFoto()),
                a.getCliente().getCidade(),
                a.getCliente().getEstado(),

                // 🔹 Agendamento
                a.getDataAgendamento(),
                a.getPeriodo(),
                a.getStatus(),
                a.getAvaliacao() != null,
                a.getAvaliacao() != null ? a.getAvaliacao().getNota() : null,
                a.getAvaliacao() != null ? a.getAvaliacao().getDescricao() : null,
                a.getCanceladoPor()
        );
    }

    /**
     * Prestador aceita agendamento.
     */
    @Transactional
    public void aceitarAgendamento(Long prestadorId, Long agendamentoId) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        if (!agendamento.getPrestador().getId().equals(prestadorId)) {
            throw new RuntimeException("Prestador não autorizado para esse agendamento.");
        }

        agendamento.setStatus(StatusAgendamento.ACEITO);
        agendamentoRepository.save(agendamento);
    }

    /**
     * Prestador recusa agendamento.
     */
    @Transactional
    public void recusarAgendamento(Long prestadorId, Long agendamentoId) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        if (!agendamento.getPrestador().getId().equals(prestadorId)) {
            throw new RuntimeException("Prestador não autorizado para esse agendamento.");
        }

        agendamento.setStatus(StatusAgendamento.NEGADO); // 🔹 corrigido
        agendamentoRepository.save(agendamento);
    }

    /**
     * Lista apenas agendamentos pendentes de um prestador (dados do cliente).
     */
    @Transactional
    public List<AgendamentoSolicitacaoResponseDTO> listarPendentesPorPrestador(Long prestadorId) {
        List<Agendamento> agendamentos = agendamentoRepository.findPendentesByPrestadorId(prestadorId);

        return agendamentos.stream().map(a ->
                new AgendamentoSolicitacaoResponseDTO(
                        a.getId(),
                        a.getCliente().getId(),
                        a.getCliente().getNome(),
                        a.getCliente().getTelefone(),
                        Arrays.toString(a.getCliente().getFoto()),
                        a.getDataAgendamento(),
                        a.getPeriodo(),
                        a.getStatus(),
                        a.getCategoria() != null ? a.getCategoria().getNome() : null
                )
        ).toList();
    }
}

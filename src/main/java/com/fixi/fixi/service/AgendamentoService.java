package com.fixi.fixi.service;

import com.fixi.fixi.dto.response.AgendamentoRespostaDTO;
import com.fixi.fixi.model.Agendamento;
import com.fixi.fixi.model.Periodo;
import com.fixi.fixi.model.StatusAgendamento;
import com.fixi.fixi.repository.AgendamentoRepository;
import com.fixi.fixi.repository.ClienteRepository;
import com.fixi.fixi.repository.PrestadorRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final PrestadorRepository prestadorRepository;

    public AgendamentoService(
            AgendamentoRepository agendamentoRepository,
            ClienteRepository clienteRepository,
            PrestadorRepository prestadorRepository
    ) {
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
        this.prestadorRepository = prestadorRepository;
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
     * Cliente cancela agendamento
     */
    @Transactional
    public void cancelarAgendamentoCliente(Long agendamentoId, Long clienteId) {
        cancelarAgendamento(agendamentoId, clienteId, false);
    }

    /**
     * Prestador cancela agendamento
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
            LocalDate data,
            Periodo periodo
    ) {
        var prestador = prestadorRepository.findById(prestadorId)
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado"));
        var cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // Regra: cliente já tem agendamento neste dia com o mesmo prestador?
        boolean mesmoDiaPrestador = agendamentoRepository.existsByClienteIdAndPrestadorIdAndDataAgendamentoAndStatusIn(
                clienteId,
                prestadorId,
                data,
                List.of(StatusAgendamento.PENDENTE, StatusAgendamento.ACEITO)
        );
        if (mesmoDiaPrestador) {
            throw new RuntimeException("Você já possui um agendamento com este prestador nesta data.");
        }

        // Criar novo agendamento
        Agendamento ag = new Agendamento();
        ag.setPrestador(prestador);
        ag.setCliente(cliente);
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

        return new AgendamentoRespostaDTO(
                a.getId(),
                a.getPrestador().getId(),
                a.getPrestador().getNome(),
                a.getPrestador().getTelefone(),
                a.getPrestador().getFoto(),
                a.getPrestador().getCidade(),
                a.getPrestador().getEstado(),
                categorias,
                a.getDataAgendamento(),
                a.getPeriodo(),
                a.getStatus(),
                a.getAvaliacao() != null,
                a.getAvaliacao() != null ? a.getAvaliacao().getNota() : null,
                a.getAvaliacao() != null ? a.getAvaliacao().getDescricao() : null,
                a.getCanceladoPor()
        );
    }
}

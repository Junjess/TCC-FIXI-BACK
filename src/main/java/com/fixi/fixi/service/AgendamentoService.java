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

    @Transactional
    public List<AgendamentoRespostaDTO> listarPorCliente(Long clienteId) {
        return agendamentoRepository.findResumoByClienteId(clienteId);
    }

    @Transactional
    public void cancelarAgendamento(Long agendamentoId, Long clienteId) {
        Agendamento ag = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        if (!ag.getCliente().getId().equals(clienteId)) {
            throw new RuntimeException("Operação não permitida");
        }

        agendamentoRepository.delete(ag);
    }

    @Transactional
    public List<AgendamentoRespostaDTO> listarPorPrestador(Long prestadorId, LocalDate from, LocalDate to) {
        return agendamentoRepository.findByPrestadorIdAndDataAgendamentoBetween(prestadorId, from, to)
                .stream()
                .map(a -> new AgendamentoRespostaDTO(
                        a.getId(),
                        a.getPrestador().getId(),
                        a.getPrestador().getNome(),
                        a.getPrestador().getTelefone(),
                        a.getPrestador().getFoto(),
                        a.getPrestador().getCidade(),
                        a.getPrestador().getEstado(),
                        a.getPrestador().getCategoria().getNome(),
                        a.getDataAgendamento(),
                        a.getPeriodo(),
                        a.getStatus()
                ))
                .toList();
    }

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

        // ✅ Nova regra: cliente já tem agendamento neste dia com o mesmo prestador?
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

        return new AgendamentoRespostaDTO(
                salvo.getId(),
                prestador.getId(),
                prestador.getNome(),
                prestador.getTelefone(),
                prestador.getFoto(),
                prestador.getCidade(),
                prestador.getEstado(),
                prestador.getCategoria().getNome(),
                salvo.getDataAgendamento(),
                salvo.getPeriodo(),
                salvo.getStatus()
        );
    }
}
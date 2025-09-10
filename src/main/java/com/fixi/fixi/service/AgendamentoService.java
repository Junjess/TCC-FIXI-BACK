package com.fixi.fixi.service;

import com.fixi.fixi.dto.response.AgendamentoRespostaDTO;
import com.fixi.fixi.model.Agendamento;
import com.fixi.fixi.repository.AgendamentoRepository;
import com.fixi.fixi.repository.ClienteRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgendamentoService {

    AgendamentoRepository agendamentoRepository;
    ClienteRepository clienteRepository;

    public AgendamentoService(AgendamentoRepository agendamentoRepository, ClienteRepository clienteRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
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
}

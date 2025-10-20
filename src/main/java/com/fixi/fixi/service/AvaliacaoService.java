package com.fixi.fixi.service;

import com.fixi.fixi.dto.request.AvaliacaoRequest;
import com.fixi.fixi.dto.response.AvaliacaoResponseDTO;
import com.fixi.fixi.model.Agendamento;
import com.fixi.fixi.model.Avaliacao;
import com.fixi.fixi.repository.AgendamentoRepository;
import com.fixi.fixi.repository.AvaliacaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final AgendamentoRepository agendamentoRepository;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository,
                            AgendamentoRepository agendamentoRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    public AvaliacaoResponseDTO salvarAvaliacao(AvaliacaoRequest dto) {
        Agendamento agendamento = agendamentoRepository.findById(dto.getAgendamentoId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Agendamento não encontrado."
                        )
                );

        if (avaliacaoRepository.findByAgendamentoId(agendamento.getId()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este agendamento já foi avaliado."
            );
        }

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setAgendamento(agendamento);
        avaliacao.setNota(dto.getNota());
        avaliacao.setDescricao(dto.getDescricao());

        Avaliacao salvo = avaliacaoRepository.save(avaliacao);

        return new AvaliacaoResponseDTO(
                salvo.getNota(),
                salvo.getAgendamento().getCliente().getNome(),
                salvo.getDescricao()
        );
    }

    public List<AvaliacaoResponseDTO> listarAvaliacoesPorPrestador(Long prestadorId) {
        return avaliacaoRepository.findByAgendamentoPrestadorId(prestadorId)
                .stream()
                .map(a -> new AvaliacaoResponseDTO(
                        a.getNota(),
                        a.getAgendamento().getCliente().getNome(),
                        a.getDescricao()
                ))
                .collect(Collectors.toList());
    }
}

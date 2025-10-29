package com.fixi.fixi.service;

import com.fixi.fixi.dto.request.AvaliacaoRequest;
import com.fixi.fixi.dto.response.AvaliacaoDirecionalResponseDTO;
import com.fixi.fixi.dto.response.AvaliacaoResponseDTO;
import com.fixi.fixi.model.Agendamento;
import com.fixi.fixi.model.Avaliacao;
import com.fixi.fixi.model.AvaliacaoTipo;
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

    // (LEGADO) Cliente -> Prestador (mantém assinatura compatível com seu controller atual se quiser)
    public AvaliacaoResponseDTO salvarAvaliacao(AvaliacaoRequest dto) {
        return salvarAvaliacaoClienteParaPrestador(dto);
    }

    public AvaliacaoResponseDTO salvarAvaliacaoClienteParaPrestador(AvaliacaoRequest dto) {
        Avaliacao salvo = salvarGenerico(dto, AvaliacaoTipo.CLIENTE_AVALIA_PRESTADOR);
        return new AvaliacaoResponseDTO(
                salvo.getNota(),
                salvo.getAgendamento().getCliente().getNome(),
                salvo.getDescricao()
        );
    }

    public AvaliacaoDirecionalResponseDTO salvarAvaliacaoPrestadorParaCliente(AvaliacaoRequest dto) {
        Avaliacao salvo = salvarGenerico(dto, AvaliacaoTipo.PRESTADOR_AVALIA_CLIENTE);
        return new AvaliacaoDirecionalResponseDTO(
                salvo.getNota(),
                salvo.getAgendamento().getPrestador().getNome(),
                salvo.getAgendamento().getCliente().getNome(),
                salvo.getDescricao(),
                salvo.getTipo()
        );
    }

    private Avaliacao salvarGenerico(AvaliacaoRequest dto, AvaliacaoTipo tipo) {
        Agendamento agendamento = agendamentoRepository.findById(dto.getAgendamentoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamento não encontrado."));

        // (Opcional) Valida se o agendamento está concluído antes de permitir avaliação
        // if (agendamento.getStatus() != StatusAgendamento.CONCLUIDO) { ... }

        // Impede duplicidade por direção
        if (avaliacaoRepository.findByAgendamentoIdAndTipo(agendamento.getId(), tipo).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este agendamento já foi avaliado nessa direção.");
        }

        Avaliacao a = new Avaliacao();
        a.setAgendamento(agendamento);
        a.setNota(dto.getNota());
        a.setDescricao(dto.getDescricao());
        a.setTipo(tipo);

        return avaliacaoRepository.save(a);
    }

    /*public AvaliacaoResponseDTO salvarAvaliacao(AvaliacaoRequest dto) {
        Agendamento agendamento = agendamentoRepository.findById(dto.getAgendamentoId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Agendamento não encontrado."
                        )
                );

        if (avaliacaoRepository.findByAgendamentoIdAndTipo(agendamento.getId()).isPresent()) {
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
    }*/

    // (LEGADO) avaliações recebidas pelo prestador (clientes avaliaram prestador) — mantém seu retorno antigo
    public List<AvaliacaoResponseDTO> listarAvaliacoesPorPrestador(Long prestadorId) {
        return avaliacaoRepository
                .findByAgendamentoPrestadorIdAndTipo(prestadorId, AvaliacaoTipo.CLIENTE_AVALIA_PRESTADOR)
                .stream()
                .map(a -> new AvaliacaoResponseDTO(
                        a.getNota(),
                        a.getAgendamento().getCliente().getNome(),
                        a.getDescricao()
                ))
                .collect(Collectors.toList());
    }

    // Novo: avaliações recebidas pelo cliente (prestadores avaliaram cliente)
    public List<AvaliacaoDirecionalResponseDTO> listarAvaliacoesPorCliente(Long clienteId) {
        return avaliacaoRepository
                .findByAgendamentoClienteIdAndTipo(clienteId, AvaliacaoTipo.PRESTADOR_AVALIA_CLIENTE)
                .stream()
                .map(a -> new AvaliacaoDirecionalResponseDTO(
                        a.getNota(),
                        a.getAgendamento().getPrestador().getNome(),
                        a.getAgendamento().getCliente().getNome(),
                        a.getDescricao(),
                        a.getTipo()
                ))
                .collect(Collectors.toList());
    }
}

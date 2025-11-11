package com.fixi.fixi.service;

import com.fixi.fixi.model.AvaliacaoPlataforma;
import com.fixi.fixi.model.Prestador;
import com.fixi.fixi.model.StatusAgendamento;
import com.fixi.fixi.repository.AvaliacaoPlataformaRepository;
import com.fixi.fixi.repository.PrestadorRepository;
import com.fixi.fixi.repository.AgendamentoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AvaliacaoPlataformaService {

    private final PrestadorRepository prestadorRepository;
    private final AvaliacaoPlataformaRepository avaliacaoRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final GroqService groqService;

    public AvaliacaoPlataformaService(
            PrestadorRepository prestadorRepository,
            AvaliacaoPlataformaRepository avaliacaoRepository,
            AgendamentoRepository agendamentoRepository, GroqService groqService) {
        this.prestadorRepository = prestadorRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.groqService = groqService;
    }

    public List<AvaliacaoPlataforma> listarAvaliacoesIA(Long prestadorId) {
        return avaliacaoRepository.findByPrestadorIdOrderByPeriodoReferenciaAsc(prestadorId);
    }

    /**
     * Calcula e salva a nota mensal de todos os prestadores.
     */
    public void calcularNotasMensais() {
        List<Prestador> prestadores = prestadorRepository.findAll();
        LocalDate periodo = LocalDate.now().withDayOfMonth(1);

        for (Prestador prestador : prestadores) {
            AvaliacaoPlataforma avaliacao = new AvaliacaoPlataforma();
            avaliacao.setPrestador(prestador);

            avaliacao.setTempoPlataforma(calcularTempoPlataforma(prestador));
            avaliacao.setTaxaAceitacao(calcularTaxaAceitacao(prestador));
            avaliacao.setTaxaCancelamento(calcularTaxaCancelamento(prestador));
            avaliacao.setAvaliacaoIa(calcularAvaliacaoIa(prestador));

            avaliacao.calcularNotaFinal();
            avaliacao.setPeriodoReferencia(periodo);

            avaliacaoRepository.save(avaliacao);
        }
    }

    /**
     * Tempo de plataforma = meses desde o cadastro, normalizado em escala 0–5.
     */
    private Double calcularTempoPlataforma(Prestador prestador) {
        if (prestador.getDataCadastro() == null) return 0.0;

        long mesesNaPlataforma = java.time.temporal.ChronoUnit.MONTHS.between(
                prestador.getDataCadastro(), LocalDate.now()
        );

        return Math.min((mesesNaPlataforma / 12.0) * 5.0, 5.0);
    }

    /**
     * Taxa de aceitação = (aceitos / total) * 5.
     */
    private Double calcularTaxaAceitacao(Prestador prestador) {
        long total = agendamentoRepository.countByPrestador(prestador);
        if (total == 0) return 0.0;

        long aceitos = agendamentoRepository.countByPrestadorAndStatus(prestador, StatusAgendamento.ACEITO);
        return (aceitos / (double) total) * 5.0;
    }

    /**
     * Taxa de cancelamento = (1 - (cancelados / total)) * 5.
     */
    private Double calcularTaxaCancelamento(Prestador prestador) {
        long total = agendamentoRepository.countByPrestador(prestador);
        if (total == 0) return 5.0;

        long cancelados = agendamentoRepository.countByPrestadorAndStatus(prestador, StatusAgendamento.CANCELADO);
        double taxaCancel = cancelados / (double) total;
        return (1 - taxaCancel) * 5.0;
    }

    private Double calcularAvaliacaoIa(Prestador prestador) {
        List<String> comentarios = agendamentoRepository.findComentariosByPrestador(prestador.getId());
        Double nota = groqService.avaliarComentariosPrestador(comentarios);
        if (nota == null || nota.isNaN()) nota = 0.0;
        return Math.max(0.0, Math.min(5.0, nota));
    }
}

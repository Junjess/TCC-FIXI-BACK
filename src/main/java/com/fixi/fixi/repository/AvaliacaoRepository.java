package com.fixi.fixi.repository;

import com.fixi.fixi.model.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    // usado para não permitir mais de uma avaliação por agendamento
    Optional<Avaliacao> findByAgendamentoId(Long agendamentoId);

    // usado para listar avaliações de um prestador
    java.util.List<Avaliacao> findByAgendamentoPrestadorId(Long prestadorId);
}

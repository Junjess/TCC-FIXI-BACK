package com.fixi.fixi.repository;

import com.fixi.fixi.model.Avaliacao;
import com.fixi.fixi.model.AvaliacaoTipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    Optional<Avaliacao> findByAgendamentoIdAndTipo(Long agendamentoId, AvaliacaoTipo tipo);

    List<Avaliacao> findByAgendamentoPrestadorIdAndTipo(Long prestadorId, AvaliacaoTipo tipo);

    List<Avaliacao> findByAgendamentoClienteIdAndTipo(Long clienteId, AvaliacaoTipo tipo);
}

package com.fixi.fixi.repository;

import com.fixi.fixi.model.AvaliacaoPlataforma;
import com.fixi.fixi.model.Prestador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AvaliacaoPlataformaRepository extends JpaRepository<AvaliacaoPlataforma, Long> {

    /**
     * Busca a última avaliação da plataforma para um prestador,
     * ordenando pelo período de referência (mês) e data de geração.
     */
    @Query("""
           SELECT a FROM AvaliacaoPlataforma a
           WHERE a.prestador = :prestador
           ORDER BY a.periodoReferencia DESC, a.dataGeracao DESC
           """)
    Optional<AvaliacaoPlataforma> findUltimaAvaliacaoByPrestador(@Param("prestador") Prestador prestador);

    List<AvaliacaoPlataforma> findByPrestadorIdOrderByPeriodoReferenciaAsc(Long prestadorId);
}
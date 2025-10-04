package com.fixi.fixi.repository;

import com.fixi.fixi.model.AvaliacaoPlataforma;
import com.fixi.fixi.model.Prestador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AvaliacaoPlataformaRepository extends JpaRepository<AvaliacaoPlataforma, Long> {

    List<AvaliacaoPlataforma> findByPrestadorIdOrderByPeriodoReferenciaAsc(Long prestadorId);

    List<AvaliacaoPlataforma> findByPrestadorOrderByPeriodoReferenciaDescDataGeracaoDesc(Prestador prestador);

}
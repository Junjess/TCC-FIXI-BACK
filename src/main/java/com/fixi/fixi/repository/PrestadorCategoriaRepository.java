package com.fixi.fixi.repository;

import com.fixi.fixi.model.PrestadorCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrestadorCategoriaRepository extends JpaRepository<PrestadorCategoria, Long> {

    // Buscar todas as categorias de um prestador
    List<PrestadorCategoria> findByPrestadorId(Long prestadorId);

    // Buscar todos os prestadores de uma categoria
    List<PrestadorCategoria> findByCategoriaId(Long categoriaId);
}

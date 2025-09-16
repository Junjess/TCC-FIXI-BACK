package com.fixi.fixi.repository;

import com.fixi.fixi.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    Categoria findByNome(String nome);
    List<Categoria> findAllByOrderByNomeAsc();
}

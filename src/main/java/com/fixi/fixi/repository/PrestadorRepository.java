package com.fixi.fixi.repository;

import com.fixi.fixi.model.Prestador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrestadorRepository extends JpaRepository<Prestador, Long> {
    Prestador findByEmail(String email);

    List<Prestador> findByEstado(String estado);
}


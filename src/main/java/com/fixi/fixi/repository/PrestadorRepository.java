package com.fixi.fixi.repository;

import com.fixi.fixi.model.Prestador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrestadorRepository extends JpaRepository<Prestador, Long> {
    Prestador findByEmail(String email);
}

package com.fixi.fixi.repository;

import com.fixi.fixi.model.Conversa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ConversaRepository extends JpaRepository<Conversa, Long> {
    List<Conversa> findByCliente_IdOrderByDataInicioAsc(Long clienteId);
    List<Conversa> findTop10ByCliente_IdOrderByDataInicioDesc(Long clienteId);
}
package com.fixi.fixi.repository;

import com.fixi.fixi.model.Mensagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensagemRepository extends JpaRepository<Mensagem, Long> {
    List<Mensagem> findByConversa_IdOrderByDataEnvioAsc(Long conversaId);
    long countByConversa_Id(Long conversaId);
    void deleteAllByConversa_Id(Long conversaId);
}
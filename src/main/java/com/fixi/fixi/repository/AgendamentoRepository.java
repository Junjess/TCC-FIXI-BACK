package com.fixi.fixi.repository;

import com.fixi.fixi.dto.response.AgendamentoRespostaDTO;
import com.fixi.fixi.model.Agendamento;
import com.fixi.fixi.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    @Query("""
                select new com.fixi.fixi.dto.response.AgendamentoRespostaDTO(
                    a.id,
                    p.id,
                    p.nome,
                    p.telefone,
                    p.foto,
                    p.cidade,
                    p.estado,
                    c.nome,
                    a.dataAgendamento,
                    a.periodo,
                    a.status
                )
                from Agendamento a
                  join a.prestador p
                  join p.categoria c
                where a.cliente.id = :clienteId
                and a.status in (com.fixi.fixi.model.StatusAgendamento.ACEITO,\s
                                       com.fixi.fixi.model.StatusAgendamento.PENDENTE)
                order by a.dataAgendamento desc
            """)


    List<AgendamentoRespostaDTO> findResumoByClienteId(@Param("clienteId") Long clienteId);

}
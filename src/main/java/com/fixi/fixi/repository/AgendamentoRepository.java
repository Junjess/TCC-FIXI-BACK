package com.fixi.fixi.repository;

import com.fixi.fixi.dto.response.AgendamentoRespostaDTO;
import com.fixi.fixi.model.Agendamento;
import com.fixi.fixi.model.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    /**
     * Histórico de agendamentos de um cliente, incluindo avaliação (se houver).
     */
    @Query("""
        select new com.fixi.fixi.dto.response.AgendamentoRespostaDTO(
            a.id,
            p.id,
            p.nome,
            p.telefone,
            p.foto,
            p.cidade,
            p.estado,
            p.categoria.nome,
            a.dataAgendamento,
            a.periodo,
            a.status,
            case when av.id is not null then true else false end,
            av.nota,
            av.descricao
        )
        from Agendamento a
        join a.prestador p
        left join Avaliacao av on av.agendamento.id = a.id
        where a.cliente.id = :clienteId
    """)
    List<AgendamentoRespostaDTO> findResumoByClienteId(@Param("clienteId") Long clienteId);

    /**
     * Agenda de um prestador em um intervalo de datas.
     */
    List<Agendamento> findByPrestadorIdAndDataAgendamentoBetween(
            Long prestadorId,
            LocalDate from,
            LocalDate to
    );

    /**
     * Verifica se já existe agendamento de um cliente com um prestador em uma data,
     * considerando status (ex: PENDENTE ou ACEITO).
     */
    boolean existsByClienteIdAndPrestadorIdAndDataAgendamentoAndStatusIn(
            Long clienteId,
            Long prestadorId,
            LocalDate dataAgendamento,
            List<StatusAgendamento> status
    );
}

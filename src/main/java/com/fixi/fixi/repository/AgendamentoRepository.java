package com.fixi.fixi.repository;

import com.fixi.fixi.model.Agendamento;
import com.fixi.fixi.model.Periodo;
import com.fixi.fixi.model.Prestador;
import com.fixi.fixi.model.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    /**
     * Histórico de agendamentos de um cliente (carregando prestador, cliente, categorias e avaliação).
     */
    @Query("""
            select distinct a
            from Agendamento a
            join fetch a.prestador p
            join fetch a.cliente c
            left join fetch a.avaliacao av
            left join fetch p.categorias pc
            left join fetch pc.categoria ca
            where c.id = :clienteId
            """)
    List<Agendamento> findHistoricoByClienteId(@Param("clienteId") Long clienteId);

    /**
     * Lista agendamentos aceitos de um prestador (carregando cliente, categorias e avaliação).
     */
    @Query("""
            select distinct a
            from Agendamento a
            join fetch a.prestador p
            join fetch a.cliente c
            join fetch a.categoria ca
            left join fetch a.avaliacao av
            where p.id = :prestadorId
              and a.status = com.fixi.fixi.model.StatusAgendamento.ACEITO
            """)
    List<Agendamento> findAceitosByPrestadorId(@Param("prestadorId") Long prestadorId);

    /**
     * Lista agendamentos de um prestador em intervalo de datas (carregando categorias).
     */
    @Query("""
            select distinct a
            from Agendamento a
            join fetch a.prestador p
            left join fetch p.categorias pc
            left join fetch pc.categoria ca
            where p.id = :prestadorId
              and a.dataAgendamento between :from and :to
            """)
    List<Agendamento> findByPrestadorIdAndDataAgendamentoBetween(
            @Param("prestadorId") Long prestadorId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    /**
     * Verifica se já existe agendamento ativo entre cliente e prestador no mesmo dia.
     */
    boolean existsByClienteIdAndPrestadorIdAndDataAgendamentoAndStatusIn(
            Long clienteId,
            Long prestadorId,
            LocalDate dataAgendamento,
            List<StatusAgendamento> status
    );

    boolean existsByClienteIdAndPrestadorIdAndDataAgendamentoAndPeriodoAndStatusIn(
            Long clienteId,
            Long prestadorId,
            LocalDate data,
            Periodo periodo,
            List<StatusAgendamento> status
    );

    boolean existsByPrestadorIdAndDataAgendamentoAndPeriodoAndStatusIn(
            Long prestadorId,
            LocalDate data,
            Periodo periodo,
            List<StatusAgendamento> status
    );

    /**
     * Lista agendamentos pendentes de um prestador.
     */
    @Query("""
            select distinct a
            from Agendamento a
            join fetch a.cliente c
            join fetch a.prestador p
            where p.id = :prestadorId
              and a.status = com.fixi.fixi.model.StatusAgendamento.PENDENTE
            """)
    List<Agendamento> findPendentesByPrestadorId(@Param("prestadorId") Long prestadorId);

    /** Total de agendamentos de um prestador */
    @Query("SELECT COUNT(a) FROM Agendamento a WHERE a.prestador = :prestador")
    long countByPrestador(@Param("prestador") Prestador prestador);

    /** Total de agendamentos por status (ACEITO, CANCELADO, etc.) */
    @Query("SELECT COUNT(a) FROM Agendamento a WHERE a.prestador = :prestador AND a.status = :status")
    long countByPrestadorAndStatus(@Param("prestador") Prestador prestador,
                                   @Param("status") StatusAgendamento status);

    @Query("SELECT av.descricao FROM Agendamento a JOIN a.avaliacao av WHERE a.prestador.id = :prestadorId AND av.descricao IS NOT NULL")
    List<String> findComentariosByPrestador(@Param("prestadorId") Long prestadorId);

    List<Agendamento> findByStatus(StatusAgendamento status);
}

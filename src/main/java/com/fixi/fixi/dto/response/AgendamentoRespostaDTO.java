package com.fixi.fixi.dto.response;

import com.fixi.fixi.model.Periodo;
import com.fixi.fixi.model.StatusAgendamento;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class AgendamentoRespostaDTO {

    private Long idAgendamento;
    private Long idPrestador;
    private String nomePrestador;
    private String telefonePrestador;
    private String fotoPrestador;
    private String cidadePrestador;
    private String estadoPrestador;

    // ✅ agora é lista, pois um prestador pode ter várias categorias
    private List<String> categoriasPrestador;

    private LocalDate data;
    private String periodo;
    private String statusAgendamento;
    private boolean avaliado;
    private Double nota;
    private String descricaoAvaliacao;
    private String canceladoPor; // CLIENTE ou PRESTADOR

    /**
     * Construtor completo (com avaliação e canceladoPor).
     */
    public AgendamentoRespostaDTO(
            Long idAgendamento,
            Long idPrestador,
            String nomePrestador,
            String telefonePrestador,
            String fotoPrestador,
            String cidadePrestador,
            String estadoPrestador,
            List<String> categoriasPrestador,
            LocalDate data,
            Periodo periodo,
            StatusAgendamento status,
            boolean avaliado,
            Double nota,
            String descricaoAvaliacao,
            String canceladoPor
    ) {
        this.idAgendamento = idAgendamento;
        this.idPrestador = idPrestador;
        this.nomePrestador = nomePrestador;
        this.telefonePrestador = telefonePrestador;
        this.fotoPrestador = fotoPrestador;
        this.cidadePrestador = cidadePrestador;
        this.estadoPrestador = estadoPrestador;
        this.categoriasPrestador = categoriasPrestador;
        this.data = data;
        this.periodo = periodo.name();
        this.statusAgendamento = status.name();
        this.avaliado = avaliado;
        this.nota = nota;
        this.descricaoAvaliacao = descricaoAvaliacao;
        this.canceladoPor = canceladoPor;
    }

    /**
     * Construtor reduzido (sem avaliação, mas com canceladoPor).
     */
    public AgendamentoRespostaDTO(
            Long idAgendamento,
            Long idPrestador,
            String nomePrestador,
            String telefonePrestador,
            String fotoPrestador,
            String cidadePrestador,
            String estadoPrestador,
            List<String> categoriasPrestador,
            LocalDate data,
            Periodo periodo,
            StatusAgendamento status,
            boolean avaliado,
            String canceladoPor
    ) {
        this.idAgendamento = idAgendamento;
        this.idPrestador = idPrestador;
        this.nomePrestador = nomePrestador;
        this.telefonePrestador = telefonePrestador;
        this.fotoPrestador = fotoPrestador;
        this.cidadePrestador = cidadePrestador;
        this.estadoPrestador = estadoPrestador;
        this.categoriasPrestador = categoriasPrestador;
        this.data = data;
        this.periodo = periodo.name();
        this.statusAgendamento = status.name();
        this.avaliado = avaliado;
        this.nota = null;
        this.descricaoAvaliacao = null;
        this.canceladoPor = canceladoPor;
    }
}

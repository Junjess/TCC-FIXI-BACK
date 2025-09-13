package com.fixi.fixi.dto.response;

import com.fixi.fixi.model.Periodo;
import com.fixi.fixi.model.StatusAgendamento;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

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
    private String categoriaPrestador;
    private LocalDate data;
    private String periodo;
    private String statusAgendamento;
    private boolean avaliado; // ✅ novo campo

    public AgendamentoRespostaDTO(
            Long idAgendamento,
            Long idPrestador,
            String nomePrestador,
            String telefonePrestador,
            String fotoPrestador,
            String cidadePrestador,
            String estadoPrestador,
            String categoriaPrestador,
            LocalDate data,
            Periodo periodo,
            StatusAgendamento status,
            boolean avaliado
    ) {
        this.idAgendamento = idAgendamento;
        this.idPrestador = idPrestador;
        this.nomePrestador = nomePrestador;
        this.telefonePrestador = telefonePrestador;
        this.fotoPrestador = fotoPrestador;
        this.cidadePrestador = cidadePrestador;
        this.estadoPrestador = estadoPrestador;
        this.categoriaPrestador = categoriaPrestador;
        this.data = data;
        this.periodo = periodo.name();
        this.statusAgendamento = status.name();
        this.avaliado = avaliado;
    }
}

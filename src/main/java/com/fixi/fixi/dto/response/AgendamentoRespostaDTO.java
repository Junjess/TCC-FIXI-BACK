package com.fixi.fixi.dto.response;

import com.fixi.fixi.model.Periodo;

import java.time.LocalDate;

public record AgendamentoRespostaDTO(
        Long idAgendamento,
        Long idPrestador,
        String nomePrestador,
        String telefonePrestador,
        String fotoPrestador,
        String cidadePrestador,
        String estadoPrestador,
        String categoriaPrestador,
        LocalDate data,
        Periodo periodo
) {
}
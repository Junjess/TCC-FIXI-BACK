package com.fixi.fixi.dto.response;

import com.fixi.fixi.model.Periodo;
import com.fixi.fixi.model.StatusAgendamento;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
public class AgendamentoSolicitacaoResponseDTO {
    private Long idAgendamento;

    // Dados do cliente
    private Long idCliente;
    private String nomeCliente;
    private String telefoneCliente;
    private String fotoCliente;

    // Dados do agendamento
    private LocalDate data;
    private String periodo;
    private String statusAgendamento;
    private String servico;

    public AgendamentoSolicitacaoResponseDTO(
            Long idAgendamento,
            Long idCliente,
            String nomeCliente,
            String telefoneCliente,
            String fotoCliente,
            LocalDate data,
            Periodo periodo,
            StatusAgendamento status,
            String servico
    ) {
        this.idAgendamento = idAgendamento;
        this.idCliente = idCliente;
        this.nomeCliente = nomeCliente;
        this.telefoneCliente = telefoneCliente;
        this.fotoCliente = fotoCliente;
        this.data = data;
        this.periodo = periodo.name();
        this.statusAgendamento = status.name();
        this.servico = servico;
    }
}

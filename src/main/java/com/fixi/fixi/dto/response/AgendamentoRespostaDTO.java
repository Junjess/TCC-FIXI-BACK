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

    // 🔹 Dados do prestador
    private Long idPrestador;
    private String nomePrestador;
    private String telefonePrestador;
    private String fotoPrestador;
    private String cidadePrestador;
    private String estadoPrestador;
    private String categoriaAgendamento;

    // 🔹 Dados do cliente
    private Long idCliente;
    private String nomeCliente;
    private String telefoneCliente;
    private String fotoCliente;
    private String cidadeCliente;
    private String estadoCliente;

    // 🔹 Dados do agendamento
    private LocalDate data;
    private String periodo;
    private String statusAgendamento;
    private boolean avaliado;
    private Double nota;
    private String descricaoAvaliacao;
    private String canceladoPor; // CLIENTE ou PRESTADOR

    // 🔹 Novos campos
    private String descricaoServico;
    private Double valorSugerido;

    /**
     * Construtor completo (com avaliação e canceladoPor).
     */
    public AgendamentoRespostaDTO(
            Long idAgendamento,

            // prestador
            Long idPrestador,
            String nomePrestador,
            String telefonePrestador,
            String fotoPrestador,
            String cidadePrestador,
            String estadoPrestador,
            List<String> categoriasPrestador,

            // cliente
            Long idCliente,
            String nomeCliente,
            String telefoneCliente,
            String fotoCliente,
            String cidadeCliente,
            String estadoCliente,

            // agendamento
            LocalDate data,
            Periodo periodo,
            StatusAgendamento status,
            boolean avaliado,
            Double nota,
            String descricaoAvaliacao,
            String canceladoPor,

            // novos campos
            String descricaoServico,
            Double valorSugerido
    ) {
        this.idAgendamento = idAgendamento;

        // prestador
        this.idPrestador = idPrestador;
        this.nomePrestador = nomePrestador;
        this.telefonePrestador = telefonePrestador;
        this.fotoPrestador = fotoPrestador;
        this.cidadePrestador = cidadePrestador;
        this.estadoPrestador = estadoPrestador;
        this.categoriaAgendamento = categoriaAgendamento;

        // cliente
        this.idCliente = idCliente;
        this.nomeCliente = nomeCliente;
        this.telefoneCliente = telefoneCliente;
        this.fotoCliente = fotoCliente;
        this.cidadeCliente = cidadeCliente;
        this.estadoCliente = estadoCliente;

        // agendamento
        this.data = data;
        this.periodo = periodo.name();
        this.statusAgendamento = status.name();
        this.avaliado = avaliado;
        this.nota = nota;
        this.descricaoAvaliacao = descricaoAvaliacao;
        this.canceladoPor = canceladoPor;

        // novos campos
        this.descricaoServico = descricaoServico;
        this.valorSugerido = valorSugerido;
    }

    public AgendamentoRespostaDTO() {
    }
}

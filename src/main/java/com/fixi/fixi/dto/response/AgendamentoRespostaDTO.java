package com.fixi.fixi.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class AgendamentoRespostaDTO {
    private Long idAgendamento;

    // prestador
    private Long idPrestador;
    private String nomePrestador;
    private String telefonePrestador;
    private String fotoPrestador;
    private String cidadePrestador;
    private String estadoPrestador;
    private String categoriaAgendamento;

    // cliente
    private Long idCliente;
    private String nomeCliente;
    private String telefoneCliente;
    private String fotoCliente;
    private String fotoTipoCliente;
    private String cidadeCliente;
    private String estadoCliente;

    // agendamento
    private LocalDate data;
    private String periodo;
    private String statusAgendamento;
    private String canceladoPor;

    private Boolean avaliacaoClienteFeita;     // cliente -> prestador
    private Boolean avaliacaoPrestadorFeita;   // prestador -> cliente

    private Double notaAvaliacaoPrestador;         // cliente -> prestador
    private String comentarioAvaliacaoPrestador;
    private Double notaAvaliacaoCliente;           // prestador -> cliente
    private String comentarioAvaliacaoCliente;

    private String descricaoServico;
    private Double valorSugerido;

    public AgendamentoRespostaDTO() {}
}


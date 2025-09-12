package com.fixi.fixi.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PrestadorDetalhesResponseDTO {
    private Long id;
    private String nome;
    private String telefone;
    private String foto;
    private String cidade;
    private String estado;
    private String descricao;
    private String categoria;
    private Double mediaAvaliacao;
    private List<AvaliacaoResponseDTO> avaliacoes;

    public PrestadorDetalhesResponseDTO(Long id,
                                String nome,
                                String telefone,
                                String foto,
                                String cidade,
                                String estado,
                                String descricao,
                                String categoria,
                                Double mediaAvaliacao,
                                List<AvaliacaoResponseDTO> avaliacoes) {
        this.id = id;
        this.nome = nome;
        this.telefone = telefone;
        this.foto = foto;
        this.cidade = cidade;
        this.estado = estado;
        this.descricao = descricao;
        this.categoria = categoria;
        this.mediaAvaliacao = mediaAvaliacao;
        this.avaliacoes = avaliacoes;
    }
}
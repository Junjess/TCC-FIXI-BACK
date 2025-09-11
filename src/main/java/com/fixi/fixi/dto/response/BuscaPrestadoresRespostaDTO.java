package com.fixi.fixi.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BuscaPrestadoresRespostaDTO {

    Long id;
    String nome;
    String telefone;
    String foto;
    String cidade;
    String estado;
    String descricao;
    String categoria;
    Double mediaAvaliacao;

    public BuscaPrestadoresRespostaDTO(Long id, String nome, String telefone, String foto, String cidade, String estado, String descricao, String categoria, Double mediaAvaliacao) {
        this.id = id;
        this.nome = nome;
        this.telefone = telefone;
        this.foto = foto;
        this.cidade = cidade;
        this.estado = estado;
        this.descricao = descricao;
        this.categoria = categoria;
        this.mediaAvaliacao = mediaAvaliacao;
    }
}

package com.fixi.fixi.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BuscaPrestadoresRespostaDTO {

    private Long id;
    private String nome;
    private String telefone;
    private String foto;
    private String cidade;
    private String estado;
    private List<CategoriaDescricaoDTO> categorias; // 👈 várias categorias
    private Double mediaAvaliacao;

    public BuscaPrestadoresRespostaDTO(Long id, String nome, String telefone, String foto,
                                       String cidade, String estado, List<CategoriaDescricaoDTO> categorias,
                                       Double mediaAvaliacao) {
        this.id = id;
        this.nome = nome;
        this.telefone = telefone;
        this.foto = foto;
        this.cidade = cidade;
        this.estado = estado;
        this.categorias = categorias;
        this.mediaAvaliacao = mediaAvaliacao;
    }
}

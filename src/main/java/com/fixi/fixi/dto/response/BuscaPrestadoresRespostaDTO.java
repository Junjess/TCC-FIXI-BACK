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
    private List<CategoriaDescricaoDTO> categorias;
    private Double mediaAvaliacao;
    private Double notaPlataforma;
    private String sobre;

    public BuscaPrestadoresRespostaDTO(Long id, String nome, String telefone, String foto,
                                       String cidade, String estado, List<CategoriaDescricaoDTO> categorias,
                                       Double mediaAvaliacao, Double notaPlataforma,  String sobre) {
        this.id = id;
        this.nome = nome;
        this.telefone = telefone;
        this.foto = foto;
        this.cidade = cidade;
        this.estado = estado;
        this.categorias = categorias;
        this.mediaAvaliacao = mediaAvaliacao;
        this.notaPlataforma = notaPlataforma;
        this.sobre = sobre;
    }
}

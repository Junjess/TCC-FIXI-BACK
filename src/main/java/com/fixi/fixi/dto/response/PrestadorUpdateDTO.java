package com.fixi.fixi.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class PrestadorUpdateDTO {
    private String nome;
    private String email;
    private String telefone;
    private String cidade;
    private String estado;
    private String cep;
    private String senha;
    private String sobre;

    // Aqui reflete exatamente o que vem do front
    private List<CategoriaDTO> categorias;

    @Getter
    @Setter
    public static class CategoriaDTO {
        private Long id;
        private String descricao;
    }
}
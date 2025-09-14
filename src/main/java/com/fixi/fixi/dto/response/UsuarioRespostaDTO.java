package com.fixi.fixi.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioRespostaDTO {
    private Long id;
    private String nome;
    private String email;
    private String foto;


    public UsuarioRespostaDTO(Long id, String nome, String email, String foto) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.foto = foto;
    }
}

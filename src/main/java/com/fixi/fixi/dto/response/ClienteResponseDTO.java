package com.fixi.fixi.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor

public class ClienteResponseDTO {

    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String cidade;
    private String estado;
    private String foto;
    private String fotoTipo;

    public ClienteResponseDTO(
            Long id,
            String nome,
            String email,
            String telefone,
            String cidade,
            String estado,
            String foto,
            String fotoTipo
    ) {
        this.id = id;
        this.nome = nome != null ? nome : "";
        this.email = email != null ? email : "";
        this.telefone = telefone != null ? telefone : "";
        this.cidade = cidade != null ? cidade : "";
        this.estado = estado != null ? estado : "";
        this.foto = foto;
        this.fotoTipo = fotoTipo != null ? fotoTipo : "";
    }
}

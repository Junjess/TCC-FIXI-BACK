package com.fixi.fixi.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor       // 🔹 Necessário para Jackson desserializar sem erro

public class ClienteResponseDTO {

    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String cidade;
    private String estado;
    private String foto;

    // 🔹 Construtor auxiliar que nunca lança NullPointer
    public ClienteResponseDTO(
            Long id,
            String nome,
            String email,
            String telefone,
            String cidade,
            String estado,
            String foto
    ) {
        this.id = id;
        this.nome = nome != null ? nome : "";
        this.email = email != null ? email : "";
        this.telefone = telefone != null ? telefone : "";
        this.cidade = cidade != null ? cidade : "";
        this.estado = estado != null ? estado : "";
        this.foto = foto; // pode ficar null, o front trata e mostra avatar padrão
    }
}

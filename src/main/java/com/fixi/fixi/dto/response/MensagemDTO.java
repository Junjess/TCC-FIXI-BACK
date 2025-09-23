package com.fixi.fixi.dto.response;

import com.fixi.fixi.model.Autor;
import com.fixi.fixi.model.Mensagem;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MensagemDTO {
    private Autor autor;
    private String texto;

    public static MensagemDTO fromEntity(Mensagem mensagem) {
        MensagemDTO dto = new MensagemDTO();
        dto.setAutor(mensagem.getAutor());
        dto.setTexto(mensagem.getTexto());
        return dto;
    }
}

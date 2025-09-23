package com.fixi.fixi.dto.response;

import com.fixi.fixi.model.Conversa;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ConversaDTO {
    private Long id;
    private String titulo;
    private LocalDateTime dataInicio;
    private List<MensagemDTO> mensagens;

    public static ConversaDTO fromEntity(Conversa conversa) {
        ConversaDTO dto = new ConversaDTO();
        dto.setId(conversa.getId());
        dto.setTitulo(conversa.getTitulo());
        dto.setDataInicio(conversa.getDataInicio());

        if (conversa.getMensagens() != null) {
            dto.setMensagens(conversa.getMensagens()
                    .stream()
                    .map(MensagemDTO::fromEntity)
                    .toList());
        }

        return dto;
    }
}


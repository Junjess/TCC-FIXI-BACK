package com.fixi.fixi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AvaliacaoResponseDTO {
    private Double nota;
    private String clienteNome;
    private String descricao;
}
package com.fixi.fixi.dto.response;

import com.fixi.fixi.model.AvaliacaoTipo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class AvaliacaoDirecionalResponseDTO {
    private Double nota;
    private String avaliadorNome;
    private String avaliadoNome;
    private String descricao;
    private AvaliacaoTipo tipo;
}
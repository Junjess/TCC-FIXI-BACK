package com.fixi.fixi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrestadorResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String cidade;
    private String estado;
    private String cep;
    private String foto; // base64
    private Double mediaAvaliacao;
    private List<CategoriaDescricaoDTO> categorias;
}
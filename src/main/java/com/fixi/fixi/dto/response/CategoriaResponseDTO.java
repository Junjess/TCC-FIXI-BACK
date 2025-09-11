package com.fixi.fixi.dto.response;

import com.fixi.fixi.model.Categoria;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaResponseDTO {
    private Long id;
    private String nome;

    public static CategoriaResponseDTO from(Categoria c) {
        return new CategoriaResponseDTO(c.getId(), c.getNome());
    }
}
package com.fixi.fixi.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RegistroPrestadorRequest {
    private String nome;
    private String email;
    private String senha;
    private String cep;
    private String telefone;
    private String tipoUsuario;

    // Lista de IDs ou nomes das categorias escolhidas
    private List<Integer> categoriasIds;
}

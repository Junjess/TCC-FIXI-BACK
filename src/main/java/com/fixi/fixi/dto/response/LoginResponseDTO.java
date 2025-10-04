package com.fixi.fixi.dto.response;

public class LoginResponseDTO<T> {
    private String token;
    private T usuario; // pode ser UsuarioRespostaDTO ou PrestadorResponseDTO

    public LoginResponseDTO(String token, T usuario) {
        this.token = token;
        this.usuario = usuario;
    }

    public String getToken() { return token; }
    public T getUsuario() { return usuario; }
}
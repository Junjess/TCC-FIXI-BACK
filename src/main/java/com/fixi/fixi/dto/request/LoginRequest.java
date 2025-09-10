package com.fixi.fixi.dto.request;

public class LoginRequest {
    private String email;
    private String senha;
    private String tipoUsuario;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getTipoUsuario() { return tipoUsuario; }
    public void setTipoUsuariopo(String tipoUsuariopo) { this.tipoUsuario = tipoUsuario; }
}